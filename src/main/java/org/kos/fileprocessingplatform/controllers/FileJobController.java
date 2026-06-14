package org.kos.fileprocessingplatform.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.kos.fileprocessingplatform.handler.MqService;
import org.kos.fileprocessingplatform.models.FileJobEntity;
import org.kos.fileprocessingplatform.services.FileJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
@SecurityRequirement(name = "bearerAuth")
public class FileJobController {

    private final FileJobService fileJobService;
    private final MqService mqService;

    @GetMapping
    public ResponseEntity<List<FileJobEntity>> getMyJobs() {
        return ResponseEntity.ok(fileJobService.getCurrentUserJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileJobEntity> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(fileJobService.getJobById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        fileJobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/init-job-file")
    public ResponseEntity<FileJobEntity> initJobFile(@RequestPart("file")  MultipartFile file) throws IOException {
        mqService.initJobFile(file);
        return ResponseEntity.ok().build();
    }
}
