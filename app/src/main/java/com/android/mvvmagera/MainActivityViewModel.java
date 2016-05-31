package com.android.mvvmagera;


import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.google.android.agera.Function;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repository;
import com.google.android.agera.RepositoryConfig;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import com.google.android.agera.net.HttpRequest;
import com.google.android.agera.net.HttpResponse;

import java.util.concurrent.ExecutorService;

import static android.graphics.BitmapFactory.decodeByteArray;
import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.Result.absentIfNull;
import static com.google.android.agera.net.HttpFunctions.httpFunction;
import static com.google.android.agera.net.HttpRequests.httpGetRequest;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class MainActivityViewModel extends BaseObservable implements Receiver<Bitmap>, Updatable {

    private static final ExecutorService NETWORK_EXECUTOR = newSingleThreadExecutor();
    private static final ExecutorService DECODE_EXECUTOR = newSingleThreadExecutor();

    private boolean mIsLoading = false;

    private Repository<Result<Bitmap>> mBackground;
    private UpdateImageListener mListener;

    public MainActivityViewModel(@NonNull UpdateImageListener listener) {
        mListener = listener;
    }

    @Bindable
    public Boolean getIsLoading() {
        return mIsLoading;
    }

    private void setLoading(boolean isLoading) {
        if (mIsLoading != isLoading) {
            mIsLoading = isLoading;
            notifyPropertyChanged(com.android.mvvmagera.BR.isLoading);
        }
    }

    public Repository<Result<Bitmap>> downloadImageByDisplayMetrics(String url, DisplayMetrics displayMetrics) {

        int size = Math.max(displayMetrics.heightPixels,
                displayMetrics.widthPixels);
        String downloadUrlByDisplaySize = url + size;

        // Create a repository containing the result of a bitmap request. Initially
        // absent, but configured to fetch the bitmap over the network based on
        // display size.
        mBackground = repositoryWithInitialValue(Result.<Bitmap>absent())
                .observe() // Optionally refresh the bitmap on events. In this case never
                .onUpdatesPerLoop() // Refresh per Looper thread loop. In this case never
                .getFrom(getHttpRequestSupplier(downloadUrlByDisplaySize)) // Supply an HttpRequest based on the display size
                .goTo(NETWORK_EXECUTOR) // Change execution to the network executor
                .attemptTransform(httpFunction())
                .orSkip() // Make the actual http request, skip on failure
                .goTo(DECODE_EXECUTOR) // Change execution to the decode executor
                .thenTransform(getTransformHttpResponse()) // Decode the response to the result of a     bitmap, absent on failure
                .onDeactivation(RepositoryConfig.SEND_INTERRUPT) // Interrupt thread on deactivation
                .compile(); // Create the repository

        return mBackground;
    }

    private Supplier<HttpRequest> getHttpRequestSupplier(final String url) {
        return new Supplier<HttpRequest>() {
            @NonNull
            @Override
            public HttpRequest get() {
                setLoading(true);
                return httpGetRequest(url).compile();
            }
        };
    }

    private Function<HttpResponse, Result<Bitmap>> getTransformHttpResponse() {
        return new Function<HttpResponse, Result<Bitmap>>() {
            @NonNull
            @Override
            public Result<Bitmap> apply(@NonNull HttpResponse response) {
                byte[] body = response.getBody();
                return absentIfNull(decodeByteArray(body, 0, body.length));
            }
        };
    }

    @Override
    public void update() {
        // Called as the repository is updated
        // If containing a valid bitmap, send to accept below
        mBackground.get().ifSucceededSendTo(this);
    }

    @Override
    public void accept(@NonNull Bitmap background) {

        setLoading(false);
        // Set the background bitmap to the background view
        mListener.onUpdateImage(background);
    }

    public interface UpdateImageListener {
        void onUpdateImage(@NonNull Bitmap bitmap);
    }
}
