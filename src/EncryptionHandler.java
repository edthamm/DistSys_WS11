import java.security.*;

import javax.crypto.*;

public class EncryptionHandler {

    private Key key;
    private Mac hmac;
    private Cipher enccipher;
    private Cipher deccipher;
    
    
    public EncryptionHandler(Key secret, String Algorithm) throws NoSuchAlgorithmException, InvalidKeyException{
        key = secret;
        hmac= Mac.getInstance(Algorithm);
        hmac.init(key);
    }
    
    public EncryptionHandler(Key secret, String Algorithm, SecureRandom IV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
        if(IV == null){
            enccipher = Cipher.getInstance(Algorithm);
            deccipher = Cipher.getInstance(Algorithm);
            enccipher.init(Cipher.ENCRYPT_MODE, secret);
            deccipher.init(Cipher.DECRYPT_MODE, secret);
        }
        else{
            enccipher = Cipher.getInstance(Algorithm);
            deccipher = Cipher.getInstance(Algorithm);
            enccipher.init(Cipher.ENCRYPT_MODE, secret, IV);
            deccipher.init(Cipher.DECRYPT_MODE, secret, IV);
        }
    }
    
    public String encryptMessage(String msg){
        return null;
    }
    
    public String decryptMessage(String msg){
        return null;
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
