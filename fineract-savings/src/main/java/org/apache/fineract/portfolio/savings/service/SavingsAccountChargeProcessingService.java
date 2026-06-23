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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

/**
 * Orchestrates all charge operations on a {@link SavingsAccount}: the user-facing ones (add, remove, waive, pay) as
 * well as the low-level transaction primitives (pay a charge, the withdrawal-fee chain) and the dormancy
 * ({@code inactive}) transition. The bodies were extracted from the entity; behaviour is intentionally unchanged.
 * Account state is read/written through the public API of the entity, so the charge orchestration no longer lives on
 * the domain entity.
 */
public interface SavingsAccountChargeProcessingService {

    void addCharge(SavingsAccount account, DateTimeFormatter formatter, SavingsAccountCharge savingsAccountCharge, Charge chargeDefinition);

    void removeCharge(SavingsAccount account, SavingsAccountCharge charge);

    void waiveCharge(SavingsAccount account, Long savingsAccountChargeId, boolean backdatedTxnsAllowedTill);

    SavingsAccountTransaction payCharge(SavingsAccount account, SavingsAccountCharge savingsAccountCharge, BigDecimal amountPaid,
            LocalDate transactionDate, DateTimeFormatter formatter, boolean backdatedTxnsAllowedTill, String refNo);

    SavingsAccountTransaction payCharge(SavingsAccount account, SavingsAccountCharge savingsAccountCharge, Money amountPaid,
            LocalDate transactionDate, boolean backdatedTxnsAllowedTill, String refNo);

    void payWithdrawalFee(SavingsAccount account, BigDecimal transactionAmount, LocalDate transactionDate, PaymentDetail paymentDetail,
            boolean backdatedTxnsAllowedTill, String refNo);

    void setSubStatusInactive(SavingsAccount account, boolean backdatedTxnsAllowedTill);
}
