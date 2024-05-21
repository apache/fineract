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
package org.apache.fineract.infrastructure.core.service.database;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.service.PasswordEncryptor;
import org.apache.fineract.infrastructure.security.utils.EncryptionUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabasePasswordEncryptor implements PasswordEncryptor {

    public static final String DEFAULT_ENCRYPTION = "AES/CBC/PKCS5Padding";

    private final FineractProperties fineractProperties;

    @SuppressWarnings("checkstyle:regexpsinglelinejava")
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(
                    "Usage: java -cp fineract-provider.jar -Dloader.main=org.apache.fineract.infrastructure.core.service.database.DatabasePasswordEncryptor org.springframework.boot.loader.launch.PropertiesLauncher <masterPassword> <plainPassword>");
            System.exit(1);
        }
        String masterPassword = args[0];
        String plainPassword = args[1];
        String encryptedPassword = EncryptionUtil.encryptToBase64(DEFAULT_ENCRYPTION, masterPassword, plainPassword);
        System.out.println(MessageFormat.format("The encrypted password: {0}", encryptedPassword));
        System.out.println(MessageFormat.format("The master password hash is: {0}", getPasswordHash(masterPassword)));
    }

    @Override
    public String encrypt(String plainPassword) {
        String masterPassword = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getMasterPassword)
                .orElse(fineractProperties.getDatabase().getDefaultMasterPassword());
        String encryption = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getEncryption).orElse(DEFAULT_ENCRYPTION);
        return EncryptionUtil.encryptToBase64(encryption, masterPassword, plainPassword);
    }

    @Override
    public String decrypt(String encryptedPassword) {
        String masterPassword = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getMasterPassword)
                .orElse(fineractProperties.getDatabase().getDefaultMasterPassword());
        String encryption = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getEncryption).orElse(DEFAULT_ENCRYPTION);
        return EncryptionUtil.decryptFromBase64(encryption, masterPassword, encryptedPassword);
    }

    public String getMasterPasswordHash() {
        String masterPassword = Optional.ofNullable(fineractProperties) //
                .map(FineractProperties::getTenant) //
                .map(FineractProperties.FineractTenantProperties::getMasterPassword) //
                .orElse(fineractProperties.getDatabase().getDefaultMasterPassword());
        return getPasswordHash(masterPassword);
    }

    private static String getPasswordHash(String masterPassword) {
        return BCrypt.hashpw(masterPassword.getBytes(StandardCharsets.UTF_8), BCrypt.gensalt());
    }

    public boolean isMasterPasswordHashValid(String hashed) {
        String masterPassword = Optional.ofNullable(fineractProperties) //
                .map(FineractProperties::getTenant) //
                .map(FineractProperties.FineractTenantProperties::getMasterPassword) //
                .orElse(fineractProperties.getDatabase().getDefaultMasterPassword());
        return BCrypt.checkpw(masterPassword, hashed);
    }
}
