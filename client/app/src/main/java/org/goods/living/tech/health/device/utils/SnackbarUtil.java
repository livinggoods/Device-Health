package org.goods.living.tech.health.device.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.goods.living.tech.health.device.R;


public class SnackbarUtil {
    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen for few seconds*****************************/
    public static void showSnakbarTypeOne(View rootView, String mMessage) {
        Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
    }

    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen*****************************/
    public static void showSnakbarTypeTwo(View rootView, String mMessage) {

        Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG)
                .make(rootView, mMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .show();

    }

    /************************************ ShowSnackbar without message, KeepItDisplayedOnScreen, OnClickOfOk restrat the activity*****************************/
    public static void showSnakbarTypeThree(View rootView, final Activity activity) {

        Snackbar
                .make(rootView, "NoInternetConnectivity", Snackbar.LENGTH_INDEFINITE)
                .setAction("TryAgain", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = activity.getIntent();
                        activity.finish();
                        activity.startActivity(intent);
                    }
                })
                .setActionTextColor(Color.CYAN)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                    }
                })
                .show();

    }

    /************************************ ShowSnackbar with message, KeepItDisplayedOnScreen, OnClickOfOk restart the activity*****************************/
    public static void showSnakbarTypeFour(View rootView, final Activity activity, String mMessage) {

        Snackbar
                .make(rootView, mMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction("TryAgain", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = activity.getIntent();
                        activity.finish();
                        activity.startActivity(intent);
                    }
                })
                .setActionTextColor(Color.CYAN)
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                    }
                })
                .show();

    }

    public static void showSnack(Activity activity, String text) {
        Snackbar.make(
                activity.getWindow().getDecorView().getRootView(),//findViewById(R.id.activity_main),
                text,//R.string.permission_denied_explanation,
                Snackbar.LENGTH_SHORT)
                .setAction(R.string.settings, null)
                .show();
    }

    public static void showSnackLong(Activity activity, String text) {
        Snackbar.make(
                activity.getWindow().getDecorView().getRootView(),//findViewById(R.id.activity_main),
                text,//R.string.permission_denied_explanation,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, null)
                .show();
    }

    public static void showSnackWithProgress(Activity activity, String text) {
        Snackbar bar = Snackbar.make(activity.getWindow().getDecorView().getRootView(), "processing...\n " + text, Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(activity);
        contentLay.addView(item, 0);
        bar.show();
    }


}