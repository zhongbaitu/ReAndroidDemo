package cn.zmn.reandroiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnCheckedChangeEvent;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;

public class MainActivity extends Activity implements TextWatcher{
    private static final String TAG = "MainActivity";
    private Subscription _subscription;
    EditText mEditText;
    EditText mEditText2;
    Button okBtn;
    CheckBox checkButton;
    boolean needCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText)findViewById(R.id.editText1);
        mEditText2 = (EditText)findViewById(R.id.editText2);
        okBtn = (Button)findViewById(R.id.buttonOk);
        checkButton = (CheckBox)findViewById(R.id.checkBox);

        test3();
    }

    private void test3(){
        checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                needCheck = b;
                if(needCheck){
                    if(mEditText.getText().toString().equals("") || mEditText2.getText().toString().equals("")){
                        okBtn.setEnabled(false);
                    }else{
                        okBtn.setEnabled(true);
                    }
                }else{
                    okBtn.setEnabled(true);
                }
            }
        });

        mEditText.addTextChangedListener(this);
        mEditText2.addTextChangedListener(this);

//        mEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                if(needCheck){
//                    if(charSequence.toString().equals("")){
//                        okBtn.setEnabled(false);
//                    }else{
//                        okBtn.setEnabled(true);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//
//        mEditText2.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                if(needCheck){
//                    if(charSequence.toString().equals("")){
//                        okBtn.setEnabled(false);
//                    }else{
//                        okBtn.setEnabled(true);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, mEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void test2(){
        Observable<Boolean> needCheckObservable = WidgetObservable.input(checkButton, true).map(new Func1<OnCheckedChangeEvent, Boolean>() {
            @Override
            public Boolean call(OnCheckedChangeEvent onCheckedChangeEvent) {
                return onCheckedChangeEvent.value();
            }
        });

        Observable<OnTextChangeEvent> editTextObservable = WidgetObservable.text(mEditText, true);
        Observable<OnTextChangeEvent> editTextObservable2 = WidgetObservable.text(mEditText2, true);

        Observable.combineLatest(needCheckObservable, editTextObservable, editTextObservable2, new Func3<Boolean, OnTextChangeEvent, OnTextChangeEvent, Boolean>() {
            @Override
            public Boolean call(Boolean aBoolean, OnTextChangeEvent onTextChangeEvent, OnTextChangeEvent onTextChangeEvent2) {
                return aBoolean ? !TextUtils.isEmpty(onTextChangeEvent.text()) && !TextUtils.isEmpty(onTextChangeEvent2.text()) : true;
            }
        }).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                okBtn.setEnabled(aBoolean);
            }
        });

        ViewObservable.clicks(okBtn)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<OnClickEvent>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(OnClickEvent onClickEvent) {
                Toast.makeText(MainActivity.this, mEditText.getText().toString() + " , " + mEditText2.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void test(){
        Func1<Object, Void> signalizer  = new Func1<Object, Void>() {
            @Override
            public Void call(Object onClickEvent) {
                return null;
            }
        };
         WidgetObservable.text(mEditText).map(signalizer)
                .mergeWith(ViewObservable.clicks(okBtn).map(signalizer))
                .timeout(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void dummy) {
                        Log.d(TAG, "文字が入力されたか、ボタンが押されたよ");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "３秒間何も操作がありませんでした", Toast.LENGTH_SHORT)
                                        .show();
//                                test();
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
//        _subscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if(needCheck){
            if(charSequence.toString().equals("")){
                okBtn.setEnabled(false);
            }else{
                okBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}


//public class MainActivity extends ActionBarActivity implements Observer<Bitmap> {
//
//    private ImageView mImageView;
//    private Subscription subscription;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mImageView = new ImageView(this);
//        setContentView(mImageView);
//        mImageView.setImageResource(R.drawable.ic_launcher);
//
//        Observable.create(new Observable.OnSubscribe<Integer>() {
//            @Override
//            public void call(final Subscriber<? super Integer> subscriber) {
//                mImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        subscriber.onNext(1);
//                    }
//                });
//            }
//        });
//        Observable<OnClickEvent> mObservable = bindActivity(this, ViewObservable.clicks(mImageView));
//        mObservable.buffer(mObservable.throttleLast(3, TimeUnit.SECONDS))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<List<OnClickEvent>>() {
//                    @Override
//                    public void call(List<OnClickEvent> onClickEvents) {
//                        if(onClickEvents.size() == 1){
//                            Toast.makeText(MainActivity.this, "点击多一下退出" , Toast.LENGTH_SHORT).show();
//                        }else if (onClickEvents.size() >= 2) {
//                            finish();
//                        }
//                    }
//                });
//
//        String urls[] = {"http://g.hiphotos.baidu.com/image/pic/item/d62a6059252dd42a0e621eaa013b5bb5c9eab843.jpg",
//                "http://b.hiphotos.baidu.com/image/pic/item/908fa0ec08fa513d47ca97dc3f6d55fbb2fbd954.jpg",
//                "http://d.hiphotos.baidu.com/image/pic/item/50da81cb39dbb6fdc1c3963e0b24ab18972b3709.jpg"};
//        test(urls);
//
//    }
//
//    private void test(String[] urls){
//        subscription = Observable.from(urls).flatMap(new Func1<String, Observable<Bitmap>>() {
//            @Override
//            public Observable<Bitmap> call(String s) {
//                return downImage(s);
//            }
//        })
//                .filter(new Func1<Bitmap, Boolean>() {
//                    @Override
//                    public Boolean call(Bitmap bitmap) {
//                        return bitmap != null;
//                    }
//                })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this);
//    }
//
//    private Observable<Bitmap> downImage(final String imageUrl){
//        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
//            @Override
//            public void call(Subscriber<? super Bitmap> subscriber) {
//                try{
//                    URL url = new URL(imageUrl);
//                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
//                    con.setDoInput(true);
//                    con.connect();
//                    int responseCode = con.getResponseCode();
//                    if(responseCode == 200){
//                        InputStream inputStream = con.getInputStream();
//                        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
//                        inputStream.close();
//                        subscriber.onNext(bmp);
//                        subscriber.onCompleted();
//                    }
//                    else{
//                        System.out.println(""+responseCode);
//                    }
//                }catch (IOException e){
//                    System.out.println(""+e.toString());
//                    subscriber.onError(e);
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        subscription.unsubscribe();
//    }
//
//    @Override
//    public void onCompleted() {
//        Toast.makeText(this, "图片加载成功", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onError(Throwable e) {
//        Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
//        mImageView.setImageResource(R.drawable.ic_launcher);
//    }
//
//    @Override
//    public void onNext(Bitmap bitmap) {
//        mImageView.setImageBitmap(bitmap);
//    }
//}
