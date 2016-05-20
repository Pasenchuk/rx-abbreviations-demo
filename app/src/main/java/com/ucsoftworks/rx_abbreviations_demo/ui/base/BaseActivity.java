package com.ucsoftworks.rx_abbreviations_demo.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.otto.Bus;
import com.ucsoftworks.rx_abbreviations_demo.R;
import com.ucsoftworks.rx_abbreviations_demo.app.App;

import javax.inject.Inject;

/**
 * Created by pasencukviktor on 06/02/16
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Inject
    Bus bus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getApp(this).getAppComponent().inject(this);
        Log.d("LifeCycle", this.getClass().getName() + " OnCreate");


    }


    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        Log.d("LifeCycle", this.getClass().getName() + " onDestroy");
        super.onDestroy();
    }

    protected void replaceFragment(Fragment fragment) {

        replaceFragment(fragment, true, null);
    }

    protected void replaceFragment(Fragment fragment, boolean addToBackStack) {

        replaceFragment(fragment, addToBackStack, null);
    }

    protected void replaceFragment(Fragment fragment, boolean addToBackStack, @Nullable String key) {

        FragmentTransaction replaceTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);

        if (addToBackStack)
            replaceTransaction
                    .addToBackStack(key);

        replaceTransaction
                .commit();
    }

    protected void addFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    protected void returnToBackStack(String stackKey, boolean inclusive) {
        //FragmentManager.POP_BACK_STACK_INCLUSIVE (1) - вытолкнуть из стэка и закрыть не только фрагменты выше
        //добавленного по ключу, но и сам фрагмент, добавленный по ключу
        //0 - вытолкнуть и закрыть только те, что выше
        getSupportFragmentManager()
                .popBackStackImmediate(stackKey, inclusive ? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0);

    }

    public Bus getBus() {
        return bus;
    }
}
