package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/8/29
 *     desc   : 提升解析速度，全文使用正则表达式匹配 + 单行字符串解析（单行不能用正则表达式，否则非常慢），速度更快
 *              超大文件可以考虑多线程
 *
 *              规范：https://tools.ietf.org/html/rfc8216
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
    private static final String EXT_NEW_LINE = "\n";
    private static final String EXT_COMMA = ",";
    private static final String EXT_EQUAL = "=";

    private static final String ATTR_ID = "tvg-id";
    private static final String ATTR_NAME = "tvg-name";
    private static final String ATTR_LOGO = "tvg-logo";
    private static final String ATTR_GROUP_TITLE = "group-title";

    /**
     * 匹配行的正则表达式
     * 说明：\cJ -> \n 换行符
     *
     * 最佳正则表达式解析行：#EXTINF:
     *
     * 下面三种正则表达式解析带多个逗号的行会有问题，想要使用下面表达式需要额外的处理：
     * #EXTINF:-1 tvg-id="TV 8,5 TR" tvg-name="||TR|| TV 8.5 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/tv8.png" group-title="TURKEY I ULUSAL",||TR|| TV 8.5 HD
     *
     * #EXTINF:([^,]+),([^\cJ]+)\cJ#EXTGRP:([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^\s]+)\s([^=]+)=([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     */

    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = new M3UPlayList();
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();
        try {
            String[] lines = Pattern.compile(EXT_INF).split(content);
            for (String line : lines) {
                try {
                    if (line.startsWith(EXT_M3U)) {
                        /**
                         * parse header
                         */
                    } else {
                        M3UItem item = parseInfo(line);
                        itemList.add(item);
                    }
                } catch (Exception e) {
                    /**
                     * 占时间
                     */
                    e.printStackTrace();
                    // Log.e(TAG, "parse item error : " + e.getMessage());
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
     * -1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD",||NL|| RTL 4 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/968
     * -1,||NL|| RTL 5 HD
     * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/967
     * ↓
     * ↓
     * ↓
     * -1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD"
     * -1
     *
     * @param line
     */
    private static M3UItem parseInfo(String line) {
        M3UItem item = new M3UItem();
        /**
         * 取URL，从第一个\n到结束
         */
        int index = line.indexOf(EXT_NEW_LINE);
        String url = line.substring(index + EXT_NEW_LINE.length());
        item.setUrl(url.trim());
        line = line.substring(0, index);

        /**
         * 取,，从最后一个"开始，如果没有"，则从开始位置查
         */
        index = line.lastIndexOf(EXT_QUOTES);
        index = line.indexOf(EXT_COMMA, index);
        item.setTitle(line.substring(index + EXT_COMMA.length()).trim());
        line = line.substring(0, index);

        /**
         * ↓
         * ↓
         * ↓
         * -1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD"
         * -1
         */

        index = line.indexOf(EXT_SPACE);
        int length = line.length();
        if (index == -1) {
            index = length;
        }
        String duration = line.substring(0, index);
        item.setDuration(duration.trim());

        /**
         * 有空格则+1，无空格表示没有其他信息
         */
        index++;
        if (index < length) {
            line = line.substring(index).trim();
            item.setId(getAttr(line, ATTR_ID));
            item.setName(getAttr(line, ATTR_NAME));
            item.setLogo(getAttr(line, ATTR_LOGO));
            item.setGroupTitle(getAttr(line, ATTR_GROUP_TITLE));
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
            int startIndex = info.indexOf(key) + key.length() + EXT_EQUAL.length() + EXT_QUOTES.length();
            int endIndex = info.indexOf(EXT_QUOTES, startIndex);
            value = info.substring(startIndex, endIndex).replace(EXT_QUOTES, "").trim();
        }
        return value;
    }
}
