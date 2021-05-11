package com.example.mydisplayingbitmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setNumColumns(3);

        // タイル状のGridViewの中に表示するImageViewのリスト
        final List<ImageView> listImageView = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            // decodeResourceメソッドでビットマップ画像が読み込まれつつ、optionsオブジェクトに
//            // 画像の高さ、幅、タイプが出力される
//            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.sample, options);
//            int imageHeight = options.outHeight;
//            int imageWidth = options.outWidth;
//            String imageType = options.outMimeType;
//            Log.d("MainActivity", "height:" + imageHeight + " width:" + imageWidth + " type:" + imageType);

            // 画像を縮小するメソッド呼び出し
            Bitmap bm = decodeSampledBitmapFromResource(
                    getResources(), R.drawable.sample, 180, 320);

            ImageView imageView = new ImageView(MainActivity.this);
            // 一つのImageViewの高さと幅を設定
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 320));
            imageView.setImageBitmap(bm);
            listImageView.add(imageView);

            Runtime r = Runtime.getRuntime();
            Log.d("MainActivity", "usedmemory[MB]:" + (int)((r.totalMemory() - r.freeMemory()) / (1024*1024)) );
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

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // まずは画像のサイズのみを呼び出し
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