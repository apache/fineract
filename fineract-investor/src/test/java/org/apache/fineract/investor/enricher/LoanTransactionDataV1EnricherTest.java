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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanTransactionDataV1EnricherTest {

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

    @InjectMocks
    private LoanTransactionDataV1Enricher underTest;

    @Test
    public void testTransactionOfNotExternallyOwnedLoanIsNotEnrichedAndNothingElseIsLookedUp() {
        LoanTransactionDataV1 data = transaction(LoanTransactionType.BUY_DOWN_FEE.getValue());
        when(externalAssetOwnerTransferRepository.findActiveOwnerByLoanId(LOAN_ID)).thenReturn(Optional.empty());

        underTest.enrich(data);

        assertNull(data.getExternalOwnerId());
        verifyNoInteractions(loanRepository);
        verifyNoInteractions(excludedTransactionTypesService);
    }

    @Test
    public void testTransactionOfNonExcludedTypeCarriesTheOwnerExternalId() {
        LoanTransactionDataV1 data = transaction(LoanTransactionType.REPAYMENT.getValue());
        stubActiveOwner();
        stubLoanProduct();
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.REPAYMENT)).thenReturn(false);

        underTest.enrich(data);

        assertEquals(OWNER_EXTERNAL_ID, data.getExternalOwnerId());
    }

    @Test
    public void testTransactionOfExcludedTypeDoesNotCarryTheOwnerExternalId() {
        LoanTransactionDataV1 data = transaction(LoanTransactionType.BUY_DOWN_FEE.getValue());
        stubActiveOwner();
        stubLoanProduct();
        when(excludedTransactionTypesService.isExcluded(LOAN_PRODUCT_ID, LoanTransactionType.BUY_DOWN_FEE)).thenReturn(true);

        underTest.enrich(data);

        assertNull(data.getExternalOwnerId());
    }

    @Test
    public void testTransactionWithoutTypeFallsBackToOwnerAttribution() {
        LoanTransactionDataV1 data = transaction(null);
        data.setType(null);
        stubActiveOwner();

        underTest.enrich(data);

        assertEquals(OWNER_EXTERNAL_ID, data.getExternalOwnerId());
        verifyNoInteractions(loanRepository);
        verifyNoInteractions(excludedTransactionTypesService);
    }

    @Test
    public void testTransactionWithUnmappableTypeIdFallsBackToOwnerAttribution() {
        LoanTransactionDataV1 data = transaction(9999);
        stubActiveOwner();

        underTest.enrich(data);

        assertEquals(OWNER_EXTERNAL_ID, data.getExternalOwnerId());
        verifyNoInteractions(loanRepository);
        verifyNoInteractions(excludedTransactionTypesService);
    }

    @Test
    public void testUnresolvableLoanProductFallsBackToOwnerAttribution() {
        LoanTransactionDataV1 data = transaction(LoanTransactionType.BUY_DOWN_FEE.getValue());
        stubActiveOwner();
        when(loanRepository.findLoanProductIdByLoanId(LOAN_ID)).thenReturn(Optional.empty());

        underTest.enrich(data);

        assertEquals(OWNER_EXTERNAL_ID, data.getExternalOwnerId());
        verifyNoInteractions(excludedTransactionTypesService);
    }

    private LoanTransactionDataV1 transaction(final Integer transactionTypeId) {
        LoanTransactionDataV1 data = new LoanTransactionDataV1();
        data.setId(1L);
        data.setLoanId(LOAN_ID);
        LoanTransactionEnumDataV1 type = new LoanTransactionEnumDataV1();
        type.setId(transactionTypeId);
        data.setType(type);
        return data;
    }

    private void stubActiveOwner() {
        ExternalAssetOwner owner = Mockito.mock(ExternalAssetOwner.class);
        ExternalId externalId = ExternalId.generate();
        when(externalAssetOwnerTransferRepository.findActiveOwnerByLoanId(LOAN_ID)).thenReturn(Optional.of(owner));
        // Not reached when the transaction type is excluded, which is exactly what the exclusion test asserts.
        Mockito.lenient().when(owner.getExternalId()).thenReturn(externalId);
        Mockito.lenient().when(externalIdMapper.mapExternalId(any())).thenReturn(OWNER_EXTERNAL_ID);
    }

    private void stubLoanProduct() {
        when(loanRepository.findLoanProductIdByLoanId(LOAN_ID)).thenReturn(Optional.of(LOAN_PRODUCT_ID));
    }
}
