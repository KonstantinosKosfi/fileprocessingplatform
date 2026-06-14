package org.kos.fileprocessingplatform.services;

import org.kos.fileprocessingplatform.models.FileJobEntity;

import java.util.List;

public interface FileJobService {
    FileJobEntity getJobById(Long jobId);

    List<FileJobEntity> getCurrentUserJobs();

    void deleteJob(Long jobId);
}
