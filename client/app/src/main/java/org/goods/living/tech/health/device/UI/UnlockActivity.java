/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.goods.living.tech.health.device.UI;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;

import org.goods.living.tech.health.device.AppController;


public class UnlockActivity extends FragmentActivity {


    private static final String TAG = UnlockActivity.class.getSimpleName();


    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    // private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.Theme_Transparent);
        super.onCreate(savedInstanceState);

        AppController.getInstance().getComponent().inject(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }


        minimise();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.DEBUG, TAG, "onResume ");
    }


    public void minimise() {

        //  Intent intent = new Intent(this, MainActivity.class);
        //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //  startActivity(intent);
        // moveTaskToBack(true);
        finish();
    }
}