package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;
import com.excellence.basetoolslibrary.utils.FileIOUtils;

import java.io.File;
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
 *     time   : 2019/8/30
 *     desc   : https://github.com/ema987/m3u8parser 正则表达式解析M3U
 *              https://github.com/dholroyd/m3u8parser  正则表达式解析
 * </pre> 
 */
public class M3URexParser {

    private static final String TAG = M3URexParser.class.getSimpleName();

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF";
    private static final String TVG_ID_TAG = "tvg-id";
    private static final String TVG_NAME_TAG = "tvg-name";
    private static final String TVG_LOGO_TAG = "tvg-logo";
    private static final String GROUP_TITLE_TAG = "group-title";

    private static final String EXT_LINE = String.format("(?=%s)", EXT_INF);
    private static final String PREFIX_COMMENT = "#";
    private static final String EMPTY_STRING = "";

    /**
     * duration遇到两种：#EXTINF:-1 和#EXTINF:-1,
     * 一种是带空格，一种是带分号
     */
    private static final String DURATION_REGEXP = EXT_INF + ":(.*?)(?: |,)";
    private static final String TVG_ID_REGEXP = TVG_ID_TAG + "=\"(.*?)\"";
    private static final String TVG_NAME_REGEXP = TVG_NAME_TAG + "=\"(.*?)\"";
    private static final String TVG_LOGO_URL_REGEXP = TVG_LOGO_TAG + "=\"(.*?)\"";
    private static final String GROUP_TITLE_REGEXP = GROUP_TITLE_TAG + "=\"(.*?)\"";
    private static final String TITLE_REGEXP = ",(.*?)\\r?\\n";
    private static final String URL_REGEXP = "\\r?\\n";

    private static final Pattern DURATION_PATTERN = Pattern.compile(DURATION_REGEXP);
    private static final Pattern TVG_ID_PATTERN = Pattern.compile(TVG_ID_REGEXP);
    private static final Pattern TVG_NAME_PATTERN = Pattern.compile(TVG_NAME_REGEXP);
    private static final Pattern TVG_LOGO_URL_PATTERN = Pattern.compile(TVG_LOGO_URL_REGEXP);
    private static final Pattern GROUP_TITLE_PATTERN = Pattern.compile(GROUP_TITLE_REGEXP);
    private static final Pattern TITLE_PATTERN = Pattern.compile(TITLE_REGEXP);
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = new M3UPlayList();
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();
        try {
            String[] linesArray = content.split(EXT_LINE);
            for (String line : linesArray) {
                try {
                    if (line.startsWith(EXT_M3U)) {
                        /**
                         * parse header
                         */
                        header = parseHeader(line);
                    } else if (line.startsWith(EXT_INF)) {
                        M3UItem item = parseItem(line);
                        itemList.add(item);
                    } else if (line.startsWith(PREFIX_COMMENT)) {
                        /**
                         * Do nothing.
                         */
                    } else if (line.equals(EMPTY_STRING)) {
                        /**
                         * Do nothing.
                         */
                    } else {
                        /**
                         * The single line is treated as the stream URL.
                         */
                        M3UItem item = new M3UItem();
                        item.setUrl(line);
                        itemList.add(item);
                    }
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

    private static M3UHeader parseHeader(String line) {
        M3UHeader header = new M3UHeader();
        return header;
    }

    private static M3UItem parseItem(String line) {
        M3UItem item = new M3UItem();
        item.setDuration(getInsideString(DURATION_PATTERN, line));
        item.setId(getInsideString(TVG_ID_PATTERN, line));
        item.setName(getInsideString(TVG_NAME_PATTERN, line));
        item.setLogo(getInsideString(TVG_LOGO_URL_PATTERN, line));
        item.setGroupTitle(getInsideString(GROUP_TITLE_PATTERN, line));
        item.setTitle(getInsideString(TITLE_PATTERN, line));
        item.setUrl(getNextToString(URL_PATTERN, line));
        return item;
    }

    /**
     * Get the string wrapped inside the pattern reg exp
     * 截取分组里匹配的字符
     *
     * @param pattern
     * @param line
     * @return
     */
    private static String getInsideString(final Pattern pattern, final String line) {
        final Matcher matcher = pattern.matcher(line);
        String result = "";
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    /**
     * Get the string next to the pattern reg exp
     * 遍历所有字符后，匹配字符到结尾
     *
     * @param pattern
     * @param line
     * @return
     */
    private static String getNextToString(final Pattern pattern, final String line) {
        final Matcher matcher = pattern.matcher(line);
        String result = "";
        if (matcher.find()) {
            result = line.substring(matcher.end());
        }
        return result;
    }

    public static M3UPlayList parse(InputStream is) {
        if (is == null) {
            return null;
        }
        String content = null;
        try {
            /**
             * Scanner对象将首先跳过输入流开头的所有空白分隔符，然后对输入流中的信息进行检查，直到遇到空白分隔符为止
             * Scanner 将空格当作了一个分隔符，那如何将含有空格的数据输出呢？
             * 这时就需要用Scanner.useDelimiter( )方法，可以将分隔符号修改为"回车"，或者其他字符。
             * useDelimiter默认以空格作为分隔符，\\A正则表达式，从字符串开头进行匹配
             */
            content = new Scanner(is).useDelimiter("\\A").next();
            CloseUtils.closeIOQuietly(is);
        } catch (Exception e) {
            Log.e(TAG, "parse: input stream error :" + e.getMessage());
        }
        return parse(content);
    }

    public static M3UPlayList parse(File file) {
        String content = FileIOUtils.readFile2String(file, null);
        return parse(content);
    }

}
