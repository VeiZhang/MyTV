package com.excellence.mytv.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.excellence.mytv.m3u.M3UParser;
import com.excellence.mytv.m3u.M3UPlayList;

import java.io.File;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_SELECT_FILE = 1024;

    private Button mPathBtn = null;
    private Button mParseBtn = null;
    private Disposable mDisposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPathBtn = (Button) findViewById(R.id.file_btn);
        mParseBtn = (Button) findViewById(R.id.json_btn);
        mParseBtn.requestFocus();

        mPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //选择图片
                //intent.setType("image/*");
                //选择音频
                //intent.setType("audio/*");
                //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType("video/*");
                //同时选择视频和图片
                //intent.setType("video/*;image/*");
                //无类型限制
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            }
        });
        mParseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String path = mPathBtn.getText().toString();
                    if (TextUtils.isEmpty(path)) {
                        Log.i(TAG, "parse assets");
                        mPathBtn.setHint("未选择路径，默认解析:assets/tv_channels_JNbDvoT2eT_plus.m3u");
                        jsonFile(getAssets().open("tv_channels_JNbDvoT2eT_plus.m3u"));
                    } else {
                        Log.i(TAG, "parse file : " + path);
                        jsonFile(new File(path));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 选择文件返回
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_FILE:
                    Uri uri = data.getData();
                    mPathBtn.setText(FileSelectUtil.parseFilePath(this, uri));
                    mParseBtn.performClick();
                    break;

                default:
                    break;
            }
        }
    }

    private void jsonFile(final Object obj) {
        if (mDisposable != null
                && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            return;
        }
        Toast.makeText(MainActivity.this, "开始解析", Toast.LENGTH_LONG).show();
        final long start = System.currentTimeMillis();
        mDisposable = Observable.just(start).subscribeOn(Schedulers.io()).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.i(TAG, "jsonFile start");
                M3UPlayList m3uPlayList = null;
                if (obj instanceof File) {
                    m3uPlayList = M3UParser.parse((File) obj);
                } else if (obj instanceof InputStream) {
                    m3uPlayList = M3UParser.parse((InputStream) obj);
                }
                Log.i(TAG, "jsonFile header : " + m3uPlayList.getHeader().toString());
                Log.i(TAG, "jsonFile item size : " + m3uPlayList.getItems().size());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                String msg = String.format("jsonFile end: %s s", (System.currentTimeMillis() - start) / 1000);
                Log.i(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(MainActivity.this, "解析异常 : " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }
        });
    }
}
