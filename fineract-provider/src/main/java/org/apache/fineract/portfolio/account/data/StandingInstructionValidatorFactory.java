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
package org.apache.fineract.portfolio.account.data;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;


import com.google.gson.JsonElement;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;

public final class StandingInstructionValidatorFactory {

    private StandingInstructionValidatorFactory() {}

    public static StandingInstructionValidator getStrategy(final FromJsonHelper fromApiJsonHelper, final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final Locale locale = Locale.getDefault();
        final Integer transferType = fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, element, locale);

        if (transferType == null) {
            return new InexistingStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
        }

        if (isAccountTransfer(transferType)) {
            return new AccountTransferStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
        }

        final Integer instructionType = fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, element, locale);
        final Integer recurrenceType = fromApiJsonHelper.extractIntegerNamed(recurrenceTypeParamName, element, locale);

        if (instructionType == null || recurrenceType == null) {
            return new InexistingStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
        }
        
        if (isLoanRepayment(transferType) && isFixedInstruction(instructionType) && isPeriodicRecurrence(recurrenceType)) {
            return new PeriodicFixedAmountLoanRepaymentStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
        }

        if (isLoanRepayment(transferType) && isDuesInstruction(instructionType)) {
            if (isPeriodicRecurrence(recurrenceType)) {
                return new PeriodicDuesLoanRepaymentStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
            } else if (isAsPerDuesRecurrence(recurrenceType)) {
                return new LoanRepaymentStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
            }
        }

        return new InexistingStandingInstruction(fromApiJsonHelper, element, baseDataValidator);
    }

    private static boolean isAccountTransfer(final Integer transferType) {
        return AccountTransferType.fromInt(transferType).isAccountTransfer();
    }

    private static boolean isLoanRepayment(final Integer transferType) {
        return AccountTransferType.fromInt(transferType).isLoanRepayment();
    }

    private static boolean isFixedInstruction(final Integer instructionType) {
        return StandingInstructionType.fromInt(instructionType).isFixedAmoutTransfer();
    }

    private static boolean isDuesInstruction(final Integer instructionType) {
        return StandingInstructionType.fromInt(instructionType).isDuesAmoutTransfer();
    }

    private static boolean isPeriodicRecurrence(final Integer recurrenceType) {
        return AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence();
    }

    private static boolean isAsPerDuesRecurrence(final Integer recurrenceType) {
        return AccountTransferRecurrenceType.fromInt(recurrenceType).isDuesRecurrence();
    }
}