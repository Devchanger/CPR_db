package com.cpr_db.cpr_db.config;

import com.cpr_db.cpr_db.entity.Knowledge;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.KnowledgeRepository;
import com.cpr_db.cpr_db.repository.SceneRepository;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final VideoRepository videoRepository;
    private final SceneRepository sceneRepository;
    private final KnowledgeRepository knowledgeRepository;

    public DataSeeder(VideoRepository videoRepository,
                      SceneRepository sceneRepository,
                      KnowledgeRepository knowledgeRepository) {
        this.videoRepository = videoRepository;
        this.sceneRepository = sceneRepository;
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public void run(String... args) {
        if (videoRepository.count() == 0) {
            videoRepository.save(new Video("video1", "https://example.com/videos/video1.mp4", 120));
            videoRepository.save(new Video("video2", "https://example.com/videos/video2.mp4", 180));
        }

        if (sceneRepository.count() == 0) {
            sceneRepository.save(scene("成人 CPR 训练", "标准成人胸外按压与人工呼吸训练场景", "basic", "heart", 1));
            sceneRepository.save(scene("儿童 CPR 训练", "针对 1-8 岁儿童的 CPR 操作训练场景", "basic", "child", 2));
            sceneRepository.save(scene("AED 使用", "自动体外除颤器 (AED) 操作训练场景", "basic", "zap", 3));
            sceneRepository.save(scene("气道异物梗阻", "海姆立克急救法训练场景", "advanced", "alert", 4));
            sceneRepository.save(scene("综合考核", "包含所有急救技能的综合考核场景", "advanced", "star", 5));
        }

        if (knowledgeRepository.count() == 0) {
            knowledgeRepository.save(knowledge("心肺复苏 (CPR) 基本步骤",
                    "1. 确认现场安全\n2. 判断患者意识和呼吸\n3. 呼叫急救 (拨打 120)\n4. 开始胸外按压: 深度 5-6cm, 频率 100-120 次/分\n5. 开放气道\n6. 人工呼吸 (30:2)\n7. 持续至 AED 到达或专业救护人员接手",
                    "基础", "CPR,心肺复苏,基础"));
            knowledgeRepository.save(knowledge("AED 使用指南",
                    "1. 打开 AED 电源\n2. 按照语音提示贴上电极片\n3. 确保无人接触患者\n4. 按下电击按钮 (如需电击)\n5. 电击后立即继续 CPR，无需移除电极片",
                    "AED", "AED,除颤,急救"));
            knowledgeRepository.save(knowledge("成人与儿童 CPR 区别",
                    "成人 CPR:\n- 双手按压，深度 5-6cm\n- 30:2 按压通气比\n\n儿童 CPR (1-8 岁):\n- 单手或双手按压，深度约 5cm\n- 30:2 按压通气比 (单人)，15:2 (双人)\n\n婴儿 CPR (<1 岁):\n- 两指按压，深度约 4cm\n- 30:2 按压通气比",
                    "基础", "CPR,儿童,成人,区别"));
        }
    }

    private Scene scene(String name, String desc, String type, String icon, int sortOrder) {
        Scene s = new Scene();
        s.setName(name);
        s.setDescription(desc);
        s.setType(type);
        s.setIcon(icon);
        s.setSortOrder(sortOrder);
        return s;
    }

    private Knowledge knowledge(String title, String content, String category, String tags) {
        Knowledge k = new Knowledge();
        k.setTitle(title);
        k.setContent(content);
        k.setCategory(category);
        k.setTags(tags);
        return k;
    }
}
