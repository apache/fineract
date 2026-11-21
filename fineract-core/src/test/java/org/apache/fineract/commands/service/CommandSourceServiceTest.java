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
package org.apache.fineract.commands.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.CannotCreateTransactionException;

@ExtendWith(MockitoExtension.class)
class CommandSourceServiceTest {

    private static final long ROW_ID = 42L;

    @Mock
    private CommandSourceRepository commandSourceRepository;

    @InjectMocks
    private CommandSourceService underTest;

    @Test
    void recoverableFailure_savesErrorUnderKeyAndRowId() {
        CommandSource commandSource = errorRow("idk", ROW_ID);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of("idk_RE42"), savedKeys);
        assertEquals("idk_RE42", commandSource.getIdempotencyKey());
        assertEquals(CommandProcessingResultType.ERROR.getValue(), commandSource.getStatus());
    }

    @Test
    void uuidKey_isRenamedWithTheLargestRowId() {
        String key = UUID.randomUUID().toString();
        CommandSource commandSource = errorRow(key, Long.MAX_VALUE);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(key + "_RE" + Long.MAX_VALUE), savedKeys);
    }

    @Test
    void renamedKeyExactlyColumnLength_isRenamed() {
        // 58 + "_RE42" = 63
        String key = "k".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH - 5);
        CommandSource commandSource = errorRow(key, ROW_ID);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(key + "_RE42"), savedKeys);
    }

    @Test
    void renamedKeyLongerThanColumn_isSavedUnderHashOfKey() throws Exception {
        // 59 + "_RE42" = 64
        String key = "k".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH - 4);
        CommandSource commandSource = errorRow(key, ROW_ID);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(sha256Prefix(key) + "_RE42"), savedKeys);
    }

    @Test
    void longestKeyWithLargestRowId_hashedKeyFitsTheColumn() throws Exception {
        String key = "k".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH);
        CommandSource commandSource = errorRow(key, Long.MAX_VALUE);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        String expected = sha256Prefix(key) + "_RE" + Long.MAX_VALUE;
        assertEquals(List.of(expected), savedKeys);
        assertEquals(62, expected.length());
    }

    @Test
    void hashedKeys_differForDifferentKeysAndRowIds() {
        String key = "a".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH);
        String otherKey = "b".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH);

        assertEquals(CommandSourceService.renamedIdempotencyKey(key, 7L), CommandSourceService.renamedIdempotencyKey(key, 7L));
        assertNotEquals(CommandSourceService.renamedIdempotencyKey(key, 7L), CommandSourceService.renamedIdempotencyKey(key, 8L));
        assertNotEquals(CommandSourceService.renamedIdempotencyKey(key, 7L), CommandSourceService.renamedIdempotencyKey(otherKey, 7L));
    }

    @Test
    void hashedKeyAlreadyTaken_fallsBackToOriginalKey() throws Exception {
        String key = "k".repeat(CommandSource.IDEMPOTENCY_KEY_LENGTH);
        String hashedKey = sha256Prefix(key) + "_RE42";
        CommandSource commandSource = errorRow(key, ROW_ID);
        List<String> savedKeys = new ArrayList<>();
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenAnswer(invocation -> {
            CommandSource saved = invocation.getArgument(0);
            savedKeys.add(saved.getIdempotencyKey());
            if (hashedKey.equals(saved.getIdempotencyKey())) {
                throw uniqueViolation();
            }
            return saved;
        });

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(hashedKey, key), savedKeys);
        assertEquals(key, commandSource.getIdempotencyKey());
    }

    @Test
    void rowWithoutId_keepsOriginalKey() {
        CommandSource commandSource = errorRow("idk", null);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of("idk"), savedKeys);
    }

    @Test
    void renamedKeyAlreadyTaken_fallsBackToOriginalKey() {
        CommandSource commandSource = errorRow("idk", ROW_ID);
        List<String> savedKeys = new ArrayList<>();
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenAnswer(invocation -> {
            CommandSource saved = invocation.getArgument(0);
            savedKeys.add(saved.getIdempotencyKey());
            if ("idk_RE42".equals(saved.getIdempotencyKey())) {
                throw uniqueViolation();
            }
            return saved;
        });

        assertDoesNotThrow(() -> underTest.saveRecoverableErrorResult(commandSource));

        assertEquals(List.of("idk_RE42", "idk"), savedKeys);
        assertEquals("idk", commandSource.getIdempotencyKey());
    }

    @Test
    void renameFailsWithOtherJpaException_restoresOriginalKeyAndRethrows() {
        CommandSource commandSource = errorRow("idk", ROW_ID);
        JpaSystemException failure = new JpaSystemException(new RuntimeException(new SQLException("deadlock detected")));
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenThrow(failure);

        JpaSystemException thrown = assertThrows(JpaSystemException.class, () -> underTest.saveRecoverableErrorResult(commandSource));

        assertEquals(failure, thrown);
        assertEquals("idk", commandSource.getIdempotencyKey());
    }

    @Test
    void renameFailsWithOtherException_restoresOriginalKeyAndRethrows() {
        CommandSource commandSource = errorRow("idk", ROW_ID);
        CannotCreateTransactionException failure = new CannotCreateTransactionException("connection lost");
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenThrow(failure);

        CannotCreateTransactionException thrown = assertThrows(CannotCreateTransactionException.class,
                () -> underTest.saveRecoverableErrorResult(commandSource));

        assertEquals(failure, thrown);
        assertEquals("idk", commandSource.getIdempotencyKey());
    }

    private List<String> recordSavedKeys() {
        List<String> savedKeys = new ArrayList<>();
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenAnswer(invocation -> {
            CommandSource saved = invocation.getArgument(0);
            savedKeys.add(saved.getIdempotencyKey());
            return saved;
        });
        return savedKeys;
    }

    private static CommandSource errorRow(String idempotencyKey, Long id) {
        CommandSource commandSource = CommandSource.builder().actionName("CREATE").entityName("CLIENT").idempotencyKey(idempotencyKey)
                .status(CommandProcessingResultType.ERROR.getValue()).resultStatusCode(400).build();
        commandSource.setId(id);
        return commandSource;
    }

    private static String sha256Prefix(String key) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, 40);
    }

    private static JpaSystemException uniqueViolation() {
        return new JpaSystemException(new RuntimeException(
                new SQLException("duplicate key value violates unique constraint \"unique_portfolio_command_source\"")));
    }
}
