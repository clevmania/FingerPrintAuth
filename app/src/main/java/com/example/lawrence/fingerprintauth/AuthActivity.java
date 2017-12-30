package com.example.lawrence.fingerprintauth;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AuthActivity extends AppCompatActivity {
    private String keyName = "clev_key";
    private String TAG = AuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if(!fingerprintManager.isHardwareDetected()){
            Log.e(TAG,"Hardware not detected");
            return;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG,"Fingerprint Permission Denied");
        }

        if(!keyguardManager.isKeyguardSecure()){
            Log.e(TAG, "Keyguard isn't enabled");
            return;
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            Log.e("KeyStore", e.getMessage());
            return;
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        } catch (Exception e) {
            Log.e("keyGenerator", e.getMessage());
            return;
        }

        try {
            keyStore.load(null);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(keyName,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build());

            keyGenerator.generateKey();
        } catch (Exception e) {
            Log.e("Generating keys", e.getMessage());
            return;
        }

        Cipher cipher;
        try{
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
            + "/" + KeyProperties.BLOCK_MODE_CBC
            + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        }catch (Exception e){
            Log.e("Cipher",e.getMessage());
            return;
        }

        try{
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE,key);
        }catch (Exception e){
            Log.e("Secret Key", e.getMessage());
        }

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject,cancellationSignal,0,new AuthenticationHandler(this),null);

    }
}
