package com.cpr_db.cpr_db.common;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

/**
 * D17/D18: shared Chinese-name → pinyin username logic used by
 * BE-B-03 / DM-B-01 / BE-C-05.
 */
public final class PinyinUtil {

    private PinyinUtil() {
    }

    /**
     * Convert a Chinese name to pinyin without tones (张三 -> zhangsan).
     * Non-Chinese characters are preserved by jpinyin.
     */
    public static String pinyin(String text) {
        try {
            return PinyinHelper.convertToPinyinString(text, "", PinyinFormat.WITHOUT_TONE);
        } catch (PinyinException e) {
            throw new BusinessException(400, "无法将姓名转换为拼音: " + text);
        }
    }

    /**
     * D18: initial password rule — pad with '1' until at least 6 chars
     * (lisi -> lisi11), satisfying /^[0-9A-Za-z]{6,}$/.
     */
    public static String initialPassword(String username) {
        if (username == null || username.length() >= 6) {
            return username;
        }
        return username + "1".repeat(6 - username.length());
    }
}
