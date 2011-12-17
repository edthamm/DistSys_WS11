import java.security.*;

import javax.crypto.*;

public class EncryptionHandler {

    private Key key;
    private Mac hmac;
    
    public EncryptionHandler(Key secret, String Algorithm) throws NoSuchAlgorithmException, InvalidKeyException{
        key = secret;
        hmac= Mac.getInstance(Algorithm);
        hmac.init(key);
    }
    
    public String encryptMessage(){
        return null;
    }
    
    public String decryptMessage(){
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
