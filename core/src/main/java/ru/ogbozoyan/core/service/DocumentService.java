package ru.ogbozoyan.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import ru.ogbozoyan.core.configuration.exception.DocumentServiceException;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DocumentService {

    private final DocumentEntityRepository documentRepository;

    private final DesckewService desckewService;

    private final S3Service s3Service;

    @Value("${app.descew.file-names}")
    private List<String> fileNames;

    public DocumentService(DocumentEntityRepository documentRepository, DesckewService desckewService, S3Service s3Service) {
        this.documentRepository = documentRepository;
        this.desckewService = desckewService;
        this.s3Service = s3Service;
    }


    @SuppressWarnings({"LoggingSimilarMessage"})
    @Transactional(noRollbackFor = DocumentServiceException.class)
    public Flux<?> uploadDocumentAndProcess(MultipartFile multipartFile) {

        DocumentEntity uploadedDoc = saveAndAddUrl(multipartFile);

        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap = getSplitFiles(multipartFile);
        log.info("Received {} files", nameAndResourceMap.size());

        uploadSplitFiles(nameAndResourceMap, uploadedDoc);

        //6. Make 11 parallel calls to Ai

        return Flux.just(uploadedDoc);
    }

    private DocumentEntity saveAndAddUrl(MultipartFile multipartFile) {
        //1
        DocumentEntity uploadedDoc = documentRepository.save(DocumentEntity.builder().build());

        //2
        S3CustomResponse s3CustomResponse = s3Service.uploadFile(uploadedDoc.getId(), multipartFile);

        //3 Receive link to file and update uploadedDoc
        uploadedDoc.setOriginalUrl(s3CustomResponse.key());
        documentRepository.save(uploadedDoc);
        return uploadedDoc;
    }

    private ConcurrentHashMap<String, ByteArrayResource> getSplitFiles(MultipartFile multipartFile) {
        //4 Call python backend to split normalises and receive 11 parts
        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap;
        try {
            ByteArrayResource byteArrayResource = new ByteArrayResource(multipartFile.getBytes());
            nameAndResourceMap = desckewService.uploadAndGetFiles(byteArrayResource);
        } catch (Exception e) {
            log.error("Could not send file to Desckew service", e);
            throw new DocumentServiceException(e);
        }
        return nameAndResourceMap;
    }

    private void uploadSplitFiles(ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap, DocumentEntity uploadedDoc) {
        //5 Put
        for (String name : nameAndResourceMap.keySet()) {

            if (!fileNames.contains(name)) {
                log.warn("Received file with name {}, but it should be one of {}", name, fileNames);
            }

            switch (name) {
                case "table_1.png" -> {
//                    CompletableFuture.supplyAsync(() -> {
//                        log.info("Uploading table 1...");
//                        return s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
//                    }).thenApplyAsync(response -> {
//                            return uploadedDoc.setTable_1_2_url(response.key());
//                        }
//                    );
                }
                case "table_1_2.png" -> {
                    log.info("Uploading table 1.2...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_1_2_url(response.key());
                }
                case "table_2_1.png" -> {
                    log.info("Uploading table 2.1...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_2_1_url(response.key());
                }
                case "table_2_2.png" -> {
                    log.info("Uploading table 2.2...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_2_2_url(response.key());
                }
                case "table_3_1.png" -> {
                    log.info("Uploading table 3.1...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_3_1_url(response.key());
                }
                case "table_3_2.png" -> {
                    log.info("Uploading table 3.2...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_3_2_url(response.key());
                }
                case "table_4_1.png" -> {
                    log.info("Uploading table 4.1...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_4_1_url(response.key());
                }
                case "table_4_2.png" -> {
                    log.info("Uploading table 4.2...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_4_2_url(response.key());
                }
                case "table_5_1.png" -> {
                    log.info("Uploading table 5.1...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_5_1_url(response.key());
                }
                case "table_5_2.png" -> {
                    log.info("Uploading table 5.2...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setTable_5_2_url(response.key());
                }
                case "last_number.png" -> {
                    log.info("Uploading employee number...");
                    S3CustomResponse response = s3Service.uploadFile(uploadedDoc.getId(), nameAndResourceMap.get(name), name);
                    uploadedDoc.setEmployeeNumberUrl(response.key());
                }
                default -> log.warn("Received file with name {}, but it should be one of {}", name, fileNames);
            }

        }
        documentRepository.save(uploadedDoc);
    }
}
