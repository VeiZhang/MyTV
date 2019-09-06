package com.excellence.mytv.xmltv;

import android.util.Log;

import com.google.android.media.tv.companionlibrary.xmltv.XmlTvParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/9/6
 *     desc   : 对 {@link com.google.android.media.tv.companionlibrary.xmltv.XmlTvParser}做一些补充
 * </pre> 
 */
public class XmlTVParser {

    private static final String TAG = XmlTVParser.class.getSimpleName();

    public static XmlTvParser.TvListing parse(String content) {
        return parse(new ByteArrayInputStream(content.getBytes()));
    }

    public static XmlTvParser.TvListing parse(InputStream is) {
        try {
            return XmlTvParser.parse(is);
        } catch (Exception e) {
            Log.e(TAG, "parse: input stream error :" + e.getMessage());
        }
        return null;
    }

    public static XmlTvParser.TvListing parse(File file) {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (Exception e) {
            Log.e(TAG, "parse file error : " + e.getMessage());
        }
        return parse(is);
    }

}
