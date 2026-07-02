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

package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;

/**
 * Immutable input to the working capital allocation processor. Carries only the data needed to decide how a
 * repayment-like amount splits across penalty charges, fee charges and principal - no JPA entities. The
 * {@code principalOutstanding} and per-charge {@code outstanding} reflect the live (already paid down) state at the
 * moment of the decision, so the processor decides against the remaining balance.
 */
public record WorkingCapitalLoanAllocationRequest(LocalDate transactionDate, BigDecimal amount,
        List<WorkingCapitalPaymentAllocationType> allocationOrder, BigDecimal principalOutstanding, List<ChargeBalance> charges) {

    public record ChargeBalance(Long chargeId, BigDecimal outstanding, LocalDate dueDate, boolean penalty) {
    }
}
