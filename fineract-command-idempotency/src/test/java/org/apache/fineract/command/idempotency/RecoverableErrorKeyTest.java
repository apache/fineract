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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RecoverableErrorKeyTest {

    @Test
    void aKeyThatFitsKeepsItsTextAndGainsTheRowId() {
        assertThat(RecoverableErrorKey.movedKey("idk", 42)).isEqualTo("idk_RE42");
    }

    @Test
    void aKeyThatWouldNotFitIsHashedSoThatTheMovedKeyAlwaysFits() {
        var longKey = "k".repeat(RecoverableErrorKey.KEY_LENGTH);
        var moved = RecoverableErrorKey.movedKey(longKey, Long.MAX_VALUE);

        assertThat(moved).hasSizeLessThanOrEqualTo(RecoverableErrorKey.KEY_LENGTH).endsWith("_RE" + Long.MAX_VALUE)
                .matches("[0-9a-f]{" + RecoverableErrorKey.HASH_LENGTH + "}_RE[0-9]+");
        assertThat(RecoverableErrorKey.movedKey(longKey, Long.MAX_VALUE)).as("The same key must always move to the same place.")
                .isEqualTo(moved);
    }

    @Test
    void theLongestKeyThatFitsIsNotHashed() {
        var key = "k".repeat(RecoverableErrorKey.KEY_LENGTH - "_RE7".length());

        assertThat(RecoverableErrorKey.movedKey(key, 7)).isEqualTo(key + "_RE7");
    }
}
