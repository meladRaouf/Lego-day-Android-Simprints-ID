package com.simprints.id.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

public class LaunchActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }
}
