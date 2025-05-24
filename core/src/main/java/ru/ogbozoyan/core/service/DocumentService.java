package ru.ogbozoyan.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;

@Service
@Slf4j
public class DocumentService {

    @Autowired
    private DocumentEntityRepository documentRepository;

    @Autowired
    private DesckewService desckewService;


    /*@Transactional
    public Flux<?> uploadDocumentAndProcess(MultipartFile multipartFile) {

        //1
        DocumentEntity uploadedDoc = documentRepository.save(DocumentEntity.builder().build());

        //2
        //put in bucket with uploadedDoc.id()

        //3 Receive link to file and update uploadedDoc

        //4 Call python backend to split normalises and receive 11 parts

        //5 Put

    }*/


    public DocumentEntity save(DocumentEntity documentEntity) {
        return documentRepository.save(documentEntity);
    }

}
