package com.excellence.mytv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.excellence.mytv.m3u.M3UParser;
import com.excellence.mytv.m3u.M3UPlayList;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText mEditText = null;
    private Button mButton = null;
    private Disposable mDisposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.file_et);
        mButton = (Button) findViewById(R.id.json_btn);
        mButton.requestFocus();
        mEditText.setText("/storage/A828039328036024/m3u/tv_channels_JNbDvoT2eT_plus_2.m3u");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonFile(new File(mEditText.getText().toString()));
            }
        });
    }

    private void jsonFile(final File file) {
        if (mDisposable != null
                && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            return;
        }
        final long start = System.currentTimeMillis();
        mDisposable = Observable.just(start).subscribeOn(Schedulers.io()).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.i(TAG, "jsonFile start");
                M3UPlayList m3uPlayList = M3UParser.parse(file);
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
        });
    }
}
