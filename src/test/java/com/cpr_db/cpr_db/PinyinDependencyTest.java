package com.cpr_db.cpr_db;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * D17 smoke: the pinyin dependency resolves and converts Chinese names the way
 * BE-B-03 / DM-B-01 / BE-C-05 will need (张三 -> zhangsan).
 */
class PinyinDependencyTest {

    @Test
    void chineseName_convertsToPinyin() throws PinyinException {
        assertEquals("zhangsan", PinyinHelper.convertToPinyinString("张三", "", PinyinFormat.WITHOUT_TONE));
        assertEquals("lisi", PinyinHelper.convertToPinyinString("李四", "", PinyinFormat.WITHOUT_TONE));
    }
}
