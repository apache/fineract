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
package org.apache.fineract.infrastructure.bulkimport.importhandler.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClientPersonImportHandlerSubmittedOnTest {

    @Test
    void submittedOnAfterActivationDateIsRejected() {
        assertThatThrownBy(
                () -> ClientPersonImportHandler.requireSubmittedOnNotAfterActivation(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Submitted On date cannot be after the Activation date");
    }

    @Test
    void submittedOnOnOrBeforeActivationDateIsAccepted() {
        assertThatCode(
                () -> ClientPersonImportHandler.requireSubmittedOnNotAfterActivation(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1)))
                .doesNotThrowAnyException();
        assertThatCode(
                () -> ClientPersonImportHandler.requireSubmittedOnNotAfterActivation(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1)))
                .doesNotThrowAnyException();
        assertThatCode(() -> ClientPersonImportHandler.requireSubmittedOnNotAfterActivation(LocalDate.of(2026, 9, 1), null))
                .doesNotThrowAnyException();
    }
}
