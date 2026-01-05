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

import static org.apache.fineract.infrastructure.core.service.DateUtils.DEFAULT_DATE_FORMATTER;
import static org.apache.fineract.investor.data.ExternalTransferStatus.ACTIVE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.ACTIVE_INTERMEDIATE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.BUYBACK;
import static org.apache.fineract.investor.data.ExternalTransferStatus.BUYBACK_INTERMEDIATE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.CANCELLED;
import static org.apache.fineract.investor.data.ExternalTransferStatus.DECLINED;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING_INTERMEDIATE;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.generic.v1.CurrencyDataV1;
import org.apache.fineract.avro.loan.v1.LoanOwnershipTransferDataV1;
import org.apache.fineract.avro.loan.v1.UnpaidChargeDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.AbstractBusinessEventWithCustomDataSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.InvestorBusinessEvent;
import org.apache.fineract.investor.service.ExternalAssetOwnersReadService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestorBusinessEventSerializer extends AbstractBusinessEventWithCustomDataSerializer<InvestorBusinessEvent> {

    private static final Set<ExternalTransferStatus> EXECUTED_TRANSFER_STATUSES = Set.of(ACTIVE, ACTIVE_INTERMEDIATE, BUYBACK,
            BUYBACK_INTERMEDIATE);

    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final List<ExternalEventCustomDataSerializer<InvestorBusinessEvent>> externalEventCustomDataSerializers;

    private static CurrencyDataV1 getCurrencyFromEvent(InvestorBusinessEvent event) {
        MonetaryCurrency loanCurrency = event.getLoan().getCurrency();
        CurrencyDataV1 currency = CurrencyDataV1.newBuilder().setCode(loanCurrency.getCode())
                .setDecimalPlaces(loanCurrency.getDigitsAfterDecimal()).setInMultiplesOf(loanCurrency.getInMultiplesOf()).build();
        return currency;
    }

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof InvestorBusinessEvent;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanOwnershipTransferDataV1.class;
    }

    @Override
    protected List<ExternalEventCustomDataSerializer<InvestorBusinessEvent>> getExternalEventCustomDataSerializers() {
        return externalEventCustomDataSerializers;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        InvestorBusinessEvent event = (InvestorBusinessEvent) rawEvent;
        ExternalTransferData transferData = externalAssetOwnersReadService.retrieveTransferData(event.get().getId());
        String transferType = getType(transferData.getStatus());
        if (ExternalTransferStatus.DECLINED.equals(transferData.getStatus()) || CANCELLED.equals(transferData.getStatus())) {
            ExternalTransferData originalTransferData = externalAssetOwnersReadService
                    .retrieveFirstTransferByExternalId(event.get().getExternalId());
            transferType = getType(originalTransferData.getStatus());
        }

        LoanOwnershipTransferDataV1.Builder builder = LoanOwnershipTransferDataV1.newBuilder().setLoanId(transferData.getLoan().getLoanId())
                .setLoanExternalId(transferData.getLoan().getExternalId()).setTransferExternalId(transferData.getTransferExternalId())
                .setAssetOwnerExternalId(transferData.getOwner().getExternalId())
                .setPreviousOwnerExternalId(
                        transferData.getPreviousOwner() != null ? transferData.getPreviousOwner().getExternalId() : null)
                .setTransferExternalGroupId(transferData.getTransferExternalGroupId())
                .setPurchasePriceRatio(transferData.getPurchasePriceRatio()).setCurrency(getCurrencyFromEvent(event))
                .setSettlementDate(transferData.getSettlementDate().format(DEFAULT_DATE_FORMATTER))
                .setSubmittedDate(transferData.getSettlementDate().format(DEFAULT_DATE_FORMATTER)).setType(transferType)
                .setTransferStatus(getStatus(transferData.getStatus()))
                .setTransferStatusReason(getTransferStatusReason(transferData.getSubStatus())).setCustomData(collectCustomData(event));

        if (transferData.getDetails() != null) {
            builder.setTotalOutstandingBalanceAmount(transferData.getDetails().getTotalOutstanding())
                    .setOutstandingPrincipalPortion(transferData.getDetails().getTotalPrincipalOutstanding())
                    .setOutstandingInterestPortion(transferData.getDetails().getTotalInterestOutstanding())
                    .setOutstandingFeePortion(transferData.getDetails().getTotalFeeChargesOutstanding())
                    .setOutstandingPenaltyPortion(transferData.getDetails().getTotalPenaltyChargesOutstanding())
                    .setUnpaidChargeData(getUnpaidChargeData(event)).setOverPaymentPortion(transferData.getDetails().getTotalOverpaid());
        }

        return builder.build();
    }

    @NonNull
    private static String getType(ExternalTransferStatus transferStatus) {
        if (transferStatus == BUYBACK || transferStatus == BUYBACK_INTERMEDIATE) {
            return "BUYBACK";
        }

        if (transferStatus == ACTIVE_INTERMEDIATE || transferStatus == PENDING_INTERMEDIATE) {
            return "INTERMEDIARYSALE";
        }

        return "SALE";
    }

    private List<UnpaidChargeDataV1> getUnpaidChargeData(InvestorBusinessEvent event) {
        java.util.Map<Long, UnpaidChargeDataV1> map = new HashMap<>();
        event.getLoan().getLoanCharges().forEach(loanCharge -> addToMap(map, loanCharge));
        return map.values().stream().toList();
    }

    private void addToMap(Map<Long, UnpaidChargeDataV1> map, LoanCharge loanCharge) {
        if (loanCharge.amountOutstanding().compareTo(BigDecimal.ZERO) > 0) {
            UnpaidChargeDataV1 toAdd = new UnpaidChargeDataV1(loanCharge.getCharge().getId(), loanCharge.name(),
                    loanCharge.amountOutstanding());
            UnpaidChargeDataV1 unpaidChargeDataV1 = map.get(loanCharge.getCharge().getId());
            if (unpaidChargeDataV1 == null) {
                map.put(toAdd.getChargeId(), toAdd);
            } else {
                unpaidChargeDataV1.setOutstandingAmount(unpaidChargeDataV1.getOutstandingAmount().add(toAdd.getOutstandingAmount()));
            }
        }
    }

    private String getStatus(ExternalTransferStatus status) {
        if (EXECUTED_TRANSFER_STATUSES.contains(status)) {
            return "EXECUTED";
        } else if (DECLINED.equals(status) || CANCELLED.equals(status)) {
            return status.name();
        } else {
            return "UNKNOWN";
        }
    }

    private String getTransferStatusReason(ExternalTransferSubStatus subStatus) {
        if (subStatus != null) {
            return subStatus.name();
        } else {
            return null;
        }
    }
}
