package com.excellence.mytv.sample;

import android.util.Log;

import com.excellence.mytv.xmltv.XmlTVParser;
import com.google.android.media.tv.companionlibrary.xmltv.XmlTvParser;

import java.io.File;
import java.io.InputStream;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/9/6
 *     desc   :
 * </pre> 
 */
public class XmlTvActivity extends BaseActivity {

    private static final String TAG = XmlTvActivity.class.getSimpleName();

    @Override
    void processParse(Object obj) {
        XmlTvParser.TvListing tvListing = null;
        if (obj instanceof File) {
            tvListing = XmlTVParser.parse((File) obj);
        } else if (obj instanceof InputStream) {
            tvListing = XmlTVParser.parse((InputStream) obj);
        }
        Log.i(TAG, "json channel : " + tvListing.getChannels().size());
        Log.i(TAG, "json program : " + tvListing.getAllPrograms().size());
    }

    @Override
    String getAssetsPath() {
        return "xmltv.xml";
    }
}
