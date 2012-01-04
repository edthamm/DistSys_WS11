import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class EncryptionHandler {

    private Key key;
    private Mac hmac;
    private Cipher enccipher;
    private Cipher deccipher;
    
    //Initialize a msg verifyer
    public EncryptionHandler(Key secret, String Algorithm) throws NoSuchAlgorithmException, InvalidKeyException{
        key = secret;
        hmac= Mac.getInstance(Algorithm);
        hmac.init(key);
    }
    //initialize a cipher with an IV (i.e. AES in our case9
    public EncryptionHandler(Key secret, String Algorithm, byte[] IV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
            enccipher = Cipher.getInstance(Algorithm);
            deccipher = Cipher.getInstance(Algorithm);
            IvParameterSpec ivp = new IvParameterSpec(IV);
            enccipher.init(Cipher.ENCRYPT_MODE, secret, ivp);
            deccipher.init(Cipher.DECRYPT_MODE, secret, ivp);
    }
    //init a chipher w/o iv but with 2 keys RSA-PSK in our case
    public EncryptionHandler(Key enc, Key dec, String Algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
        enccipher = Cipher.getInstance(Algorithm);
        deccipher = Cipher.getInstance(Algorithm);
        enccipher.init(Cipher.ENCRYPT_MODE, enc);
        deccipher.init(Cipher.DECRYPT_MODE, dec);
    }
    
    public String encryptMessage(String msg) throws IllegalBlockSizeException, BadPaddingException{
        String b64 = Base64.encode(msg.getBytes());
        byte[] cipher = enccipher.doFinal(b64.getBytes());
        String cipherb64 = Base64.encode(cipher);
        return cipherb64;
    }
    
    public String decryptMessage(String msg) throws Base64DecodingException, IllegalBlockSizeException, BadPaddingException{
        byte[] cipher = Base64.decode(msg);
        byte[] b64 = deccipher.doFinal(cipher);
        String clear = new String(Base64.decode(b64));
        return clear;
    }
    
    
    
    public byte[] generateIntegrityCheck(byte[] msg){
        hmac.update(msg);
        byte[] hash = hmac.doFinal();
        hmac.reset();
        return hash;
    }
    
    public boolean checkIntegrity(String message, byte[] rcvhash){
        byte[] msg = message.getBytes();
        hmac.update(msg);
        byte[] msghash = hmac.doFinal();
        hmac.reset();
        return MessageDigest.isEqual(rcvhash, msghash);
    }
}
