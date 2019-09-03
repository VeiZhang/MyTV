package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/8/29
 *     desc   : 提升解析速度，全文使用正则表达式匹配 + 单行字符串解析（单行不能用正则表达式，否则非常慢），速度更快
 *              超大文件可以考虑多线程
 *
 *              参考
 *                  https://github.com/ema987/m3u8parser    正则表达式解析
 *                  https://github.com/dholroyd/m3u8parser  正则表达式解析
 *                  https://github.com/crazyks/M3UPlayer    字符串解析解析
 *
 * </pre>
 */

public class M3UParser {

    private static final String TAG = M3UParser.class.getSimpleName();

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_SPACE = " ";
    private static final String EXT_QUOTES = "\"";

    private static final String ATTR_ID = "tvg-id";
    private static final String ATTR_NAME = "tvg-name";
    private static final String ATTR_LOGO = "tvg-logo";
    private static final String ATTR_GROUP_TITLE = "group-title";

    /**
     * 截取头部，保留 #EXTM3U，截断遇到的第一个#EXTINF
     */
    private static final String HEADER_REGEXP = String.format("%s[\\s\\S]*?(?=%s)", EXT_M3U, EXT_INF);

    /**
     * 匹配行的正则表达式
     * 说明：\cJ -> \n 换行符
     *
     * #EXTINF:([^,]+),([^\cJ]+)\cJ#EXTGRP:([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^\s]+)\s([^=]+)=([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     */
    private static final String ITEM_REGEXP = "#EXTINF:([^,]+),([^\\cJ]+)\\cJ([^\\cJ]+)";
    private static final Pattern ITEM_PATTERN = Pattern.compile(ITEM_REGEXP);

    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = new M3UPlayList();
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();
        try {
            Matcher itemMatcher = ITEM_PATTERN.matcher(content);
            while (itemMatcher.find()) {
                try {
                    String info = itemMatcher.group(1).trim();
                    M3UItem item = parseInfo(info);
                    item.setTitle(itemMatcher.group(2).trim());
                    item.setUrl(itemMatcher.group(3).trim());
                    itemList.add(item);
                } catch (Exception e) {
                    Log.e(TAG, "parse item error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parse error : " + e.getMessage());
        }
        m3uPlayList.setHeader(header);
        m3uPlayList.setItems(itemList);
        return m3uPlayList;
    }

    public static M3UPlayList parse(InputStream is) {
        String content = null;
        try {
            /**
             * Scanner对象将首先跳过输入流开头的所有空白分隔符，然后对输入流中的信息进行检查，直到遇到空白分隔符为止
             * Scanner 将空格当作了一个分隔符，那如何将含有空格的数据输出呢？
             * 这时就需要用Scanner.useDelimiter( )方法，可以将分隔符号修改为"回车"，或者其他字符。
             * useDelimiter默认以空格作为分隔符，\\A正则表达式，从字符串开头进行匹配
             */
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            if (scanner.hasNext()) {
                content = scanner.next();
            }
            CloseUtils.closeIOQuietly(is);
        } catch (Exception e) {
            Log.e(TAG, "parse: input stream error :" + e.getMessage());
        }
        return parse(content);
    }

    public static M3UPlayList parse(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (Exception e) {
            Log.e(TAG, "parse file error : " + e.getMessage());
        }
        return parse(is);
    }

    /**
     * #EXTINF:-1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD",||NL|| RTL 4 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/968
     * #EXTINF:-1,||NL|| RTL 5 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/967
     *
     * 完整信息转换为info信息是
     * -1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD"
     * -1
     *
     * @param info
     */
    private static M3UItem parseInfo(String info) {
        M3UItem item = new M3UItem();
        int index = info.indexOf(EXT_SPACE);
        int length = info.length();
        if (index == -1) {
            index = length;
        }
        String duration = info.substring(0, index);
        item.setDuration(duration);

        /**
         * 有空格则+1，无空格表示没有其他信息
         */
        index++;
        if (index < length) {
            info = info.substring(index).trim();
            item.setId(getAttr(info, ATTR_ID));
            item.setName(getAttr(info, ATTR_NAME));
            item.setLogo(getAttr(info, ATTR_LOGO));
            item.setGroupTitle(getAttr(info, ATTR_GROUP_TITLE));
        }

        return item;
    }

    private static String getAttr(String info, String key) {
        String value = null;
        if (info.contains(key)) {
            /**
             * tvg-id="RTL4.nl"
             * 先找tvg-id=为起始位置
             * 再找第二个引号为结束位置
             * 最后清除引号和空格
             */
            int startIndex = info.indexOf(key) + key.length() + 1;
            int endIndex = info.indexOf(EXT_QUOTES, startIndex + 1);
            value = info.substring(startIndex, endIndex).replace(EXT_QUOTES, "").trim();
        }
        return value;
    }
}
