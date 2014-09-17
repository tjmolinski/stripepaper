package com.tjm.stripepaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class MyPreferencesActivity extends PreferenceActivity {

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    if(MyWallpaperService.INSTANCE != null) {
                        MyWallpaperService.INSTANCE.onVisibilityChanged(true);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(spChanged);
    }
}