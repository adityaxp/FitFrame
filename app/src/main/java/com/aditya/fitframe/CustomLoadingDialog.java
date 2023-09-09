package com.aditya.fitframe;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;


public class CustomLoadingDialog {


    Activity activity;
    AlertDialog alertDialog;
    LottieAnimationView lottieAnimationView;
    TextView progressTitleTextView;
    String title;

    public CustomLoadingDialog(Activity currentActivity, String currentTitle) {
        activity = currentActivity;
        title = currentTitle;
    }

    void customLoadingDialogShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_progress_dialog, null));
        builder.setCancelable(false);

        View view = activity.getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);



        alertDialog = builder.create();
        alertDialog.show();
    }

    void  customLoadingDialogDismiss(){
        alertDialog.dismiss();
    }
}
