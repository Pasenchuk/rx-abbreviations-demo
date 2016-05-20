package com.ucsoftworks.rx_abbreviations_demo.di;

import android.support.annotation.NonNull;

import com.squareup.otto.Bus;
import com.ucsoftworks.rx_abbreviations_demo.app.App;
import com.ucsoftworks.rx_abbreviations_demo.network.AbbreviationsApi;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pasencukviktor on 10/02/16
 */

@Module
public class AppModule {

    private static final String BASE_URL = "http://nactem.ac.uk/software/acromine/";
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Bus provideBus() {
        return new Bus();
    }


    @Provides
    @Singleton
    public AbbreviationsApi provideApi() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(getInterceptor())
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build()
                .create(AbbreviationsApi.class);
    }

    @NonNull
    private Interceptor getInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build();
                return chain.proceed(request);
            }
        };
    }

}
