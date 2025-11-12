package com.hvc.brandlocus.utils;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;

@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {
//    @Value("${encryption.SECRET}")
    private String SECRET = "12345667";
//    @Value("${encryption.MODE}")
    private String MODE = "DES";

    private Key key;
    private  Cipher cipher;

    @PostConstruct
    public void init() throws Exception {
        this.key = new SecretKeySpec(this.SECRET.getBytes(), this.MODE);
        this.cipher = Cipher.getInstance(MODE);
    }
    @Override
    public String convertToDatabaseColumn(String s) {
        if(s == null){
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(s)));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }
}
