package com.example.lawrence.fingerprintauth;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by grandilo-lawrence on 12/27/17.
 */

public class AuthenticationHandler extends FingerprintManager.AuthenticationCallback {
    private AuthActivity authActivity;
    private ImageView thumbsImage;
    private TextView response;

    public AuthenticationHandler(AuthActivity authActivity){
        this.authActivity = authActivity;
        thumbsImage = authActivity.findViewById(R.id.iv_thumb);
        response = authActivity.findViewById(R.id.tv_response);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        showToast("Auth Error " + errString.toString());
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        showToast("Auth Help "+helpString.toString());
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        thumbsImage.setImageResource(R.drawable.thumb_auth);
        response.setTextColor(Color.GREEN);
        response.setText(R.string.success_msg);
        Intent loginSuccess = new Intent(authActivity,MainActivity.class);
        authActivity.startActivity(loginSuccess);
        authActivity.finish();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        resetImage();

    }

    public void showToast(String msgDisplay){
        Toast.makeText(authActivity, msgDisplay,Toast.LENGTH_LONG).show();
    }

    public void resetImage(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                thumbsImage.setImageResource(R.drawable.thumb);
                response.setTextColor(Color.BLACK);
                response.setText(R.string.msg);
                handler.postDelayed(this,1000);
            }
        };
        handler.postDelayed(runnable,2500);
        thumbsImage.setImageResource(R.drawable.thumb_error);
        response.setTextColor(Color.RED);
        response.setText(R.string.err_msg);
    }
}
