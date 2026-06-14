package org.kos.fileprocessingplatform.repositories;

import org.kos.fileprocessingplatform.models.FileJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileJobRepo extends JpaRepository<FileJobEntity, Long> {
    Optional<FileJobEntity> findByIdAndOwnerId(Long id, UUID ownerId);
    List<FileJobEntity> findAllByOwnerId(UUID ownerId);
}
