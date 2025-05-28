package ru.ogbozoyan.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;
import ru.ogbozoyan.core.web.dto.S3CustomResponse;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentCrudService {
    private final DocumentEntityRepository documentRepository;
    private final S3Service s3Service;

    public DocumentCrudService(DocumentEntityRepository documentRepository, S3Service s3Service) {
        this.documentRepository = documentRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public DocumentEntity saveAndAddUrl(MultipartFile multipartFile) {
        //1
        DocumentEntity uploadedDoc = documentRepository.save(DocumentEntity.builder().build());

        //2
        S3CustomResponse s3CustomResponse = s3Service.uploadFile(uploadedDoc.getId(), multipartFile);

        //3 Receive link to file and update uploadedDoc
        uploadedDoc.setFileName(s3CustomResponse.filename());
        uploadedDoc.setS3Key(s3CustomResponse.key());
        uploadedDoc.setIsFullyProcessed(false);
        uploadedDoc.setIsSplit(false);
        documentRepository.save(uploadedDoc);
        return uploadedDoc;
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> findAll() {
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DocumentEntity findByUuid(UUID uuid) {
        return documentRepository.findById(uuid).orElse(null);
    }
}
