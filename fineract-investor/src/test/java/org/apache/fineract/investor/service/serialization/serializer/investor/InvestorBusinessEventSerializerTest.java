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
package org.apache.fineract.investor.service.serialization.serializer.investor;

import static org.apache.fineract.investor.data.ExternalTransferStatus.ACTIVE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.BUYBACK;
import static org.apache.fineract.investor.data.ExternalTransferStatus.CANCELLED;
import static org.apache.fineract.investor.data.ExternalTransferStatus.DECLINED;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.BALANCE_NEGATIVE;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.BALANCE_ZERO;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.SAMEDAY_TRANSFERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.loan.v1.LoanOwnershipTransferDataV1;
import org.apache.fineract.avro.loan.v1.UnpaidChargeDataV1;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferDataDetails;
import org.apache.fineract.investor.data.ExternalTransferLoanData;
import org.apache.fineract.investor.data.ExternalTransferOwnerData;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.investor.service.ExternalAssetOwnersReadService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class InvestorBusinessEventSerializerTest {

    public static final long LOAN_ID = 222L;
    public static final String ASSET_OWNER_EXTERNAL_ID = "1ad87015-8b05-49de-9ed8-e9c214fda7eb";
    public static final String LOAN_EXTERNAL_ID = "29641fe0-0ac6-409a-bb8b-24fdf08d2891";
    public static final ExternalId TRANSFER_EXTERNAL_ID = new ExternalId("ac303982-46a5-4cea-9f71-67b69063a2b7");

    @Test
    public void testSerializationSellOK() {
        doTest(ACTIVE, null, "SALE", "EXECUTED", null);
    }

    @Test
    public void testSerializationBuybackOK() {
        doTest(BUYBACK, null, "BUYBACK", "EXECUTED", null);
    }

    @Test
    public void testSerializationDeclinedNegativeBalance() {
        doTest(DECLINED, BALANCE_NEGATIVE, "SALE", "DECLINED", "BALANCE_NEGATIVE");
    }

    @Test
    public void testSerializationDeclinedBalanceZero() {
        doTest(DECLINED, BALANCE_ZERO, "SALE", "DECLINED", "BALANCE_ZERO");
    }

    @Test
    public void testSerializationCancelledSameDayTransfer() {
        doTest(CANCELLED, SAMEDAY_TRANSFERS, "SALE", "CANCELLED", "SAMEDAY_TRANSFERS");
    }

    private void doTest(ExternalTransferStatus status, ExternalTransferSubStatus subStatus, String expectedType, String expectedStatus,
            String expectedReason) {
        // given
        ExternalAssetOwnersReadService mockReadService = Mockito.mock(ExternalAssetOwnersReadService.class);
        when(mockReadService.retrieveTransferData(123L)).thenReturn(createTransferData(status, subStatus));
        when(mockReadService.retrieveFirstTransferByExternalId(any(ExternalId.class))).thenReturn(createTransferData(ACTIVE, null));
        Loan loan = Mockito.mock(Loan.class);
        when(loan.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, 1));
        List<LoanCharge> loanCharges = createMockCharges();
        when(loan.getLoanCharges()).thenReturn(loanCharges);
        LoanOwnershipTransferBusinessEvent loanOwnershipTransferBusinessEvent = new LoanOwnershipTransferBusinessEvent(
                createExternalAssetOwnerTransfer(status, subStatus), loan);

        // when
        InvestorBusinessEventSerializer serializer = new InvestorBusinessEventSerializer(mockReadService);
        ByteBufferSerializable byteBufferSerializable = serializer.toAvroDTO(loanOwnershipTransferBusinessEvent);

        // then
        verifyFields(byteBufferSerializable, expectedType, expectedStatus, expectedReason);
    }

    private List<LoanCharge> createMockCharges() {
        List<LoanCharge> loanCharges = new ArrayList<>();
        loanCharges.add(loanCharge(1L, "charge a", new BigDecimal("10.00000")));
        loanCharges.add(loanCharge(1L, "charge a", new BigDecimal("15.00000")));
        loanCharges.add(loanCharge(2L, "charge b", BigDecimal.ZERO));
        loanCharges.add(loanCharge(3L, "charge c", new BigDecimal("12.00000")));
        return loanCharges;
    }

    private LoanCharge loanCharge(Long chargeId, String name, BigDecimal amountOutstanding) {
        LoanCharge loanCharge = mock(LoanCharge.class);
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(chargeId);
        when(charge.getName()).thenReturn(name);
        when(loanCharge.name()).thenReturn(name);
        when(loanCharge.getCharge()).thenReturn(charge);
        when(loanCharge.amountOutstanding()).thenReturn(amountOutstanding);
        return loanCharge;
    }

    private static void verifyFields(ByteBufferSerializable byteBufferSerializable, String type, String status, String statusReason) {
        assertTrue(byteBufferSerializable instanceof LoanOwnershipTransferDataV1);
        LoanOwnershipTransferDataV1 result = (LoanOwnershipTransferDataV1) byteBufferSerializable;
        assertEquals(LOAN_ID, result.getLoanId());
        assertEquals("EUR", result.getCurrency().getCode());
        assertEquals(2, result.getCurrency().getDecimalPlaces());
        assertEquals(1, result.getCurrency().getInMultiplesOf());
        assertEquals("1.0", result.getPurchasePriceRatio());
        assertEquals(ASSET_OWNER_EXTERNAL_ID, result.getAssetOwnerExternalId());
        assertEquals(LOAN_EXTERNAL_ID, result.getLoanExternalId());
        assertEquals(TRANSFER_EXTERNAL_ID.getValue(), result.getTransferExternalId());
        assertEquals(LOAN_ID, result.getLoanId());
        assertEquals(new BigDecimal("1108.00000"), result.getTotalOutstandingBalanceAmount());
        assertEquals(new BigDecimal("100.00000"), result.getOutstandingInterestPortion());
        assertEquals(new BigDecimal("1000.00000"), result.getOutstandingPrincipalPortion());
        assertEquals(new BigDecimal("5.00000"), result.getOutstandingFeePortion());
        assertEquals(new BigDecimal("3.00000"), result.getOutstandingPenaltyPortion());
        assertEquals(BigDecimal.ZERO, result.getOverPaymentPortion());
        assertEquals("2023-06-11", result.getSettlementDate());
        assertEquals("2023-06-11", result.getSubmittedDate());
        assertEquals(status, result.getTransferStatus());
        assertEquals(statusReason, result.getTransferStatusReason());
        assertEquals(type, result.getType());
        verifyUnpaidCharges(result.getUnpaidChargeData());
    }

    private static void verifyUnpaidCharges(List<UnpaidChargeDataV1> unpaidChargeData) {
        assertEquals(2, unpaidChargeData.size());
        Map<Long, UnpaidChargeDataV1> map = unpaidChargeData.stream()
                .collect(Collectors.toMap(UnpaidChargeDataV1::getChargeId, Function.identity()));
        assertEquals("charge a", map.get(1L).getChargeName());
        assertEquals(new BigDecimal("25.00000"), map.get(1L).getOutstandingAmount());
        assertEquals("charge c", map.get(3L).getChargeName());
        assertEquals(new BigDecimal("12.00000"), map.get(3L).getOutstandingAmount());
    }

    private ExternalAssetOwnerTransfer createExternalAssetOwnerTransfer(ExternalTransferStatus status,
            ExternalTransferSubStatus subStatus) {
        ExternalAssetOwnerTransfer mock = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(mock.getStatus()).thenReturn(status);
        when(mock.getSubStatus()).thenReturn(subStatus);
        when(mock.getId()).thenReturn(123L);
        when(mock.getExternalId()).thenReturn(new ExternalId("456"));
        return mock;
    }

    private ExternalTransferData createTransferData(ExternalTransferStatus status, ExternalTransferSubStatus subStatus) {
        ExternalTransferDataDetails details = new ExternalTransferDataDetails();
        details.setDetailsId(444L);
        details.setTotalOutstanding(new BigDecimal("1108.00000"));
        details.setTotalInterestOutstanding(new BigDecimal("100.00000"));
        details.setTotalPrincipalOutstanding(new BigDecimal("1000.00000"));
        details.setTotalFeeChargesOutstanding(new BigDecimal("5.00000"));
        details.setTotalPenaltyChargesOutstanding(new BigDecimal("3.00000"));
        details.setTotalOverpaid(BigDecimal.ZERO);

        ExternalTransferData data = new ExternalTransferData();
        data.setOwner(new ExternalTransferOwnerData(ASSET_OWNER_EXTERNAL_ID));
        data.setStatus(status);
        data.setSubStatus(subStatus);
        data.setTransferId(123L);
        data.setLoan(new ExternalTransferLoanData(LOAN_ID, LOAN_EXTERNAL_ID));
        data.setEffectiveFrom(LocalDate.of(2023, 6, 10));
        data.setEffectiveTo(LocalDate.of(9999, 12, 31));
        data.setSettlementDate(LocalDate.of(2023, 6, 11));
        data.setPurchasePriceRatio("1.0");
        data.setTransferExternalId(TRANSFER_EXTERNAL_ID.getValue());
        data.setDetails(details);
        return data;
    }

}
