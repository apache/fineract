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
package org.apache.fineract.portfolio.workingcapitalloan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class WorkingCapitalLoanCommandParameterUtilTest {

    @ParameterizedTest
    @ValueSource(strings = { "chargeoff", "chargeOff", "charge-off", "CHARGEOFF", "Charge-Off", " charge-off " })
    void acceptsEveryNamingConventionOfTheSameCommand(final String commandParam) {
        assertThat(WorkingCapitalLoanCommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.CHARGE_OFF_LOAN_COMMAND)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "-", "charge_off", "charge off", "chargeOffs", "undoChargeOff" })
    void rejectsBlankOrDifferentCommands(final String commandParam) {
        assertThat(WorkingCapitalLoanCommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.CHARGE_OFF_LOAN_COMMAND)).isFalse();
    }

    @Test
    void keepsSimilarCommandsDistinct() {
        assertThat(WorkingCapitalLoanCommandParameterUtil.is("undo-charge-off", WorkingCapitalLoanConstants.UNDO_CHARGE_OFF_LOAN_COMMAND))
                .isTrue();
        assertThat(WorkingCapitalLoanCommandParameterUtil.is("undo-charge-off", WorkingCapitalLoanConstants.UNDO_COMMAND)).isFalse();
        assertThat(WorkingCapitalLoanCommandParameterUtil.is("adjust", WorkingCapitalLoanConstants.ADJUST_COMMAND)).isTrue();
        assertThat(WorkingCapitalLoanCommandParameterUtil.is("adjustment", WorkingCapitalLoanConstants.ADJUST_COMMAND)).isFalse();
    }

    @Test
    void isIndependentOfTheDefaultLocale() {
        final Locale defaultLocale = Locale.getDefault();
        try {
            // Turkish lower-cases 'I' to a dotless 'ı', which would break a locale-sensitive comparison
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(WorkingCapitalLoanCommandParameterUtil.is("WRITE-OFF", WorkingCapitalLoanConstants.WRITE_OFF_LOAN_COMMAND)).isTrue();
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
