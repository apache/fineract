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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.loan;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanAccountDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanBusinessEventSerializer implements BusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanAccountDataMapper mapper;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanInstallmentLevelDelinquencyEventProducer installmentLevelDelinquencyEventProducer;
    private final LoanSummaryBalancesRepository loanSummaryBalancesRepository;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanBusinessEvent event = (LoanBusinessEvent) rawEvent;
        Long loanId = event.get().getId();
        LoanAccountData data = service.retrieveOne(loanId);

        data = service.fetchRepaymentScheduleData(data);

        Collection<LoanChargeData> loanCharges = loanChargeReadPlatformService.retrieveLoanCharges(loanId);
        if (CollectionUtils.isNotEmpty(loanCharges)) {
            data.setCharges(loanCharges);
        }

        CollectionData delinquentData = delinquencyReadPlatformService.calculateLoanCollectionData(loanId);
        data.setDelinquent(delinquentData);

        if (data.getSummary() != null) {
            data.setSummary(LoanSummaryData.withTransactionAmountsSummary(data.getSummary(), data.getRepaymentSchedule(),
                    loanSummaryBalancesRepository.retrieveLoanSummaryBalancesByTransactionType(loanId,
                            LoanApiConstants.LOAN_SUMMARY_TRANSACTION_TYPES)));
        } else {
            data.setSummary(LoanSummaryData.withOnlyCurrencyData(data.getCurrency()));
        }

        List<LoanInstallmentDelinquencyBucketDataV1> installmentsDelinquencyData = installmentLevelDelinquencyEventProducer
                .calculateInstallmentLevelDelinquencyData(event.get(), data.getCurrency());

        LoanAccountDataV1 result = mapper.map(data);
        result.getDelinquent().setInstallmentDelinquencyBuckets(installmentsDelinquencyData);
        return result;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanAccountDataV1.class;
    }
}
