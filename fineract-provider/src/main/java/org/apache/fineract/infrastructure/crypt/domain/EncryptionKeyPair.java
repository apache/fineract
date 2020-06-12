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
package org.apache.fineract.infrastructure.crypt.domain;

import org.joda.time.LocalDateTime;

/**
 * @author manoj
 */
public final class EncryptionKeyPair {
    private final byte[] privateKey;
    private final byte[] publicKey;
    private final String version;
    private final LocalDateTime createdDateTime;

    public EncryptionKeyPair(byte[] privateKey, byte[] publicKey, LocalDateTime createdDateTime, final String version) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.createdDateTime = createdDateTime;
        this.version = version;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public String getVersion() {
        return version;
    }
}