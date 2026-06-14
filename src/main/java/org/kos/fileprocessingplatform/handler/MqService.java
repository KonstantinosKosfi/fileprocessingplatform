package org.kos.fileprocessingplatform.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kos.fileprocessingplatform.config.properties.FileProcessingProperties;
import org.kos.fileprocessingplatform.config.properties.Folders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqService {

    private final FileProcessingProperties properties;
    private final Folders folders;
    private final JmsTemplate jmsTemplate;
    @Value("${app.mq.queue-in}")
    private String queueIn;


    public void initJobFile(MultipartFile file) throws IOException {
        log.debug("init job file");
        validationFile(file);
        String fileName = sanitizeFileName(file.getOriginalFilename());
        log.info("File {} passed validation", fileName);
        saveToInputFolder(file, fileName);

        processFile(file, fileName);

    }

    public void initJobFile(Path filePath) throws IOException {
        validatePathFile(filePath);

        String fileName = filePath.getFileName().toString();
        String content = Files.readString(filePath, StandardCharsets.UTF_8);

        log.info("Processing file from folder: {}", filePath.toAbsolutePath());

        jmsTemplate.convertAndSend(queueIn, content, message -> {
            message.setStringProperty("fileName", fileName);
            message.setStringProperty("contentType", "application/xml");
            return message;
        });

        log.info("Sent file {} to queue {}", fileName, queueIn);
    }

    private void validatePathFile(Path filePath) {
        if (filePath == null || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("File does not exist");
        }

        if (!hasAllowedExtension(filePath.getFileName().toString())) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        validateFileSize(filePath);
    }

    private void processFile(MultipartFile file, String fileName) throws IOException {
        log.info("Processing file {}", fileName);
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        jmsTemplate.convertAndSend(queueIn, content, message -> {
            message.setStringProperty("fileName", fileName);
            message.setStringProperty("contentType", file.getContentType());
            return message;
        });
    }

    private void saveToInputFolder(MultipartFile file, String fileName) throws IOException {
        Path inputDir = Path.of(folders.getInput());
        Files.createDirectories(inputDir);

        Path inputFile = resolveInside(inputDir, fileName);
        Files.write(inputFile, file.getBytes());

        log.info("Saved original file to {}", inputFile.toAbsolutePath());
    }

    private void validationFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxFileSizeBytes()) {
            throw new IllegalArgumentException("File exceeds maximum allowed size");
        }

        if (!hasAllowedExtension(file.getOriginalFilename())) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private void validateFileSize(Path filePath) {
        try {
            if (Files.size(filePath) > maxFileSizeBytes()) {
                throw new IllegalArgumentException("File exceeds maximum allowed size");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not determine file size", e);
        }
    }

    private long maxFileSizeBytes() {
        return properties.getMaxFileSizeMb() * 1024L * 1024L;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }

        String normalizedSeparators = fileName.replace('\\', '/');
        String sanitized = Paths.get(normalizedSeparators).getFileName().toString();

        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return sanitized;
    }

    private Path resolveInside(Path directory, String fileName) {
        Path base = directory.toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();

        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        return target;
    }


    public boolean hasAllowedExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return false;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return properties.getAllowedExtensions().contains(extension);
    }
}
