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
package org.apache.fineract.portfolio.workingcapitalloan.serialization.serializer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanChargeDataV1;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.charge.WorkingCapitalLoanChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.ChargeIdAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeEnricher {

    private final WorkingCapitalLoanTransactionRelationRepository transactionRelationRepository;
    private final List<WorkingCapitalLoanChargeExternalEventCustomDataSerializer> chargeExternalEventCustomDataSerializers;

    public void populateAccruals(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanChargeDataV1> charges) {
        if (charges == null || charges.isEmpty() || loan.getLoanProduct() == null
                || !loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            return;
        }
        final Map<Long, BigDecimal> accruedAmountsByChargeId = transactionRelationRepository
                .fetchTransactionAmountPerCharge(loan.getId(), LoanTransactionType.ACCRUAL).stream()
                .collect(Collectors.toMap(ChargeIdAndAmountHolder::chargeId, ChargeIdAndAmountHolder::amount));
        charges.forEach(charge -> {
            final BigDecimal amountAccrued = accruedAmountsByChargeId.getOrDefault(charge.getId(), BigDecimal.ZERO);
            charge.setAmountAccrued(amountAccrued);
            charge.setAmountUnrecognized(MathUtil.subtractToZero(charge.getAmount(), amountAccrued));
        });
    }

    public Map<String, ByteBuffer> collectCustomData(final WorkingCapitalLoanBusinessEvent event, final Long chargeId) {
        return collect(serializer -> serializer.serialize(event, chargeId));
    }

    public Map<String, ByteBuffer> collectCustomData(final WorkingCapitalLoanChargeBusinessEvent event, final Long chargeId) {
        return collect(serializer -> serializer.serialize(event, chargeId));
    }

    private Map<String, ByteBuffer> collect(
            final Function<WorkingCapitalLoanChargeExternalEventCustomDataSerializer, ByteBuffer> serialization) {
        return chargeExternalEventCustomDataSerializers.stream().collect(HashMap::new, (map, serializer) -> {
            final ByteBuffer buffer = serialization.apply(serializer);
            if (buffer != null) {
                map.put(serializer.key(), buffer);
            }
        }, HashMap::putAll);
    }
}
