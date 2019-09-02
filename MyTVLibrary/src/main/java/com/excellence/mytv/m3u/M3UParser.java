package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/8/29
 *     desc   : https://github.com/crazyks/M3UPlayer    字符串解析解析，不完整，根据思路修改
 * </pre>
 */

public class M3UParser {

    private static final String TAG = M3UParser.class.getSimpleName();

    private static final String PREFIX_EXTM3U = "#EXTM3U";
    private static final String PREFIX_EXTINF = "#EXTINF:";
    /**
     * 以分隔符 #EXTINF: 分隔字符串，split但是不删除 #EXTINF: 如：https://cloud.tencent.com/developer/ask/69502
     * 回车作为分隔符 \\r?\\n
     */
    private static final String EXT_LINE = String.format("(?=%s)", PREFIX_EXTINF);
    private static final String PREFIX_COMMENT = "#";
    private static final String EMPTY_STRING = "";
    private static final String EXT_NEW_LINE = "\\r?\\n";

    private static final String ATTR_TYPE = "type";
    private static final String ATTR_DLNA_EXTRAS = "dlna_extras";

    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DURATION = "duration";
    private static final String ATTR_LOGO = "logo";
    private static final String ATTR_URL = "url";
    private static final String ATTR_GROUP_TITLE = "group-title";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_TVG_PREFIX = "tvg-";
    private static final String ATTR_TVG_SUFFIX = "-tvg";

    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = new M3UPlayList();
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();
        try {
            String[] linesArray = content.split(EXT_LINE);
            for (String line : linesArray) {
                try {
                    line = shrink(line);
                    if (line.startsWith(PREFIX_EXTM3U)) {
                        header = parseHead(shrink(line.replaceFirst(PREFIX_EXTM3U, EMPTY_STRING)));
                    } else if (line.startsWith(PREFIX_EXTINF)) {
                        M3UItem item = parseItem(shrink(line.replaceFirst(PREFIX_EXTINF, EMPTY_STRING)));
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

    private static String shrink(String str) {
        return str == null ? null : str.trim();
    }

    private static void putAttr(Map<String, String> map, String key, String value) {
        map.put(key, value);
    }

    private static String getAttr(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null) {
            value = map.get(ATTR_TVG_PREFIX + key);
            if (value == null) {
                value = map.get(key + ATTR_TVG_SUFFIX);
            }
        }
        return value;
    }

    private static M3UHeader parseHead(String line) {
        Map<String, String> attr = parseAttributes(line);
        M3UHeader header = new M3UHeader();
        header.setName(getAttr(attr, ATTR_NAME));
        header.setType(getAttr(attr, ATTR_TYPE));
        header.setDLNAExtras(getAttr(attr, ATTR_DLNA_EXTRAS));
        return header;
    }

    private static M3UItem parseItem(String line) {
        Map<String, String> attr = parseAttributes(line);
        M3UItem item = new M3UItem();
        item.setDuration(getAttr(attr, ATTR_DURATION));
        item.setId(getAttr(attr, ATTR_ID));
        item.setName(getAttr(attr, ATTR_NAME));
        item.setLogo(getAttr(attr, ATTR_LOGO));
        item.setGroupTitle(getAttr(attr, ATTR_GROUP_TITLE));
        item.setTitle(getAttr(attr, ATTR_TITLE));
        item.setUrl(getAttr(attr, ATTR_URL));
        item.setType(getAttr(attr, ATTR_TYPE));
        item.setDLNAExtras(getAttr(attr, ATTR_DLNA_EXTRAS));
        return item;
    }

    private static Map<String, String> parseAttributes(String line) {
        Map<String, String> attr = new HashMap<>();
        if (line == null || line.equals(EMPTY_STRING)) {
            return attr;
        }

        String[] lineItemList = line.split(EXT_NEW_LINE);
        if (lineItemList.length > 0) {
            line = lineItemList[0];
            if (lineItemList.length > 1) {
                attr.put(ATTR_URL, lineItemList[1]);
            }
        }

        Status status = Status.READY;
        String tmp = line;
        StringBuffer connector = new StringBuffer();
        int i = 0;
        char c = tmp.charAt(i);
        if (c == '-' || Character.isDigit(c)) {
            connector.append(c);
            while (++i < tmp.length()) {
                c = tmp.charAt(i);
                if (Character.isDigit(c)) {
                    connector.append(c);
                } else {
                    break;
                }
            }
            putAttr(attr, ATTR_DURATION, connector.toString());
            tmp = shrink(tmp.replaceFirst(connector.toString(), EMPTY_STRING));
            reset(connector);
            i = 0;
        }
        String key = EMPTY_STRING;
        boolean startWithQuota = false;
        while (i < tmp.length()) {
            c = tmp.charAt(i++);
            switch (status) {
                case READY:
                    if (Character.isWhitespace(c)) {
                        // Do nothing
                    } else if (c == ',') {
                        putAttr(attr, ATTR_TITLE, tmp.substring(i));
                        i = tmp.length();
                    } else {
                        connector.append(c);
                        status = Status.READING_KEY;
                    }
                    break;
                case READING_KEY:
                    if (c == '=') {
                        key = shrink(key + connector.toString());
                        reset(connector);
                        status = Status.KEY_READY;
                    } else {
                        connector.append(c);
                    }
                    break;
                case KEY_READY:
                    if (!Character.isWhitespace(c)) {
                        if (c == '"') {
                            startWithQuota = true;
                        } else {
                            connector.append(c);
                        }
                        status = Status.READING_VALUE;
                    }
                    break;
                case READING_VALUE:
                    if (startWithQuota) {
                        connector.append(c);
                        int end = tmp.indexOf("\"", i);
                        end = end == -1 ? tmp.length() : end;
                        connector.append(tmp.substring(i, end));
                        startWithQuota = false;
                        putAttr(attr, key, connector.toString());
                        i = end + 1;
                        reset(connector);
                        key = EMPTY_STRING;
                        status = Status.READY;
                        break;
                    }
                    if (Character.isWhitespace(c)) {
                        if (connector.length() > 0) {
                            putAttr(attr, key, connector.toString());
                            reset(connector);
                        }
                        key = EMPTY_STRING;
                        status = Status.READY;
                    } else {
                        connector.append(c);
                    }
                    break;
                default:
                    break;
            }
        }
        if (!key.equals(EMPTY_STRING) && connector.length() > 0) {
            putAttr(attr, key, connector.toString());
            reset(connector);
        }
        return attr;
    }

    private static void reset(StringBuffer buffer) {
        buffer.delete(0, buffer.length());
    }

    private enum Status {
        READY, READING_KEY, KEY_READY, READING_VALUE,
    }
}
