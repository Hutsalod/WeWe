package chat.wewe.persistence.encrypt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;


import static android.content.Context.MODE_PRIVATE;

public class Cryptor extends AppCompatActivity {


    public static boolean privatChat = false;
    public static Context ctx;

    public static KeyPair getKeyPair() {
        KeyPair kp = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            kp = kpg.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return kp;
    }

    public static String encryptRSAToString(String clearText, String publicKey) {
        String encryptedBase64 = "";
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey.getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePublic(keySpec);


            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(clearText.getBytes());
            encryptedBase64 = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedBase64.replaceAll("(\\r|\\n)", "");
    }

    public String token(){
        return ctx.getSharedPreferences("SIP", MODE_PRIVATE).getString("CHAT_PRIVAT","");
    }

    public String decryptRSAToString(String encryptedBase64, String privateKey) {
        String decryptedString = "";

        Log.d("CHAT_PUBLIC","Test ");
        Log.d("CHAT_PUBLIC","Test key"+token());
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePrivate(keySpec);

            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.decode(encryptedBase64.getBytes("UTF-8"), Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

                decryptedString = new String(decryptedBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptedString;
    }



}
