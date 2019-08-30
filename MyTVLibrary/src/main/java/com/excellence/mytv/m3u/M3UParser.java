package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;
import com.excellence.basetoolslibrary.utils.FileIOUtils;

import java.io.File;
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
 *              https://github.com/ema987/m3u8parser    正则表达式解析
 *              https://github.com/dholroyd/m3u8parser  正则表达式解析
 * </pre>
 */

/**
 * This class is used to parse a .m3u file.
 *
 * @author Ke
 */

/**
 * #EXTM3U
 * #EXTINF:-1 tvg-id="Ned1.nl" tvg-name="||NL|| NPO 1 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/npo1hd.png" group-title="NEDERLAND HD",||NL|| NPO 1 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/976
 * #EXTINF:-1 tvg-id="Ned2.nl" tvg-name="||NL|| NPO 2 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/npo2hd.png" group-title="NEDERLAND HD",||NL|| NPO 2 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/974
 * #EXTINF:-1 tvg-id="Ned3.nl" tvg-name="||NL|| NPO 3 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/npo3hd.png" group-title="NEDERLAND HD",||NL|| NPO 3 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/973
 * #EXTINF:-1 tvg-id="RTL4.nl" tvg-name="||NL|| RTL 4 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/rtl4.png" group-title="NEDERLAND HD",||NL|| RTL 4 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/968
 * #EXTINF:-1,||NL|| RTL 5 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/967
 * #EXTINF:-1,||NL|| SBS 6 HD
 * http://line.protv.cc:8000/JNbDvoT2eT/yY7KS0F8t4/961
 */
public class M3UParser {

    private static final String TAG = M3UParser.class.getSimpleName();

    private static final String PREFIX_EXTM3U = "#EXTM3U";
    private static final String PREFIX_EXTINF = "#EXTINF:";
    /**
     * 以分隔符 #EXTINF: 分隔字符串，但是不删除分隔符 https://cloud.tencent.com/developer/ask/69502
     * 回车作为分隔符 \\r?\\n
     */
    private static final String EXT_LINE = String.format("(?=%s)", PREFIX_EXTINF);
    private static final String PREFIX_COMMENT = "#";
    private static final String EMPTY_STRING = "";
    private static final String EXT_NEW_LINE = "\\r?\\n";

    private static final String ATTR_TYPE = "type";
    private static final String ATTR_DLNA_EXTRAS = "dlna_extras";
    private static final String ATTR_PLUGIN = "plugin";

    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DURATION = "duration";
    private static final String ATTR_LOGO = "logo";
    private static final String ATTR_URL = "url";
    private static final String ATTR_GROUP_TITLE = "group-title";
    private static final String ATTR_TVG_PREFIX = "tvg-";
    private static final String ATTR_TVG_SUFFIX = "-tvg";

    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = null;
        try {
            String[] linesArray = content.split(EXT_LINE);

            m3uPlayList = new M3UPlayList();
            M3UHeader header = new M3UHeader();
            List<M3UItem> itemList = new ArrayList<>();

            for (String line : linesArray) {
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
            }
            m3uPlayList.setHeader(header);
            m3uPlayList.setItems(itemList);
        } catch (Exception e) {
            Log.e(TAG, "parse error");
        }
        return m3uPlayList;
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
            Log.e(TAG, "parse: input stream error");
        }
        return parse(content);
    }

    public static M3UPlayList parse(File file) {
        String content = FileIOUtils.readFile2String(file, null);
        return parse(content);
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

    private static M3UHeader parseHead(String words) {
        Map<String, String> attr = parseAttributes(words);
        M3UHeader header = new M3UHeader();
        header.setName(getAttr(attr, ATTR_NAME));
        header.setType(getAttr(attr, ATTR_TYPE));
        header.setDLNAExtras(getAttr(attr, ATTR_DLNA_EXTRAS));
        header.setPlugin(getAttr(attr, ATTR_PLUGIN));
        return header;
    }

    private static M3UItem parseItem(String line) {
        Map<String, String> attr = parseAttributes(line);
        M3UItem item = new M3UItem();
        item.setId(getAttr(attr, ATTR_ID));
        item.setName(getAttr(attr, ATTR_NAME));
        item.setDuration(convert2int(getAttr(attr, ATTR_DURATION)));
        item.setLogo(getAttr(attr, ATTR_LOGO));
        item.setGroupTitle(getAttr(attr, ATTR_GROUP_TITLE));
        item.setUrl(getAttr(attr, ATTR_URL));
        item.setType(getAttr(attr, ATTR_TYPE));
        item.setDLNAExtras(getAttr(attr, ATTR_DLNA_EXTRAS));
        item.setPlugin(getAttr(attr, ATTR_PLUGIN));
        return item;
    }

    private static Map<String, String> parseAttributes(String line) {
        Map<String, String> attr = new HashMap<>();
        if (line == null || line.equals(EMPTY_STRING)) {
            return attr;
        }

        String[] lineArray = line.split(EXT_NEW_LINE);
        if (lineArray.length > 1) {
            line = lineArray[0];
            attr.put(ATTR_URL, lineArray[1]);
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
                        putAttr(attr, ATTR_NAME, tmp.substring(i));
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

    private static int convert2int(String value) {
        int ret = -1;
        try {
            ret = Integer.parseInt(value);
        } catch (Exception e) {
            ret = -1;
        }
        return ret;
    }

    private static void reset(StringBuffer buffer) {
        buffer.delete(0, buffer.length());
    }

    private enum Status {
        READY, READING_KEY, KEY_READY, READING_VALUE,
    }
}
