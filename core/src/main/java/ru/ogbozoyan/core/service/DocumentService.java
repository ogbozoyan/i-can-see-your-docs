package ru.ogbozoyan.core.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ogbozoyan.core.configuration.exception.DocumentServiceException;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.dao.entity.TableBig;
import ru.ogbozoyan.core.dao.entity.TableNamesEnum;
import ru.ogbozoyan.core.dao.entity.TableSmall;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;
import ru.ogbozoyan.core.web.api.DocumentEditTableResultReauestDTO;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.LAST_NUMBER_TABLE;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_1;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_1_2;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_2_1;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_2_2;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_3_1;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_3_2;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_4_1;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_4_2;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_5_1;
import static ru.ogbozoyan.core.dao.entity.TableNamesEnum.TABLE_5_2;

@Slf4j
@Service
public class DocumentService {

    private final DocumentEntityRepository documentRepository;

    private final DesckewService desckewService;

    private final S3Service s3Service;

    private final AiService aiService;

    private final Executor uploadExecutor;

    private final DocumentCrudService documentCrudService;

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

    public record AiProcessingResult(TableBig bigTable, List<TableSmall> smallTables, Number employeeNumber) {
    }


    public DocumentService(DocumentEntityRepository documentRepository, DesckewService desckewService, S3Service s3Service, AiService aiService, Executor uploadExecutor,
                           DocumentCrudService documentCrudService) {
        this.documentRepository = documentRepository;
        this.desckewService = desckewService;
        this.s3Service = s3Service;
        this.aiService = aiService;
        this.uploadExecutor = uploadExecutor;
        this.documentCrudService = documentCrudService;
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> findAll() {
        return documentCrudService.findAll();
    }

    @Transactional
    public DocumentEntity editTableResult(UUID uuid, TableNamesEnum tableName, Boolean isBig, DocumentEditTableResultReauestDTO tableResultReauestDTO) {

        DocumentEntity byUuid = documentCrudService.findByUuid(uuid);
        if (isBig) {
            byUuid.setTable_1_Result(tableResultReauestDTO.tableBig());
        } else {
            byUuid.setSmallTableByName(tableName, tableResultReauestDTO.tableSmall());
        }
        return documentCrudService.saveAndFlush(byUuid);
    }

    public Boolean reprocessTable(UUID uuid, TableNamesEnum tableName) {
        DocumentEntity byUuid = documentCrudService.findByUuid(uuid);

        if (!byUuid.getIsFullyProcessed() || !byUuid.getIsSplit()) {
            return false;
        }

        String urlByTableName = byUuid.getUrlByTableName(tableName);
        if (tableName.equals(LAST_NUMBER_TABLE)) {
            BigDecimal bigDecimal = aiService.processEmployeeAi(urlByTableName);
            byUuid.setEmployeeNumberResult(bigDecimal);
            documentCrudService.saveAndFlush(byUuid);
            return true;
        } else if (tableName.equals(TABLE_1)) {
            TableBig tableBig = aiService.processTableAiBigTable(urlByTableName);
            byUuid.setTable_1_Result(tableBig);
            documentCrudService.saveAndFlush(byUuid);
            return true;
        } else {
            TableSmall tableSmall = aiService.processTableAiSmall(urlByTableName, tableName);
            byUuid.setSmallTableByName(tableName, tableSmall);
            documentCrudService.saveAndFlush(byUuid);
            return true;
        }

    }


    @Transactional(readOnly = true)
    public DocumentEntity findByUuid(UUID uuid) {
        return documentCrudService.findByUuid(uuid);
    }

    @SuppressWarnings({"LoggingSimilarMessage"})
    public DocumentEntity uploadDocument(MultipartFile multipartFile) {
        return Mono.just(documentCrudService.saveAndAddUrl(multipartFile)).block();
    }

    @Deprecated
    public Flux<UploadedFileResponse> splitDocumentToPartsAndUploadToS3Stream(UUID uuid) {
        DocumentEntity documentEntity = lock.execute(
            () -> documentRepository.findById(uuid).orElseThrow(() -> new DocumentServiceException("Document not found"))
        );

        if (documentEntity.getIsSplit() != null && documentEntity.getIsSplit()) {
            return alreadySplitFlux(documentEntity);
        }

        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap = getSplitFiles(
            documentEntity.getS3Key() + documentEntity.getFileName()
        );

        log.info("Received {} files", nameAndResourceMap.size());

        List<Mono<UploadedFileResponse>> uploadMonos = uploadSplitFiles(nameAndResourceMap, documentEntity);

        documentEntity.setIsSplit(true);
        documentRepository.saveAndFlush(documentEntity);
        return Flux.concat(uploadMonos);
    }

    public List<UploadedFileResponse> splitDocumentToPartsAndUploadToS3(UUID uuid) {
        DocumentEntity documentEntity = lock.execute(
            () -> documentRepository.findById(uuid).orElseThrow(() -> new DocumentServiceException("Document not found"))
        );

        if (documentEntity.getIsSplit() != null && documentEntity.getIsSplit()) {
            return alreadySplit(documentEntity);
        }
        ConcurrentHashMap<String, ByteArrayResource> nameAndResourceMap = getSplitFiles(
            documentEntity.getS3Key() + documentEntity.getFileName()
        );

        log.info("Received {} files", nameAndResourceMap.size());

        List<Mono<UploadedFileResponse>> uploadMonos = uploadSplitFiles(nameAndResourceMap, documentEntity);

        documentEntity.setIsSplit(true);
        documentRepository.saveAndFlush(documentEntity);
        return Flux.concat(uploadMonos).collectList().block();
    }


    //6 Upload all parts to S3

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
                        setUrlFieldByName(name, response.key() + name, uploadedDoc);
                        documentRepository.save(uploadedDoc);
                    }

