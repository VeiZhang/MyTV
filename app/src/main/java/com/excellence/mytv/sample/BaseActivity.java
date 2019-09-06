package com.excellence.mytv.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/9/6
 *     desc   :
 * </pre> 
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int REQUEST_SELECT_FILE = 1024;

    private Button mPathBtn = null;
    private Button mParseBtn = null;
    private Disposable mDisposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mPathBtn = findViewById(R.id.file_btn);
        mParseBtn = findViewById(R.id.json_btn);
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
                        mPathBtn.setHint("未选择路径，默认解析:assets/" + getAssetsPath());
                        jsonFile(getAssets().open(getAssetsPath()));
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
        Toast.makeText(BaseActivity.this, "开始解析", Toast.LENGTH_LONG).show();
        final long start = System.currentTimeMillis();
        mDisposable = Observable.just(start).subscribeOn(Schedulers.io()).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.i(TAG, "jsonFile start");
                processParse(obj);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                String msg = String.format("jsonFile end: %s s", (System.currentTimeMillis() - start) / 1000);
                Log.i(TAG, msg);
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(BaseActivity.this, "解析异常 : " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }
        });
    }

    /**
     * 解析过程
     */
    abstract void processParse(Object obj);

    abstract String getAssetsPath();
}
