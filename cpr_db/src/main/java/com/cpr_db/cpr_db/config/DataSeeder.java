package com.cpr_db.cpr_db.config;

import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final VideoRepository videoRepository;

    public DataSeeder(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public void run(String... args) {
        if (videoRepository.count() == 0) {
            videoRepository.save(new Video("video1", "https://example.com/videos/video1.mp4", 120));
            videoRepository.save(new Video("video2", "https://example.com/videos/video2.mp4", 180));
        }
    }
}
