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
package org.apache.fineract.portfolio.account.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;

public final class AccountTransferEnumerations {

    private AccountTransferEnumerations() {

    }

    public static EnumOptionData accountType(final Integer type) {
        return accountType(PortfolioAccountType.fromInt(type));
    }

    public static EnumOptionData accountType(final PortfolioAccountType type) {

        EnumOptionData optionData = null;

        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case LOAN:
                    optionData = new EnumOptionData(PortfolioAccountType.LOAN.getValue().longValue(), PortfolioAccountType.LOAN.getCode(),
                            "Loan Account");
                break;
                case SAVINGS:
                    optionData = new EnumOptionData(PortfolioAccountType.SAVINGS.getValue().longValue(),
                            PortfolioAccountType.SAVINGS.getCode(), "Savings Account");
                break;
            }
        }

        return optionData;
    }

    public static EnumOptionData recurrenceType(final Integer type) {
        return recurrenceType(AccountTransferRecurrenceType.fromInt(type));
    }

    public static EnumOptionData recurrenceType(final AccountTransferRecurrenceType type) {
        EnumOptionData optionData = null;
        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case PERIODIC:
                    optionData = new EnumOptionData(AccountTransferRecurrenceType.PERIODIC.getValue().longValue(),
                            AccountTransferRecurrenceType.PERIODIC.getCode(), "Periodic Recurrence");
                break;
                case AS_PER_DUES:
                    optionData = new EnumOptionData(AccountTransferRecurrenceType.AS_PER_DUES.getValue().longValue(),
                            AccountTransferRecurrenceType.AS_PER_DUES.getCode(), "As Per Dues Recurrence");
                break;
            }
        }
        return optionData;
    }

    public static EnumOptionData transferType(final Integer type) {
        return transferType(AccountTransferType.fromInt(type));
    }

    public static EnumOptionData transferType(final AccountTransferType type) {
        EnumOptionData optionData = null;
        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case ACCOUNT_TRANSFER:
                    optionData = new EnumOptionData(AccountTransferType.ACCOUNT_TRANSFER.getValue().longValue(),
                            AccountTransferType.ACCOUNT_TRANSFER.getCode(), "Account Transfer");
                break;
                case LOAN_REPAYMENT:
                    optionData = new EnumOptionData(AccountTransferType.LOAN_REPAYMENT.getValue().longValue(),
                            AccountTransferType.LOAN_REPAYMENT.getCode(), "Loan Repayment");
                break;
                case CHARGE_PAYMENT:
                    optionData = new EnumOptionData(AccountTransferType.CHARGE_PAYMENT.getValue().longValue(),
                            AccountTransferType.CHARGE_PAYMENT.getCode(), "Charge Payment");
                break;
                case INTEREST_TRANSFER:
                    optionData = new EnumOptionData(AccountTransferType.INTEREST_TRANSFER.getValue().longValue(),
                            AccountTransferType.INTEREST_TRANSFER.getCode(), "Interest Transfer");
                break;
                case LOAN_DOWN_PAYMENT:
                    optionData = new EnumOptionData(AccountTransferType.LOAN_DOWN_PAYMENT.getValue().longValue(),
                            AccountTransferType.LOAN_DOWN_PAYMENT.getCode(), "Loan Down Payment");
                break;

            }
        }
        return optionData;
    }

    public static EnumOptionData standingInstructionPriority(final Integer type) {
        return standingInstructionPriority(StandingInstructionPriority.fromInt(type));
    }

    public static EnumOptionData standingInstructionPriority(final StandingInstructionPriority type) {
        EnumOptionData optionData = null;
        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case HIGH:
                    optionData = new EnumOptionData(StandingInstructionPriority.HIGH.getValue().longValue(),
                            StandingInstructionPriority.HIGH.getCode(), "High Priority");
                break;
                case LOW:
                    optionData = new EnumOptionData(StandingInstructionPriority.LOW.getValue().longValue(),
                            StandingInstructionPriority.LOW.getCode(), "Low Priority");
                break;
                case MEDIUM:
                    optionData = new EnumOptionData(StandingInstructionPriority.MEDIUM.getValue().longValue(),
                            StandingInstructionPriority.MEDIUM.getCode(), "Medium Priority");
                break;
                case URGENT:
                    optionData = new EnumOptionData(StandingInstructionPriority.URGENT.getValue().longValue(),
                            StandingInstructionPriority.URGENT.getCode(), "Urgent Priority");
                break;
            }
        }
        return optionData;
    }

    public static EnumOptionData standingInstructionStatus(final Integer type) {
        return standingInstructionStatus(StandingInstructionStatus.fromInt(type));
    }

    public static EnumOptionData standingInstructionStatus(final StandingInstructionStatus type) {
        EnumOptionData optionData = null;
        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case ACTIVE:
                    optionData = new EnumOptionData(StandingInstructionStatus.ACTIVE.getValue().longValue(),
                            StandingInstructionStatus.ACTIVE.getCode(), "Active");
                break;
                case DELETED:
                    optionData = new EnumOptionData(StandingInstructionStatus.DELETED.getValue().longValue(),
                            StandingInstructionStatus.DELETED.getCode(), "Deleted");
                break;
                case DISABLED:
                    optionData = new EnumOptionData(StandingInstructionStatus.DISABLED.getValue().longValue(),
                            StandingInstructionStatus.DISABLED.getCode(), "Disabled");
                break;
            }
        }
        return optionData;
    }

    public static EnumOptionData standingInstructionType(final Integer type) {
        return standingInstructionType(StandingInstructionType.fromInt(type));
    }

    public static EnumOptionData standingInstructionType(final StandingInstructionType type) {
        EnumOptionData optionData = null;
        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case DUES:
                    optionData = new EnumOptionData(StandingInstructionType.DUES.getValue().longValue(),
                            StandingInstructionType.DUES.getCode(), "Dues");
                break;
                case FIXED:
                    optionData = new EnumOptionData(StandingInstructionType.FIXED.getValue().longValue(),
                            StandingInstructionType.FIXED.getCode(), "Fixed");
                break;

            }
        }
        return optionData;
    }

}
