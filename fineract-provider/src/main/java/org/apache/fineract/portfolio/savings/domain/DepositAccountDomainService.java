/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.useradministration.domain.AppUser;

public interface DepositAccountDomainService {

    SavingsAccountTransaction handleWithdrawal(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean applyWithdrawFee, boolean isRegularTransaction);

    SavingsAccountTransaction handleFDDeposit(FixedDepositAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail);

    SavingsAccountTransaction handleRDDeposit(RecurringDepositAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean isRegularTransaction);

    Long handleFDAccountClosure(FixedDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);

    Long handleRDAccountClosure(RecurringDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);

    Long handleFDAccountPreMatureClosure(FixedDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);

    Long handleRDAccountPreMatureClosure(RecurringDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);
}