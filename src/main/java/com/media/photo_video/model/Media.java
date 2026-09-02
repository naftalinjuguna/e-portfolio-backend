package com.media.photo_video.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




@Entity
@Table(name = "media")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable=false)
    private String contentType;

    @Column(nullable=false)
    private Long fileSize;

    @Column(nullable=false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
