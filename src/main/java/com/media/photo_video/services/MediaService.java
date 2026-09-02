package com.media.photo_video.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.media.photo_video.model.Media;
import com.media.photo_video.repository.MediaRepository;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final Cloudinary cloudinary;

    // Inject both the database repository and the Cloudinary configuration bean
    public MediaService(MediaRepository mediaRepository, Cloudinary cloudinary) {
        this.mediaRepository = mediaRepository;
        this.cloudinary = cloudinary;
    }

    public Media uploadImage(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new IllegalArgumentException("Only image and video files are allowed");
        }

        // Determine dynamic asset classification type required by Cloudinary API
        String resourceType = contentType.startsWith("video/") ? "video" : "image";

        // Upload raw byte streams directly to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType
        ));

        // Extract the permanent HTTPS URL and the unique public ID (needed for deletion logs)
        String secureUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        String originalFilename = file.getOriginalFilename();

        Media media = new Media();
        media.setOriginalFilename(originalFilename);

        // CRITICAL STORAGE CHOICE:
        // We reuse storedFilename to keep the publicId, and filePath to hold the absolute cloud web link
        media.setStoredFilename(publicId);
        media.setContentType(contentType);
        media.setFileSize(file.getSize());
        media.setFilePath(secureUrl);
        media.setCreatedAt(LocalDateTime.now());

        return mediaRepository.save(media);
    }

    public List<Media> getAllMedia() {
        return mediaRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Media getMediaById(Long id) {
        return mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Media not found"));
    }

    public void deleteMedia(Long id) throws Exception {
        Media media = mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Media not found"));

        // Determine target classification type for deletion routing 
        String resourceType = media.getContentType() != null && media.getContentType().startsWith("video/")
                ? "video"
                : "image";

        // Remove the asset permanently from Cloudinary servers using the stored public ID
        cloudinary.uploader().destroy(media.getStoredFilename(), ObjectUtils.asMap(
                "resource_type", resourceType
        ));

        // Delete the entry record from your Aiven MySQL database
        mediaRepository.delete(media);
    }
}
