package org.kos.fileprocessingplatform.services;

import lombok.RequiredArgsConstructor;
import org.kos.fileprocessingplatform.models.FileJobEntity;
import org.kos.fileprocessingplatform.models.UserEntity;
import org.kos.fileprocessingplatform.repositories.FileJobRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileJobServiceImpl implements FileJobService {

    private final FileJobRepo fileJobRepository;
    private final CurrentUserService currentUserService;


    @Override
    public FileJobEntity getJobById(Long jobId) {
        UserEntity currentUser = currentUserService.getCurrentUser();

        if (currentUserService.isAdmin(currentUser)) {
            return fileJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
        }

        return fileJobRepository.findByIdAndOwnerId(jobId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this job"));
    }

    @Override
    public List<FileJobEntity> getCurrentUserJobs() {
        UserEntity currentUser = currentUserService.getCurrentUser();

        if (currentUserService.isAdmin(currentUser)) {
            return fileJobRepository.findAll();
        }

        return fileJobRepository.findAllByOwnerId(currentUser.getId());
    }

    @Override
    public void deleteJob(Long jobId) {
        UserEntity currentUser = currentUserService.getCurrentUser();

        FileJobEntity job;

        if (currentUserService.isAdmin(currentUser)) {
            job = fileJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
        } else {
            job = fileJobRepository.findByIdAndOwnerId(jobId, currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("You do not have access to this job"));
        }

        fileJobRepository.delete(job);
    }
}
