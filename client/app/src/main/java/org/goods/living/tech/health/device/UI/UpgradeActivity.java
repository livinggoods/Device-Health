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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.goods.living.tech.health.device.AppController;
import org.goods.living.tech.health.device.models.User;


public class UpgradeActivity extends FragmentActivity {


    private static final String TAG = UpgradeActivity.class.getSimpleName();

    public static final String FORCE_UPDATE = "FORCE_UPDATE";


    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    // private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setTheme(R.style.Theme_Transparent);
        super.onCreate(savedInstanceState);

        //  AppController.getInstance().getComponent().inject(this);
        Crashlytics.log("Showing UpgradeActivity");


        Bundle b = getIntent().getExtras();
        boolean force = b.getBoolean(FORCE_UPDATE);

        User user = AppController.getInstance().getUser();

        Answers.getInstance().logCustom(new CustomEvent("App Update")
                .putCustomAttribute("Reason", "force:" + force));


        update(force);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.DEBUG, TAG, "onResume ");
    }

    void update(boolean force) {
        //If the versions are not the same
        final Context context = this;
        //if (force) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("An Update is Available");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Click button action
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                dialog.dismiss();
            }
        });

        if (!force)
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Cancel button action
                    finish();
                }
            });

        builder.setCancelable(false);
        builder.show();
        //  }
    }
}