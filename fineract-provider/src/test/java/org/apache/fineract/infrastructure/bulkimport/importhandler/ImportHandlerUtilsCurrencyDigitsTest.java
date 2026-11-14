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
package org.apache.fineract.infrastructure.bulkimport.importhandler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ImportHandlerUtilsCurrencyDigitsTest {

    @Test
    void amountWithMoreDecimalsThanTheCurrencyIsRejected() {
        assertThatThrownBy(() -> ImportHandlerUtils.requireAmountWithinCurrencyDigits(new BigDecimal("1.123456"), 2))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount has more decimal places than the currency allows (2)");
    }

    @Test
    void amountWithinTheCurrencyDigitsIsAccepted() {
        assertThatCode(() -> ImportHandlerUtils.requireAmountWithinCurrencyDigits(new BigDecimal("1.12"), 2)).doesNotThrowAnyException();
        assertThatCode(() -> ImportHandlerUtils.requireAmountWithinCurrencyDigits(BigDecimal.valueOf(10.5), 2)).doesNotThrowAnyException();
        assertThatCode(() -> ImportHandlerUtils.requireAmountWithinCurrencyDigits(new BigDecimal("100.000"), 0)).doesNotThrowAnyException();
    }

    @Test
    void unknownCurrencyDigitsAreNotChecked() {
        assertThatCode(() -> ImportHandlerUtils.requireAmountWithinCurrencyDigits(new BigDecimal("1.123456"), null))
                .doesNotThrowAnyException();
    }
}
