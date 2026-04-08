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
package org.apache.fineract.infrastructure.security.utils;

import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class EncryptionUtil {

    private static SecureRandom random = new SecureRandom();

    private EncryptionUtil() {}

    public static String encryptToBase64(@NotNull String cipherType, @NotNull String masterPassword, @NotNull String data) {
        try {
            Cipher cipher = Cipher.getInstance(cipherType);
            byte[] salt = new byte[16];
            byte[] ivBytes = new byte[16];
            random.nextBytes(salt);
            random.nextBytes(ivBytes);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            SecretKeySpec keySpec = generateKeySpec(masterPassword, salt);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encValue = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] finalCiphertext = new byte[encValue.length + 2 * 16];

            System.arraycopy(ivBytes, 0, finalCiphertext, 0, 16);
            System.arraycopy(salt, 0, finalCiphertext, 16, 16);
            System.arraycopy(encValue, 0, finalCiphertext, 32, encValue.length);
            return Base64.getEncoder().encodeToString(finalCiphertext);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to encrypt data. Please check if the master password and algorithm is correct.", e);
        }
    }

    public static String decryptFromBase64(@NotNull String cipherType, @NotNull String masterPassword, @NotNull String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(cipherType);
            byte[] rawData = Base64.getDecoder().decode(encryptedData);
            byte[] ivBytes = new byte[16];
            byte[] salt = new byte[16];
            byte[] ciphertext = new byte[rawData.length - 32];

            System.arraycopy(rawData, 0, ivBytes, 0, 16);
            System.arraycopy(rawData, 16, salt, 0, 16);
            System.arraycopy(rawData, 32, ciphertext, 0, rawData.length - 32);

            SecretKeySpec keySpec = generateKeySpec(masterPassword, salt);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to decrypt data. Please check if the master password and algorithm is correct.", e);
        }
    }

    @NotNull
    private static SecretKeySpec generateKeySpec(String masterPassword, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, 65536, 256); // AES-256
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

}
