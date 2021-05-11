package com.example.mydisplayingbitmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;

        if (mMemoryCache == null) {
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMomory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                // 匿名内部クラスとして実装し、その一つの要素に対してそれがどれだけのサイズを利用しているか
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        GridView mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setNumColumns(3);

        // タイル状のGridViewの中に表示するImageViewのリスト
        final List<ImageView> listImageView = new ArrayList<>();
        for (int i = 0; i < 100; i++) {

            ImageView imageView = new ImageView(MainActivity.this);
            // 一つのImageViewの高さと幅を設定
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 320));

            loadBitmap(R.drawable.sample, imageView);
            listImageView.add(imageView);

            // メモリのログ出力
//            Runtime r = Runtime.getRuntime();
//            Log.d("MainActivity", "usedmemory[MB]:" + (int)((r.totalMemory() - r.freeMemory()) / (1024*1024)) );
        }

        BaseAdapter mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return listImageView.size();
            }

            @Override
            public Object getItem(int i) {
                return listImageView.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                return listImageView.get(i);
            }
        };
        mGridView.setAdapter(mAdapter);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    // ImageViewにビットマップ画像を読み込む
    public void loadBitmap(int resId, ImageView imageView) {
//        // AsyncTaskクラスの子クラスのインスタンス生成
//        BitmapWorkTask task = new BitmapWorkTask(imageView, getResources());
//        // 非同期に実行
//        task.execute(resId);

        final String imageKey = String.valueOf(resId);
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        // まずはキャッシュから取得、キャッシュが存在しない場合はとりあえずアイコンを表示後に非同期で実行処理
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
            BitmapWorkTask task = new BitmapWorkTask(imageView, getResources());
            task.execute(resId);
        }
    }

    // 引数は、execute実行時の引数の型、戻り値の型、バックグラウンド処理の結果取得される値の型
    class BitmapWorkTask extends AsyncTask<Integer, Void, Bitmap> {

        /* WeakReference
        * 参照型をひとつだけ格納できるコンテナ.ここではImageViewインスタンスを格納
        * 弱参照.他からの参照がなくなると自動的に不要なメモリとみなされガベージコレクタにより削除される
        * メモリ節約のため
        */
        private final WeakReference<ImageView> imageViewReference;
        private final Resources resources;

        public BitmapWorkTask(ImageView imageView, Resources resources) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.resources = resources;
        }

        // Decode image in background.
        // executeメソッド呼び出し時にバックグラウンドスレッドで実行される処理
        @Override
        protected Bitmap doInBackground(Integer... params) {
//            return decodeSampledBitmapFromResource(resources, params[0], 180, 320);

            // 縮小後、キャッシュに追加する
            Bitmap bitmap = decodeSampledBitmapFromResource(resources, params[0], 180, 320);
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                      int reqWidth, int reqHeight) {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }

        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfwidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfwidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
    }

}