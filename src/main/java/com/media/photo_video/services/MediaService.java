package com.media.photo_video.services;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.media.photo_video.model.Media;
import com.media.photo_video.repository.MediaRepository;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    private final Path uploadDirectory = Paths.get("uploads");

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Media uploadImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Nly image files are allowed");
        }

        Files.createDirectories(uploadDirectory);

        String originalFilename = file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFilename = UUID.randomUUID() + extension;

        Path filePath = uploadDirectory.resolve(storedFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Media media = new Media();

        media.setOriginalFilename(originalFilename);
        media.setStoredFilename(storedFilename);
        media.setContentType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setFilePath(filePath.toString());
        media.setCreatedAt(LocalDateTime.now());

        return mediaRepository.save(media);

    }

    public List<Media> getAllMedia() {
        return mediaRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Media getMediaById(Long id) {
        return mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Media not found"));
    }

    public void deleteMedia(Long id) throws IOException {
        Media media = mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Media not found"));

        Path filePath = Paths.get(media.getFilePath());

        Files.deleteIfExists(filePath);

        mediaRepository.delete(media);
    }

}
