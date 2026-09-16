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
package org.apache.fineract.investor.enricher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionEnumDataV1;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.service.ExcludedTransactionTypesService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The adjustment enricher delegates to {@link LoanTransactionDataV1Enricher} for both sides of the adjustment, so the
 * two records must be classified independently of each other.
 */
@ExtendWith(MockitoExtension.class)
public class LoanTransactionAdjustmentDataV1EnricherTest {

    private static final Long LOAN_ID = 22L;
    private static final Long LOAN_PRODUCT_ID = 33L;
    private static final String OWNER_EXTERNAL_ID = "owner-external-id";

    @Mock
    private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    @Mock
    private ExternalIdMapper externalIdMapper;
    @Mock
    private ExcludedTransactionTypesService excludedTransactionTypesService;
    @Mock
    private LoanRepository loanRepository;

    @Test
    public void testBothSidesOfTheAdjustmentAreEvaluatedIndependently() {
        LoanTransactionAdjustmentDataV1 data = new LoanTransactionAdjustmentDataV1();
        LoanTransactionDataV1 excludedSide = transaction(1L, LoanTransactionType.BUY_DOWN_FEE.getValue());
        LoanTransactionDataV1 attributedSide = transaction(2L, LoanTransactionType.REPAYMENT.getValue());
        data.setTransactionToAdjust(excludedSide);
        data.setNewTransactionDetail(attributedSide);

        stubActiveOwner();
        when(loanRepository.findLoanProductIdByLoanId(LOAN_ID)).thenReturn(Optional.of(LOAN_PRODUCT_ID));
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.BUY_DOWN_FEE)).thenReturn(true);
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.REPAYMENT)).thenReturn(false);

        new LoanTransactionAdjustmentDataV1Enricher(new LoanTransactionDataV1Enricher(externalAssetOwnerTransferRepository,
                externalIdMapper, excludedTransactionTypesService, loanRepository)).enrich(data);

        assertNull(excludedSide.getExternalOwnerId());
        assertEquals(OWNER_EXTERNAL_ID, attributedSide.getExternalOwnerId());
    }

    private LoanTransactionDataV1 transaction(final Long id, final Integer transactionTypeId) {
        LoanTransactionDataV1 data = new LoanTransactionDataV1();
        data.setId(id);
        data.setLoanId(LOAN_ID);
        LoanTransactionEnumDataV1 type = new LoanTransactionEnumDataV1();
        type.setId(transactionTypeId);
        data.setType(type);
        return data;
    }

    private void stubActiveOwner() {
        ExternalAssetOwner owner = Mockito.mock(ExternalAssetOwner.class);
        when(externalAssetOwnerTransferRepository.findActiveOwnerByLoanId(LOAN_ID)).thenReturn(Optional.of(owner));
        Mockito.lenient().when(owner.getExternalId()).thenReturn(ExternalId.generate());
        Mockito.lenient().when(externalIdMapper.mapExternalId(any())).thenReturn(OWNER_EXTERNAL_ID);
    }
}
