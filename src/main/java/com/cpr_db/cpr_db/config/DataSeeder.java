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
            // ========== 基础 CPR ==========
            k("心肺复苏（CPR）是什么？",
              "心肺复苏（CPR）是一种紧急急救技术，通过胸外按压和人工呼吸维持心脏骤停患者的重要器官供血和供氧，直到专业医疗人员接手。它是抢救心搏骤停最有效的手段之一。",
              "基础", "CPR,定义");
            k("为什么要学习心肺复苏？",
              "心脏骤停后 4 分钟内是抢救黄金时间，每延迟 1 分钟存活率下降 7%-10%。普通人掌握 CPR 可以在急救人员到达前维持患者生命。80% 以上的院外心脏骤停发生在家中，学会 CPR 可能挽救家人的生命。",
              "基础", "CPR,重要性");
            k("心肺复苏的基本步骤是什么？",
              "国际通用的 CPR 步骤为'叫叫 CABD'：\n①叫：确认现场安全，呼叫患者看有无反应\n②叫：呼叫急救电话 120，并找人取 AED\n③C（Compression）：胸外按压，深度 5-6cm，频率 100-120 次/分\n④A（Airway）：开放气道，仰头抬颏\n⑤B（Breathing）：人工呼吸 2 次，每次约 1 秒\n⑥D（Defibrillation）：AED 到达后立即使用",
              "基础", "CPR,步骤,CABD");
            k("如何判断是否需要做心肺复苏？",
              "①轻拍双肩大声呼叫，检查有无反应\n②观察胸部有无起伏，判断有无正常呼吸（5-10 秒内）\n③若患者无反应且无呼吸或仅有濒死喘息，立即开始 CPR\n注意：不要求普通施救者检查脉搏，以免延误抢救",
              "基础", "CPR,判断");
            k("成人心肺复苏的按压深度和频率是多少？",
              "按压深度：至少 5 厘米，不超过 6 厘米\n按压频率：100-120 次/分钟\n按压位置：两乳头连线中点（胸骨下半部）\n要领：用掌根按压，双臂垂直，借身体重力下压，每次按压后让胸廓完全回弹",
              "基础", "CPR,按压,深度,频率");
            k("人工呼吸的正确方法是什么？",
              "①开放气道：仰头抬颏法（一手压前额、另一手抬下颏）\n②捏住患者鼻翼\n③口对口完全包住患者嘴唇\n④缓慢吹气约 1 秒，见胸廓隆起即可\n⑤松开鼻翼，让气体呼出\n⑥共吹气 2 次，每次间隔 1 秒\n按压与通气比例为 30:2",
              "基础", "CPR,人工呼吸");
            k("什么是 30:2 的按压通气比？",
              "30:2 是指进行 30 次胸外按压后，给予 2 次人工呼吸，这是一个完整的 CPR 循环。成人单人施救或双人施救均采用此比例。儿童双人施救时采用 15:2 的比例。",
              "基础", "CPR,比例");
            k("CPR 过程中如何判断是否有效？",
              "①每次按压时可触及大动脉搏动（如颈动脉）\n②患者面色、唇色由苍白或紫绀转为红润\n③瞳孔由大变小\n④出现自主呼吸或挣扎动作\n⑤意识逐渐恢复\n以上任一迹象出现都说明 CPR 有效，应继续施救",
              "基础", "CPR,效果,判断");

            // ========== AED ==========
            k("AED 是什么？",
              "AED（自动体外除颤器）是一种便携式急救设备，可以自动分析患者心律，判断是否需要电击除颤，并用语音指导施救者操作。使用简单，非专业人员经过简单培训即可掌握。",
              "AED", "AED,定义");
            k("AED 的使用步骤是什么？",
              "①打开 AED 电源开关\n②按照机器语音提示，将电极片贴在患者裸露的胸前（一片在右胸上部锁骨下方，一片在左胸外侧乳头下方）\n③确保无人接触患者，AED 自动分析心律\n④如需电击，按下闪烁的电击按钮\n⑤电击后立即继续 CPR，2 分钟后再由 AED 分析",
              "AED", "AED,步骤");
            k("使用 AED 时需要注意什么？",
              "①确保患者胸前干燥，如有水或汗液需擦干\n②贴电极片前移除患者胸前的药物贴片\n③电极片必须直接贴在裸露皮肤上，剪开衣服\n④电击时任何人不得接触患者\n⑤如患者有植入式起搏器，电极片应贴在距起搏器 2.5cm 以外\n⑥8 岁以下儿童应使用儿童电极片或儿童模式",
              "AED", "AED,注意事项");
            k("AED 电极片应该贴在什么位置？",
              "成人标准贴法：\n- 一片贴在右胸上部，锁骨正下方，胸骨右侧\n- 一片贴在左胸乳头外侧，腋前线内侧约 5-6cm 处\n确保电极片不接触、不重叠，避开锁骨、胸骨和乳头。前侧-后侧贴法也可作为备选方案。",
              "AED", "AED,电极片,位置");
            k("儿童可以使用 AED 吗？",
              "可以。1 岁以上儿童均可使用 AED。\n- 1-8 岁：优先使用儿童电极片或开启儿童模式\n- 如无儿童电极片，成人电极片也可使用，但需确保两片不接触\n- 1 岁以下婴儿：不推荐常规使用 AED，除非设备明确标注适用于婴儿",
              "AED", "AED,儿童");

            // ========== 儿童 CPR ==========
            k("儿童 CPR 和成人 CPR 有什么区别？",
              "①按压深度：儿童约 5cm，成人 5-6cm\n②按压手法：儿童根据体型用单手或双手，成人双手\n③按压通气比：单人施救均为 30:2；双人施救时儿童为 15:2\n④人工呼吸量：儿童只需看到胸部微微隆起即可\n⑤婴儿（<1 岁）用两指按压，深度约 4cm",
              "儿童CPR", "儿童,CPR,区别");
            k("婴儿心肺复苏应该怎么做？",
              "①确认安全，轻拍婴儿脚底检查反应\n②呼叫 120\n③用两指（食指和中指）在乳头连线正下方按压，深度约 4cm，频率 100-120 次/分\n④开放气道时注意不要过度仰头（婴儿气道更脆弱）\n⑤口对口鼻人工呼吸，吹气量只需看到胸部微隆\n⑥按压通气比 30:2（单人）",
              "儿童CPR", "婴儿,CPR");
            k("儿童 CPR 按压深度到底是多少？",
              "按压深度应为胸廓前后径的 1/3：\n- 婴儿（<1 岁）：约 4cm\n- 儿童（1-8 岁）：约 5cm\n- 8 岁以上体型较大儿童：参照成人标准 5-6cm\n切记：宁可深一点，也不要按得太浅，有效循环比肋骨骨折更重要。",
              "儿童CPR", "儿童,按压深度");

            // ========== 急救知识 ==========
            k("什么是海姆立克急救法？",
              "海姆立克急救法用于气道异物梗阻的急救：\n①站在患者背后，双臂环抱其腰部\n②一手握拳，拳心向内放在患者肚脐上方、胸骨下方\n③另一手抓住拳头，快速向上向内冲击腹部\n④持续冲击直到异物排出或患者意识丧失\n自救时可借助椅子靠背、桌角等冲击腹部",
              "急救", "海姆立克,异物梗阻");
            k("气道异物梗阻如何识别？",
              "轻度梗阻：患者能用力咳嗽、说话、呼吸\n重度梗阻：\n- 无法说话或咳嗽\n- 呼吸困难或呼吸时有尖锐声\n- 双手抓住喉咙（通用窒息手势）\n- 面色发紫或苍白\n重度梗阻需立即实施海姆立克急救法！",
              "急救", "异物梗阻,识别");
            k("遇到有人晕倒应该如何处理？",
              "①确保现场安全\n②判断患者意识（呼叫、拍肩）\n③如无反应→立即呼叫 120 并叫他人取 AED\n④检查呼吸（看胸部起伏 5-10 秒）\n⑤无呼吸→立即开始 CPR\n⑥有呼吸→摆成恢复体位，等待救援\n⑦持续观察呼吸状态",
              "急救", "晕倒,处理");
            k("什么是恢复体位？",
              "恢复体位（Recovery Position）是将有呼吸但无意识的患者侧卧放置：\n①患者平躺，将其近侧手臂伸直上举\n②远侧腿屈膝，脚掌平放\n③一手托住远侧脸颊，拉动远侧膝将患者翻向施救者一侧\n④调整上方腿使其髋膝弯曲着地\n⑤保持气道开放\n适用于有呼吸但意识不清的患者，防止舌根后坠和呕吐窒息。",
              "急救", "恢复体位");
            k("溺水者应该如何急救？",
              "①先确保自身安全再施救\n②将溺水者救上岸后，立即判断意识和呼吸\n③如无呼吸，先清理口鼻异物，给予 2 次人工呼吸\n④然后开始 30:2 的 CPR\n⑤即使在水中时间较长，也应持续 CPR 直到专业救援到达\n⑥注意保暖\n注意：溺水 CPR 比普通 CPR 更强调先做人工呼吸！",
              "急救", "溺水");
            k("什么是心搏骤停的常见原因？",
              "心搏骤停最常见的可逆原因''5H5T''：\n5H：低血氧、低血容量、低/高钾血症、氢离子过多（酸中毒）、低体温\n5T：张力性气胸、心包填塞、毒素中毒、肺栓塞、冠脉血栓\n了解这些有助于识别危险因素。",
              "急救", "心搏骤停,原因");

            // ========== 常见问题 ==========
            k("做 CPR 会不会造成肋骨骨折？",
              "有可能。正确的 CPR 按压可能导致肋骨或胸骨骨折，这在急救中是常见的。但请注意：肋骨骨折远好过死亡。有效的按压维持了大脑和心脏的供血，是挽救生命的关键。即使发生骨折，也应继续按压。",
              "常见问题", "CPR,骨折");
            k("可以只做胸外按压不做人工呼吸吗？",
              "可以。对于未经训练的普通人或不愿做口对口呼吸的施救者，Hands-Only CPR（仅胸外按压）也是有效的。研究表明，在院外心脏骤停的最初几分钟内，仅按压的 CPR 效果不亚于标准 CPR。但儿童和溺水者建议同时做人工呼吸。",
              "常见问题", "CPR,胸外按压");
            k("CPR 应该持续做多久？",
              "持续 CPR 直到出现以下情况之一：\n①患者恢复自主呼吸和意识\n②AED 到达并提示分析心律\n③专业医疗人员接手\n④施救者体力耗尽无法继续\n⑤现场变得不安全\n不要轻易放弃！有报道 CPR 持续 30 分钟以上仍成功救活患者。",
              "常见问题", "CPR,持续时间");
            k("如果施救者感到疲劳怎么办？",
              "CPR 非常消耗体力。如有其他人在场，应每 2 分钟（约 5 个 30:2 循环）轮换一次。轮换时应尽量缩短中断时间（<5 秒），在有 AED 时可利用分析心律的间隙轮换。如独自一人在场，尽力而为。",
              "常见问题", "CPR,疲劳,轮换");
            k("CPR 成功的标志是什么？",
              "CPR 成功的标志是 ROSC（自主循环恢复）：\n①出现可触及的脉搏\n②自主呼吸恢复\n③意识逐渐恢复\n④面色、口唇由紫绀转红润\n⑤瞳孔缩小\n注意：即使出现上述迹象，仍需持续观察并做好再次施救的准备，直到专业医护人员接手。",
              "常见问题", "CPR,成功,ROSC");
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

    private void k(String question, String answer, String category, String tags) {
        Knowledge k = new Knowledge();
        k.setQuestion(question);
        k.setAnswer(answer);
        k.setCategory(category);
        k.setTags(tags);
        knowledgeRepository.save(k);
    }
}