                    return new UploadedFileResponse(name, response.key() + name);
                }, uploadExecutor)
            );

            monos.add(uploadMono);
        }

        return monos;
    }

    //7 Call Ai service to generate tables
    @Transactional
    public AiProcessingResult processDocumentToAi(UUID uuid) {
        log.info("Processing document {}", uuid);
        DocumentEntity documentEntity = lock.execute(
            () -> documentRepository.findById(uuid).orElseThrow(() -> new DocumentServiceException("Document not found"))
        );

        if (documentEntity.getIsSplit() == null || !documentEntity.getIsSplit()) {
            throw new DocumentServiceException("Докумен не разбит на части, надо сначала разбить на части");
        }

        if (documentEntity.getIsFullyProcessed() != null && documentEntity.getIsFullyProcessed()) {
            log.info("Fully processed document {}", uuid);
            return null;
        }

        List<TableSmall> smallTables = new ArrayList<>();
        AtomicReference<TableBig> bigTable = new AtomicReference<>();
        AtomicReference<BigDecimal> employeeNumber = new AtomicReference<>();

        Arrays.stream(TableNamesEnum.values())
            .parallel()
            .forEach(tableNameEnum -> {
                    switch (tableNameEnum) {
                        case TABLE_1 -> {
                            log.info("Processing Big table 1");
                            bigTable.set(aiService.processTableAiBigTable(documentEntity.getUrlByTableName(tableNameEnum)));
                        }
                        case TABLE_1_2, TABLE_5_2, TABLE_5_1, TABLE_4_2, TABLE_4_1, TABLE_3_2, TABLE_3_1, TABLE_2_2, TABLE_2_1 -> {
                            log.info("Processing table {}", tableNameEnum);
                            smallTables.add(aiService.processTableAiSmall(documentEntity.getUrlByTableName(tableNameEnum), tableNameEnum));
                        }
                        case LAST_NUMBER_TABLE -> {
                            log.info("Processing last number table");
                            employeeNumber.set(aiService.processEmployeeAi(documentEntity.getUrlByTableName(tableNameEnum)));
                        }
                    }
                }
            );

        log.info("Processed all tables");
        setDocumentEntityProcessed(documentEntity, bigTable.get(), smallTables, employeeNumber.get());

        return new AiProcessingResult(bigTable.get(), smallTables, employeeNumber.get());
    }

    private void setDocumentEntityProcessed(DocumentEntity documentEntity, TableBig bigTable, List<TableSmall> smallTables, BigDecimal employeeNumber) {

        documentEntity.setIsFullyProcessed(true);
        documentEntity.setTable_1_Result(bigTable);
        documentEntity.setEmployeeNumberResult(employeeNumber);

        for (TableSmall tableSmall : smallTables) {
            switch (tableSmall.tableName()) {
                case TABLE_1_2 -> documentEntity.setTable_1_2_Result(tableSmall);
                case TABLE_2_1 -> documentEntity.setTable_2_1_result(tableSmall);
                case TABLE_2_2 -> documentEntity.setTable_2_2_Result(tableSmall);
                case TABLE_3_1 -> documentEntity.setTable_3_1_result(tableSmall);
                case TABLE_3_2 -> documentEntity.setTable_3_2_result(tableSmall);
                case TABLE_4_1 -> documentEntity.setTable_4_1_result(tableSmall);
                case TABLE_4_2 -> documentEntity.setTable_4_2_result(tableSmall);
                case TABLE_5_1 -> documentEntity.setTable_5_1_result(tableSmall);
                case TABLE_5_2 -> documentEntity.setTable_5_2_result(tableSmall);
            }
        }
        documentRepository.saveAndFlush(documentEntity);
    }

    //4 Call split and receive 11 parts
    private ConcurrentHashMap<String, ByteArrayResource> getSplitFiles(String filePath) {
        try {
            return desckewService.split(filePath);
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


    private static Flux<UploadedFileResponse> alreadySplitFlux(DocumentEntity documentEntity) {
        log.info("Document {} is already split", documentEntity.getId());
        return Flux.concat(Mono.fromSupplier(() -> new UploadedFileResponse("table_1.png", documentEntity.getS3Key() + "table_1.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_1_2.png", documentEntity.getS3Key() + "table_1_2.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_2_1.png", documentEntity.getS3Key() + "table_2_1.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_2_2.png", documentEntity.getS3Key() + "table_2_2.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_3_1.png", documentEntity.getS3Key() + "table_3_1.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_3_2.png", documentEntity.getS3Key() + "table_3_2.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_4_1.png", documentEntity.getS3Key() + "table_4_1.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_4_2.png", documentEntity.getS3Key() + "table_4_2.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_5_1.png", documentEntity.getS3Key() + "table_5_1.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("table_5_2.png", documentEntity.getS3Key() + "last_number.png")),
            Mono.fromSupplier(() -> new UploadedFileResponse("last_number.png", documentEntity.getS3Key() + "last_number.png"))
        );
    }

    private static List<UploadedFileResponse> alreadySplit(DocumentEntity documentEntity) {
        log.info("Document {} is already split", documentEntity.getId());
        return List.of(
            new UploadedFileResponse(TABLE_1.getName(), documentEntity.getS3Key() + TABLE_1.getName()),
            new UploadedFileResponse(TABLE_1_2.getName(), documentEntity.getS3Key() + TABLE_1_2.getName()),
            new UploadedFileResponse(TABLE_2_1.getName(), documentEntity.getS3Key() + TABLE_2_1.getName()),
            new UploadedFileResponse(TABLE_2_2.getName(), documentEntity.getS3Key() + TABLE_2_2.getName()),
            new UploadedFileResponse(TABLE_3_1.getName(), documentEntity.getS3Key() + TABLE_3_1.getName()),
            new UploadedFileResponse(TABLE_3_2.getName(), documentEntity.getS3Key() + TABLE_3_2.getName()),
            new UploadedFileResponse(TABLE_4_1.getName(), documentEntity.getS3Key() + TABLE_4_1.getName()),
            new UploadedFileResponse(TABLE_4_2.getName(), documentEntity.getS3Key() + TABLE_4_2.getName()),
            new UploadedFileResponse(TABLE_5_1.getName(), documentEntity.getS3Key() + TABLE_5_1.getName()),
            new UploadedFileResponse(TABLE_5_2.getName(), documentEntity.getS3Key() + TABLE_5_2.getName()),
            new UploadedFileResponse(LAST_NUMBER_TABLE.getName(), documentEntity.getS3Key() + LAST_NUMBER_TABLE.getName())
        );
    }
}
