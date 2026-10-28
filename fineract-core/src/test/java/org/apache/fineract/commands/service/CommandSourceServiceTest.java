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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    private static final String ACTION = "CREATE";
    private static final String ENTITY = "CLIENT";

    @Mock
    private CommandSourceRepository commandSourceRepository;

    @InjectMocks
    private CommandSourceService underTest;

    @Test
    void recoverableFailure_firstRename_savesErrorUnderKeyRe1() {
        CommandSource commandSource = errorRow("idk");
        when(commandSourceRepository.countRecoverableRenames(ACTION, ENTITY, "idk!_RE%")).thenReturn(0L);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of("idk_RE1"), savedKeys);
        assertEquals(CommandProcessingResultType.ERROR.getValue(), commandSource.getStatus());
    }

    @Test
    void recoverableFailure_oneRenamedRowExists_savesUnderKeyRe2() {
        CommandSource commandSource = errorRow("idk");
        when(commandSourceRepository.countRecoverableRenames(ACTION, ENTITY, "idk!_RE%")).thenReturn(1L);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of("idk_RE2"), savedKeys);
        verify(commandSourceRepository).countRecoverableRenames(ACTION, ENTITY, "idk!_RE%");
    }

    @Test
    void renameCollidesThreeTimes_fallsBackToOriginalKey() {
        CommandSource commandSource = errorRow("idk");
        when(commandSourceRepository.countRecoverableRenames(ACTION, ENTITY, "idk!_RE%")).thenReturn(0L);
        List<String> savedKeys = new ArrayList<>();
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenAnswer(invocation -> {
            CommandSource saved = invocation.getArgument(0);
            savedKeys.add(saved.getIdempotencyKey());
            if (saved.getIdempotencyKey().contains("_RE")) {
                throw uniqueViolation();
            }
            return saved;
        });

        assertDoesNotThrow(() -> underTest.saveRecoverableErrorResult(commandSource));

        assertEquals(List.of("idk_RE1", "idk_RE2", "idk_RE3", "idk"), savedKeys);
        assertEquals("idk", commandSource.getIdempotencyKey());
    }

    @Test
    void renameFailsWithOtherException_restoresOriginalKeyAndRethrows() {
        CommandSource commandSource = errorRow("idk");
        when(commandSourceRepository.countRecoverableRenames(ACTION, ENTITY, "idk!_RE%")).thenReturn(0L);
        CannotCreateTransactionException failure = new CannotCreateTransactionException("connection lost");
        when(commandSourceRepository.saveAndFlush(any(CommandSource.class))).thenThrow(failure);

        CannotCreateTransactionException thrown = assertThrows(CannotCreateTransactionException.class,
                () -> underTest.saveRecoverableErrorResult(commandSource));

        assertEquals(failure, thrown);
        assertEquals("idk", commandSource.getIdempotencyKey());
    }

    @Test
    void renamedKeyLongerThanColumn_keepsOriginalKey() {
        String key = "k".repeat(61);
        CommandSource commandSource = errorRow(key);
        when(commandSourceRepository.countRecoverableRenames(eq(ACTION), eq(ENTITY), anyString())).thenReturn(0L);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(key), savedKeys);
    }

    @Test
    void renamedKeyExactlyColumnLength_isRenamed() {
        String key = "k".repeat(60);
        CommandSource commandSource = errorRow(key);
        when(commandSourceRepository.countRecoverableRenames(eq(ACTION), eq(ENTITY), anyString())).thenReturn(0L);
        List<String> savedKeys = recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        assertEquals(List.of(key + "_RE1"), savedKeys);
    }

    @Test
    void likePatternEscapesWildcardsAndEscapeCharacter() {
        CommandSource commandSource = errorRow("a_b%c!");
        when(commandSourceRepository.countRecoverableRenames(eq(ACTION), eq(ENTITY), anyString())).thenReturn(0L);
        recordSavedKeys();

        underTest.saveRecoverableErrorResult(commandSource);

        verify(commandSourceRepository).countRecoverableRenames(ACTION, ENTITY, "a!_b!%c!!!_RE%");
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

    private static CommandSource errorRow(String idempotencyKey) {
        return CommandSource.builder().actionName(ACTION).entityName(ENTITY).idempotencyKey(idempotencyKey)
                .status(CommandProcessingResultType.ERROR.getValue()).resultStatusCode(400).build();
    }

    private static JpaSystemException uniqueViolation() {
        return new JpaSystemException(new RuntimeException(
                new SQLException("duplicate key value violates unique constraint \"unique_portfolio_command_source\"")));
    }
}
