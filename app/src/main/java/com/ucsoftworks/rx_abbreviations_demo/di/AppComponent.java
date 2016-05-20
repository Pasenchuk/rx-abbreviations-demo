package com.ucsoftworks.rx_abbreviations_demo.di;
import com.ucsoftworks.rx_abbreviations_demo.ui.base.BaseActivity;
import com.ucsoftworks.rx_abbreviations_demo.ui.base.BaseFragment;
import com.ucsoftworks.rx_abbreviations_demo.ui.main_screen.MainFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by pasencukviktor on 10/02/16
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(BaseActivity activity);

    void inject(BaseFragment fragment);

    void inject(MainFragment fragment);

}
