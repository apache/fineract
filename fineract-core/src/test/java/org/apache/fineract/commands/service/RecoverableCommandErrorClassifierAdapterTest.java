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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class RecoverableCommandErrorClassifierAdapterTest {

    private final RecoverableErrorClassifier legacy = mock(RecoverableErrorClassifier.class);

    private final RecoverableCommandErrorClassifierAdapter adapter = new RecoverableCommandErrorClassifierAdapter(legacy);

    @Test
    void theLegacyRulesDecide() {
        var recoverable = new IllegalStateException("recoverable");
        var permanent = new IllegalStateException("permanent");

        when(legacy.isRecoverable(recoverable)).thenReturn(true);
        when(legacy.isRecoverable(permanent)).thenReturn(false);

        assertThat(adapter.isRecoverable(recoverable)).isTrue();
        assertThat(adapter.isRecoverable(permanent)).isFalse();
    }

    @Test
    void aFailureTheLegacyRulesCannotClassifyIsNotRecoverable() {
        assertThat(adapter.isRecoverable(new Exception("checked"))).isFalse();
        assertThat(adapter.isRecoverable(null)).isFalse();
        verifyNoInteractions(legacy);
    }
}
