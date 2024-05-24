package fiveavian.proxvc.util;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class BufferAES {
    public static void encrypt(Key key, ByteBuffer source, ByteBuffer destination)
            throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipher.doFinal(source, destination);
    }

    public static void decrypt(Key key, ByteBuffer source, ByteBuffer destination)
            throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        cipher.doFinal(source, destination);
    }
}
