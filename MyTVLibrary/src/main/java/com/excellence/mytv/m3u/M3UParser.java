package com.excellence.mytv.m3u;

import android.util.Log;

import com.excellence.basetoolslibrary.utils.CloseUtils;
import com.excellence.basetoolslibrary.utils.EmptyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * 8K读取，8M解析
     */
    private static final int BUF_LEN = 8 * 1024;
    private static final int BUF_COUNT = 8 * 1024 * 1024;

    private static final String EXT_M3U = "#EXTM3U";
    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_SPACE = " ";
    /**
     * 转义 "
     */
    private static final String EXT_QUOTES = "\"";
    /**
     * 转义 \"，一些节目名中带有"
     */
    private static final String EXT_SLASH_QUOTES = "\\\"";
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
     * 下面三种正则表达式解析特殊的行会有问题，想要使用下面表达式需要额外的处理：
     * 1.带多个逗号的行：从尾部找"，旁边的,
     * #EXTINF:-1 tvg-id="TV 8,5 TR" tvg-name="||TR|| TV 8.5 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/tv8.png" group-title="TURKEY I ULUSAL",||TR|| TV 8.5 HD
     * 2.节目名带"，即\"。由于问题1延伸出来的问题2：找"会找到\"节目名中，导致index不对
     * #EXTINF:-1 tvg-ID="" tvg-name="|FR| El Camino : Un film \"Breaking Bad\" (AUDIO)" tvg-logo="https://image.tmdb.org/t/p/w500/ePXuKdXZuJx8hHMNr2yM4jY2L7Z.jpg" group-title="|FR| CINÉMA",|FR| El Camino : Un film \"Breaking Bad\" (AUDIO)
     *
     * #EXTINF:([^,]+),([^\cJ]+)\cJ#EXTGRP:([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^\s]+)\s([^=]+)=([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     * #EXTINF:([^,]+),([^\cJ]+)\cJ([^\cJ]+)
     */

    /**
     * 每行解析，速度最快
     *
     * @param is
     * @return
     */
    public static M3UPlayList parseLine(InputStream is) {
        M3UPlayList m3uPlayList = new M3UPlayList();
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();

        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);
        try {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith(EXT_INF)) {
                    StringBuilder sb = new StringBuilder(line);
                    while ((line = br.readLine()) != null
                            && !line.startsWith(EXT_INF)) {
                        sb.append('\n').append(line);
                        try {
                            M3UItem m3uItem = M3UParser.parseInfo(sb.toString());
                            itemList.add(m3uItem);
                            break;
                        } catch (Exception e) {
//                            Log.e(TAG, "parse m3u item error");
                        }
                    }
                } else {
//                    if (line.startsWith(EXT_M3U)) {
//                        /**
//                         * parse header
//                         */
//                    }
                    line = br.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CloseUtils.closeIOQuietly(br);

        m3uPlayList.setHeader(header);
        if (m3uPlayList.getItems() == null) {
            m3uPlayList.setItems(itemList);
        } else {
            m3uPlayList.getItems().addAll(itemList);
        }
        return m3uPlayList;
    }

    /**
     * 一次性解析内容
     *
     * @param content
     * @return
     */
    public static M3UPlayList parse(String content) {
        M3UPlayList m3uPlayList = new M3UPlayList();

        parse(m3uPlayList, content);

        return m3uPlayList;
    }

    /**
     * 多次解析内容，比如大文件解析 {@link #parse(File)}
     *
     * @param m3uPlayList
     * @param content
     * @return
     */
    public static void parse(M3UPlayList m3uPlayList, String content) {
        if (m3uPlayList == null) {
            m3uPlayList = new M3UPlayList();
        }
        M3UHeader header = new M3UHeader();
        List<M3UItem> itemList = new ArrayList<>();
        try {
            String[] lines = Pattern.compile(EXT_INF).split(content);
            for (String line : lines) {
                try {
//                    if (line.startsWith(EXT_M3U)) {
//                        /**
//                         * parse header
//                         */
//                    } else
                    if (EmptyUtils.isNotEmpty(line)) {
                        M3UItem item = parseInfo(line);
                        itemList.add(item);
                    }
                } catch (Exception e) {
                    /**
                     * 占时间
                     */
                    // Log.e(TAG, "parse item error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parse error : " + e.getMessage());
        }
        m3uPlayList.setHeader(header);
        if (m3uPlayList.getItems() == null) {
            m3uPlayList.setItems(itemList);
        } else {
            m3uPlayList.getItems().addAll(itemList);
        }
    }

    /**
     * 优化解析速度&内存，但是速度比不上每行解析
     * 分段解析，每次读取8K，读满8M，再解析，保存列表；再循环
     *
     * @param is
     * @return
     */
    public static M3UPlayList parse(InputStream is) {
        M3UPlayList m3uPlayList = new M3UPlayList();

        StringBuffer content = new StringBuffer();
        try {
            int count = 0;
            int len;
            byte[] buffer = new byte[BUF_LEN];
            do {
                len = is.read(buffer, 0, buffer.length);
                if (len == -1) {
                    parse(m3uPlayList, content.toString());
                    break;
                } else {
                    count += len;
                    content.append(new String(buffer, 0, len));
                    if (count >= BUF_COUNT) {
                        len = content.lastIndexOf(EXT_INF);
                        parse(m3uPlayList, content.substring(0, len));
                        /**
                         * 把截掉的字符串当做下一次的起始
                         */
                        content = new StringBuffer(content.substring(len));
                        count = content.length();
                    }
                }
            } while (true);
            CloseUtils.closeIOQuietly(is);
        } catch (Exception e) {
            Log.e(TAG, "parse: input stream error :" + e.getMessage());
        }
        return m3uPlayList;
    }

    /**
     * 读取文件，分段解析，速度慢于{@link #parseLine(File)}
     *
     * @param file
     * @return
     */
    public static M3UPlayList parse(File file) {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (Exception e) {
            Log.e(TAG, "parse file error : " + e.getMessage());
        }
        return parse(is);
    }

    /**
     * 读取文件，按行解析，速度快于{@link #parse(File)}
     *
     * @param file
     * @return
     */
    public static M3UPlayList parseLine(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (Exception e) {
            Log.e(TAG, "parse file error : " + e.getMessage());
        }
        return parseLine(is);
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
    public static M3UItem parseInfo(String line) {
        M3UItem item = new M3UItem();
        /**
         * 取URL，从第一个\n到结束
         */
        int index = line.indexOf(EXT_NEW_LINE);
        String url = line.substring(index + EXT_NEW_LINE.length());
        item.setUrl(url.trim());
        line = line.substring(0, index);

        /**
         * 取,，从最后一个"开始，如果没有"，则从开始位置查，需要排除节目名中的\"
         */
        int extQuotesIndex = line.length();
        do {
            String tempLine = line.substring(0, extQuotesIndex);
            extQuotesIndex = tempLine.lastIndexOf(EXT_QUOTES);
            /**
             * 判断是否遇到了\"，而不是"
             */
            int extSlashQuotesIndex = tempLine.lastIndexOf(EXT_SLASH_QUOTES);
            if (extSlashQuotesIndex == (extQuotesIndex - 1)) {
                extQuotesIndex--;
            } else {
                break;
            }

            if (extQuotesIndex <= 0) {
                break;
            }
        } while (true);
        index = extQuotesIndex;

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
        String lowerCaseInfo = info.toLowerCase();
        String keyLowerCase = key.toLowerCase();
        if (lowerCaseInfo.contains(keyLowerCase)) {
            /**
             * tvg-id="RTL4.nl"
             * 先找tvg-id=为起始位置
             * 再找第二个引号为结束位置
             * 最后清除引号和空格
             *
             * 这里注意转义字符
             */
            int startIndex = lowerCaseInfo.indexOf(keyLowerCase) + key.length() + EXT_EQUAL.length() + 1;
            int endIndex = 0;
            int searchQuotesIndex = startIndex;
            do {
                endIndex = info.indexOf(EXT_QUOTES, searchQuotesIndex);
                /**
                 * 判断是否遇到了\"，而不是"
                 */
                int extSlashQuotesIndex = info.indexOf(EXT_SLASH_QUOTES, searchQuotesIndex);
                if (extSlashQuotesIndex == (endIndex - 1)) {
                    searchQuotesIndex = extSlashQuotesIndex + EXT_SLASH_QUOTES.length();
                } else {
                    break;
                }
                if (searchQuotesIndex >= info.length()) {
                    break;
                }
            } while (true);
            value = info.substring(startIndex, endIndex).trim();
        }
        return value;
    }
}
