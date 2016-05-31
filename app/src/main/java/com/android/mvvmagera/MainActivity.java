package com.android.mvvmagera;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.android.mvvmagera.databinding.ActivityMainBinding;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;

public class MainActivity extends AppCompatActivity implements MainActivityViewModel.UpdateImageListener {

    private static final String BACKGROUND_BASE_URL =
            "http://www.gravatar.com/avatar/4df6f4fe5976df17deeea19443d4429d?s=";

    private ActivityMainBinding mDataBinding;

    private Repository<Result<Bitmap>> mDownloadRepository;
    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mViewModel = new MainActivityViewModel(this);
        mDataBinding.setViewModel(mViewModel);

        mDownloadRepository = mViewModel.downloadImageByDisplayMetrics(BACKGROUND_BASE_URL, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start listening to the repository, triggering the flow
        if (mDownloadRepository != null && mViewModel != null) {
            mDownloadRepository.addUpdatable(mViewModel);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening to the repository, deactivating it
        if (mDownloadRepository != null && mViewModel != null) {
            mDownloadRepository.removeUpdatable(mViewModel);
        }
    }

    @Override
    public void onUpdateImage(@NonNull Bitmap bitmap) {
        mDataBinding.imageView.setImageBitmap(bitmap);
    }
}
