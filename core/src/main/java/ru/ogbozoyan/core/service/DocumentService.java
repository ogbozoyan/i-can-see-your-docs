package ru.ogbozoyan.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ogbozoyan.core.dao.repository.DocumentEntityRepository;

@Service
@Slf4j
public class DocumentService {

    @Autowired
    private DocumentEntityRepository documentRepository;

    public void saveDocument() {

    }

}
