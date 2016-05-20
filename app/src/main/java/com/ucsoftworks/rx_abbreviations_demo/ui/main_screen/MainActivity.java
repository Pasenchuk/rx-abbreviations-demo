package com.ucsoftworks.rx_abbreviations_demo.ui.main_screen;

import android.os.Bundle;

import com.ucsoftworks.rx_abbreviations_demo.R;
import com.ucsoftworks.rx_abbreviations_demo.ui.base.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            addFragment(new MainFragment());
        }
    }
}
