package com.media.photo_video.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.media.photo_video.model.Media;

public interface MediaRepository extends JpaRepository<Media, Long> {

}
