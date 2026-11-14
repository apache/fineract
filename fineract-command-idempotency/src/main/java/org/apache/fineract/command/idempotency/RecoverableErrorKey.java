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
package org.apache.fineract.command.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The key a failed command is moved to when its failure is recoverable, so that its original key is free for a retry.
 *
 * <p>
 * The same scheme the legacy command processing uses: {@code <key>_RE<id>}, where {@code <id>} is the command's own row
 * id, so the moved key is unique and still traceable to the original. When that would not fit the column, the key part
 * is replaced by the first {@value #HASH_LENGTH} hex characters of its SHA-256 hash, which always fits.
 */
public final class RecoverableErrorKey {

    public static final String SUFFIX = "_RE";

    /** The width of {@code m_command.idempotency_key}. */
    public static final int KEY_LENGTH = 255;

    static final int HASH_LENGTH = 40;

    private RecoverableErrorKey() {}

    public static String movedKey(String idempotencyKey, long commandId) {
        var suffix = SUFFIX + commandId;

        if (idempotencyKey.length() + suffix.length() <= KEY_LENGTH) {
            return idempotencyKey + suffix;
        }

        return hash(idempotencyKey) + suffix;
    }

    private static String hash(String idempotencyKey) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(idempotencyKey.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest).substring(0, HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
