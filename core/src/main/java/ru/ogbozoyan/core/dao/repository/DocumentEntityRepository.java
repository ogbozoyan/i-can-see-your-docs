package ru.ogbozoyan.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ogbozoyan.core.dao.entity.DocumentEntity;

import java.util.UUID;

public interface DocumentEntityRepository extends JpaRepository<DocumentEntity, UUID> {
}