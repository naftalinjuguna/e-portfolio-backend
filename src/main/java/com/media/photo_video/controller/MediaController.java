package com.media.photo_video.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.media.photo_video.model.Media;
import com.media.photo_video.services.MediaService;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/media")
@CrossOrigin
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Media> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Media media = mediaService.uploadImage(file);
            return ResponseEntity.ok(media);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia() {
        return ResponseEntity.ok(mediaService.getAllMedia());
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getMediaFile(@PathVariable Long id) {
        try {
            Media media = mediaService.getMediaById(id);

            Path path = Paths.get(media.getFilePath());

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, media.getContentType()).body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {

        try {
            mediaService.deleteMedia(id);

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();

        }
    }

}
