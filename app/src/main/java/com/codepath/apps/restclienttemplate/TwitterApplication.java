package com.codepath.apps.restclienttemplate;

import android.app.Application;

import com.codepath.apps.restclienttemplate.inject.AppComponent;
import com.codepath.apps.restclienttemplate.inject.AppModule;
import com.codepath.apps.restclienttemplate.inject.DaggerAppComponent;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * @author Joseph Gardi
 */
public class TwitterApplication extends Application {
    private AppComponent component;

	@Override
	public void onCreate() {
		super.onCreate();

		FlowManager.init(new FlowConfig.Builder(this).build());
		FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
	}


    public AppComponent getAppComponent() {
        return component;
    }
}
