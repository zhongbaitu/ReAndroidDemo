package cn.zmn.reandroiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends Activity {
    private static final String TAG = "MyActivity";
    private Subscription _subscription;
    EditText editName;
    Func1<Object, Void> signalizer;
    Button buttonOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = (EditText)findViewById(R.id.editName);
        buttonOk = (Button)findViewById(R.id.buttonOk);

        // OnTextChangeEvent や OnClickEvent をただの Void シグナルに変換
        signalizer  = new Func1<Object, Void>() {
            @Override
            public Void call(Object onClickEvent) {
                return null;
            }
        };

        test();
    }

    private void test(){
        // 文字入力イベントのストリームと…
        _subscription = WidgetObservable.text(editName).map(signalizer)
                // ボタン押されたのストリームを合体
                .mergeWith(ViewObservable.clicks(buttonOk).map(signalizer))
                        // 3秒間なんもなかったらエラーにする
                .timeout(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void dummy) {
                        // 何かアクションがあったらこっち
                        Log.d(TAG, "文字が入力されたか、ボタンが押されたよ");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        // 3秒間何もなかったらこっち
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "３秒間何も操作がありませんでした", Toast.LENGTH_SHORT)
                                        .show();
                                test();
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        // イベント系は無限ストリームだから開放してやらないとリークするはず
        _subscription.unsubscribe();
        super.onDestroy();
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
