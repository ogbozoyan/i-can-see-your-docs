package ru.ogbozoyan.core.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Lock;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ogbozoyan.core.configuration.exception.DocumentServiceException;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class DocumentService {

    private final DocumentEntityRepository documentRepository;

    private final DesckewService desckewService;

    private final S3Service s3Service;

    private final AiService aiService;

    private final Executor uploadExecutor;

    private final DocumentCrudService documentService;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Lock lock = Lock.of(reentrantLock);

    private final List<String> fileNames = new ArrayList<>();

    @PostConstruct
    public void init() {
        fileNames.add("table_1.png");
        fileNames.add("table_1_2.png");
        fileNames.add("table_2_1.png");
        fileNames.add("table_2_2.png");
        fileNames.add("table_3_1.png");
        fileNames.add("table_3_2.png");
        fileNames.add("table_4_1.png");
        fileNames.add("table_4_2.png");
        fileNames.add("table_5_1.png");
        fileNames.add("table_5_2.png");
        fileNames.add("last_number.png");

        log.info("Initialized files {}", fileNames);
    }

    public record UploadedFileResponse(String name, String url) {
    }


    public DocumentService(DocumentEntityRepository documentRepository, DesckewService desckewService, S3Service s3Service, AiService aiService, Executor uploadExecutor,
                           DocumentCrudService documentService) {
        this.documentRepository = documentRepository;
        this.desckewService = desckewService;
        this.s3Service = s3Service;
        this.aiService = aiService;
        this.uploadExecutor = uploadExecutor;
        this.documentService = documentService;
    }


    @SuppressWarnings({"LoggingSimilarMessage"})
    public Flux<UploadedFileResponse> uploadDocumentAndProcess(MultipartFile multipartFile) {

        DocumentEntity uploadedDoc = lock.execute(() -> documentService.saveAndAddUrl(multipartFile));

        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap = getSplitFiles(multipartFile);
        log.info("Received {} files", nameAndResourceMap.size());

        //6 Upload all parts to S3
        List<Mono<UploadedFileResponse>> uploadMonos = uploadSplitFiles(nameAndResourceMap, uploadedDoc);

        return Flux.merge(uploadMonos);
    }


    private List<Mono<UploadedFileResponse>> uploadSplitFiles(
        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap,
        DocumentEntity uploadedDoc
    ) {
        List<Mono<UploadedFileResponse>> monos = new ArrayList<>();

        for (Map.Entry<String, ByteArrayResource> entry : nameAndResourceMap.entrySet()) {
            String name = entry.getKey();
            ByteArrayResource resource = entry.getValue();

            if (!fileNames.contains(name)) {
                log.warn("Unexpected file: {}", name);
                continue;
            }

            Mono<UploadedFileResponse> uploadMono = Mono.fromFuture(
                CompletableFuture.supplyAsync(() -> {
                    log.info("Uploading {} in thread {}", name, Thread.currentThread().getName());
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), resource, name);

                    // Save URL to DocumentEntity
                    synchronized (uploadedDoc) {
                        setUrlFieldByName(name, response.key(), uploadedDoc);
                        documentRepository.save(uploadedDoc);
                    }

                    return new UploadedFileResponse(name, response.key());
                }, uploadExecutor)
            );

            monos.add(uploadMono);
        }

        return monos;
    }


    private ConcurrentHashMap<String, ByteArrayResource> getSplitFiles(MultipartFile multipartFile) {
        //4 Call python backend to split normalises and receive 11 parts
        try {
            return desckewService.uploadAndGetFiles(multipartFile.getResource());
        } catch (Exception e) {
            log.error("Could not send file to Desckew service", e);
            throw new DocumentServiceException(e);
        }
    }

    private void setUrlFieldByName(String name, String url, DocumentEntity doc) {
        switch (name) {
            case "table_1.png" -> doc.setTable_1_url(url);
            case "table_1_2.png" -> doc.setTable_1_2_url(url);
            case "table_2_1.png" -> doc.setTable_2_1_url(url);
            case "table_2_2.png" -> doc.setTable_2_2_url(url);
            case "table_3_1.png" -> doc.setTable_3_1_url(url);
            case "table_3_2.png" -> doc.setTable_3_2_url(url);
            case "table_4_1.png" -> doc.setTable_4_1_url(url);
            case "table_4_2.png" -> doc.setTable_4_2_url(url);
            case "table_5_1.png" -> doc.setTable_5_1_url(url);
            case "table_5_2.png" -> doc.setTable_5_2_url(url);
            case "last_number.png" -> doc.setEmployeeNumberUrl(url);
        }
    }


}
