package com.example.mydisplayingbitmaps;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

// LruCacheのフィールドを保持するためだけのFragment
// 画面回転時などActivityが再構成されても、Fragmentを保持し続けLruCacheが再作成されないようにするため
public class RetainFragment extends Fragment {
    private static final String TAG = "RetainFragment";
    public LruCache<String, Bitmap> mRetainedCache;

    public RetainFragment() {}

    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new RetainFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 保持されるようにする
        setRetainInstance(true);
    }
}
