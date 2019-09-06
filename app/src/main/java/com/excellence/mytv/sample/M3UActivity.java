package com.excellence.mytv.sample;

import android.util.Log;

import com.excellence.mytv.m3u.M3UParser;
import com.excellence.mytv.m3u.M3UPlayList;

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
public class M3UActivity extends BaseActivity {

    private static final String TAG = M3UActivity.class.getSimpleName();

    @Override
    void processParse(Object obj) {
        M3UPlayList m3uPlayList = null;
        if (obj instanceof File) {
            m3uPlayList = M3UParser.parse((File) obj);
        } else if (obj instanceof InputStream) {
            m3uPlayList = M3UParser.parse((InputStream) obj);
        }
        Log.i(TAG, "json header : " + m3uPlayList.getHeader().toString());
        Log.i(TAG, "json item size : " + m3uPlayList.getItems().size());
    }

    @Override
    String getAssetsPath() {
        return "tv_channels_plus.m3u";
    }
}
