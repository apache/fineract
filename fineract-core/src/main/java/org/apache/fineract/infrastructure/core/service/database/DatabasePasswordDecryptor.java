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

import static org.apache.fineract.infrastructure.core.service.database.DatabasePasswordEncryptor.DEFAULT_ENCRYPTION;

import java.text.MessageFormat;
import org.apache.fineract.infrastructure.security.utils.EncryptionUtil;

public final class DatabasePasswordDecryptor {

    private DatabasePasswordDecryptor() {}

    @SuppressWarnings("checkstyle:regexpsinglelinejava")
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(
                    "Usage: java -cp fineract-provider.jar -Dloader.main=org.apache.fineract.infrastructure.core.service.database.DatabasePasswordDecryptor org.springframework.boot.loader.launch.PropertiesLauncher <masterPassword> <base64Password>");
            System.exit(1);
        }
        String masterPassword = args[0];
        String base64Password = args[1];
        String decryptedPassword = EncryptionUtil.decryptFromBase64(DEFAULT_ENCRYPTION, masterPassword, base64Password);
        System.out.println(MessageFormat.format("The decrypted password: {0}", decryptedPassword));
    }
}
