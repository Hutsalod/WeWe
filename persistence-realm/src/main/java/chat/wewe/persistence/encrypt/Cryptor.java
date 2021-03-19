package chat.wewe.persistence.encrypt;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.Cipher;

public class Cryptor extends AppCompatActivity {



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


            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(clearText.getBytes());
            encryptedBase64 = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedBase64.replaceAll("(\\r|\\n)", "");
    }

    public static String token(){
        return ctx.getSharedPreferences("SIP", MODE_PRIVATE).getString("CHAT_PRIVAT","");
    }

    public static void  setSendEncrypt(int setEncrypt){
        ctx.getSharedPreferences("SIP", MODE_PRIVATE).edit().putInt("setEncrypt", setEncrypt).commit();
    }

    public static Integer getSendEncrypt(){
        return ctx.getSharedPreferences("SIP", MODE_PRIVATE).getInt("setEncrypt",0);
    }

    public static void  setEncrypt(String rid, String device){
        ctx.getSharedPreferences("SIP", MODE_PRIVATE).edit().putString(rid, device).commit();
    }

    public static String getEncrypt(String rid){
        return ctx.getSharedPreferences("SIP", MODE_PRIVATE).getString(rid,"");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public String decrypt(String encryptedBase64, String privateKey) {
        EncryptionManager encService = new EncryptionManager(ctx.getApplicationContext());
        encService.createMasterKey();
        String myPublicKeyString = new String(Base64.encode(privateKey.trim().getBytes(), Base64.DEFAULT));
        Key othersPublicKey = encService.getOtherPublicKey(myPublicKeyString);
        encService.decrypt(encryptedBase64,othersPublicKey);
            return null;
    }


    public String decryptRSAToString(String encryptedBase64, String privateKey) {
        String decryptedString = "[Crypted Message]";

        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePrivate(keySpec);


            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
