/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.crypt.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Hex;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.crypt.domain.EncryptionKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author manoj
 */
@Component
public class RSAEncryptionUtils {
    private final static Logger LOG = LoggerFactory.getLogger(RSAEncryptionUtils.class);
    private final Random random = new SecureRandom();

    public EncryptionKeyPair generateKeys(){
        KeyPairGenerator keyPairGenerator;
        EncryptionKeyPair encryptionKeys = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            byte[] publicKeyAsBytes = publicKey.getEncoded();
            byte[] privateKeyAsBytes = privateKey.getEncoded();

            encryptionKeys = new EncryptionKeyPair(privateKeyAsBytes, publicKeyAsBytes, DateUtils.getLocalDateTimeOfTenant(), generateVersionString());

        } catch (NoSuchAlgorithmException e) {
           LOG.error("Error occurred", e);
           throw new GeneralPlatformDomainRuleException("error.msg.error.in.generating.keys", "Error in generating keys");
        }
        return encryptionKeys;
    }

    public String decryptUsingRSA(String encryptedText, final byte[] privateKeyAsBytes, boolean isBase64Encoded) {
        String decryptedString = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyAsBytes));
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, privateKey);
            if(isBase64Encoded) {
                encryptedText = Hex.encodeHexString(encryptedText.getBytes( UTF_8));
                byte[] decryptByteFromUi = decrypt.doFinal(DatatypeConverter.parseHexBinary(encryptedText));
                decryptedString =  new String(decryptByteFromUi,  UTF_8);
            } else {
                byte[] decryptByteFromUi = decrypt.doFinal(encryptedText.getBytes( UTF_8));
                decryptedString =  new String(decryptByteFromUi, UTF_8);
            }
        }catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Error occurred", e);
            throw new GeneralPlatformDomainRuleException("error.msg.error.in.decryption.keys", "Error in decryption");
        }
        return decryptedString;
    }





    private String generateVersionString() {
        final char[] buf = new char[15];
        final String alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" ;
        final char[] symbols = alphanum.toCharArray();
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
        }
        return  new String(buf);
    }
}