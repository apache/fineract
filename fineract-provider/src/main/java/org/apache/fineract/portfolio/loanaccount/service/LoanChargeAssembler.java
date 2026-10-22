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
package org.apache.fineract.portfolio.loanaccount.service;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWithoutMandatoryFieldException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;

@RequiredArgsConstructor
public class LoanChargeAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanProductRepository loanProductRepository;
    private final ExternalIdFactory externalIdFactory;
    private final LoanChargeService loanChargeService;

    public Set<LoanCharge> fromParsedJson(final JsonElement element, List<LoanDisbursementDetails> disbursementDetails) {
        JsonArray jsonDisbursement = this.fromApiJsonHelper.extractJsonArrayNamed("disbursementData", element);
        List<Long> disbursementChargeIds = new ArrayList<>();

        if (jsonDisbursement != null && !jsonDisbursement.isEmpty()) {
            for (int i = 0; i < jsonDisbursement.size(); i++) {
                final JsonObject jsonObject = jsonDisbursement.get(i).getAsJsonObject();
                if (jsonObject != null && jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName) != null) {
                    String chargeIds = jsonObject.getAsJsonPrimitive(LoanApiConstants.loanChargeIdParameterName).getAsString();
                    if (chargeIds != null) {
                        if (chargeIds.contains(",")) {
                            Iterable<String> chargeId = Splitter.on(',').split(chargeIds);
                            for (String loanChargeId : chargeId) {
                                disbursementChargeIds.add(Long.parseLong(loanChargeId));
                            }
                        } else {
                            disbursementChargeIds.add(Long.parseLong(chargeIds));
                        }
                    }

                }
            }
        }

        final Set<LoanCharge> loanCharges = new HashSet<>();
        final BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);
        final Integer numberOfRepayments = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("numberOfRepayments", element);
        final Long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
        final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                .orElseThrow(() -> new LoanProductNotFoundException(productId));
        final boolean isMultiDisbursal = loanProduct.isMultiDisburseLoan();
        LocalDate expectedDisbursementDate = null;

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long loanChargeId = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType",
                            loanChargeElement, locale);
                    final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed("dueDate", loanChargeElement, dateFormat,
                            locale);
                    final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", loanChargeElement,
                            locale);
                    final String externalIdStr = this.fromApiJsonHelper.extractStringNamed("externalId", loanChargeElement);
                    final ExternalId externalId = externalIdFactory.create(externalIdStr);
                    if (loanChargeId == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        ChargeTimeType chargeTime = null;
                        if (chargeTimeType != null) {
                            chargeTime = ChargeTimeType.fromInt(chargeTimeType);
                        }
                        ChargeCalculationType chargeCalculation = null;
                        if (chargeCalculationType != null) {
                            chargeCalculation = ChargeCalculationType.fromInt(chargeCalculationType);
                        }
                        ChargePaymentMode chargePaymentModeEnum = null;
                        if (chargePaymentMode != null) {
                            chargePaymentModeEnum = ChargePaymentMode.fromInt(chargePaymentMode);
                        }
                        if (!isMultiDisbursal) {
                            if (chargeDefinition.isDownPaymentCharge()) {
                                validateDownPaymentChargeApplicable(chargeDefinition, element, loanProduct);
                                expectedDisbursementDate = this.fromApiJsonHelper
                                        .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                                final BigDecimal percentageAppliedTo = resolveDownPaymentPercentageBase(principal, element, loanProduct);
                                final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, percentageAppliedTo, amount,
                                        chargeTime, chargeCalculation, expectedDisbursementDate, chargePaymentModeEnum, numberOfRepayments,
                                        externalId);
                                loanCharges.add(loanCharge);
                            } else {
                                final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, principal, amount, chargeTime,
                                        chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, externalId);
                                loanCharges.add(loanCharge);
                            }
                        } else {
                            if (topLevelJsonElement.has("disbursementData") && topLevelJsonElement.get("disbursementData").isJsonArray()) {
                                final JsonArray disbursementArray = topLevelJsonElement.get("disbursementData").getAsJsonArray();
                                if (!disbursementArray.isEmpty()) {
                                    JsonObject disbursementDataElement = disbursementArray.get(0).getAsJsonObject();
                                    expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(
                                            LoanApiConstants.expectedDisbursementDateParameterName, disbursementDataElement, dateFormat,
                                            locale);
                                } else {
                                    expectedDisbursementDate = this.fromApiJsonHelper
                                            .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                                }
                            }

                            if (ChargeTimeType.DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                if (CollectionUtils.isEmpty(disbursementDetails)) {
                                    // non-tranche
                                    final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, principal, amount, chargeTime,
                                            chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, externalId);
                                    loanCharges.add(loanCharge);
                                } else {
                                    // tranche
                                    for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                                        LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
                                        if (chargeDefinition.isPercentageOfApprovedAmount() && disbursementDetail
                                                .expectedDisbursementDateAsLocalDate().equals(expectedDisbursementDate)) {
                                            final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, principal, amount,
                                                    chargeTime, chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments,
                                                    externalId);
                                            loanCharges.add(loanCharge);
                                            if (loanCharge.isTrancheDisbursementCharge()) {
                                                loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                                                        disbursementDetail);
                                                loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                            }
                                        } else {
                                            if (disbursementDetail.expectedDisbursementDateAsLocalDate().equals(expectedDisbursementDate)) {
                                                final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition,
                                                        disbursementDetail.getPrincipal(), amount, chargeTime, chargeCalculation,
                                                        disbursementDetail.expectedDisbursementDateAsLocalDate(), chargePaymentModeEnum,
                                                        numberOfRepayments, externalId);
                                                loanCharges.add(loanCharge);
                                                if (loanCharge.isTrancheDisbursementCharge()) {
                                                    loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge,
                                                            disbursementDetail);
                                                    loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = null;
                                for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                                    if (ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().equals(chargeDefinition.getChargeTimeType())) {
                                        final ExternalId chargeExternalId = externalIdFactory.create(externalIdStr);
                                        final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition,
                                                disbursementDetail.getPrincipal(), amount, chargeTime, chargeCalculation,
                                                disbursementDetail.expectedDisbursementDateAsLocalDate(), chargePaymentModeEnum,
                                                numberOfRepayments, chargeExternalId);
                                        loanCharges.add(loanCharge);
                                        loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                                        loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                                    }
                                }
                            } else if (chargeDefinition.isDownPaymentCharge()) {
                                validateDownPaymentChargeApplicable(chargeDefinition, element, loanProduct);
                                // Multi-disburse with disallowExpectedDisbursements has no disbursementData; fall back
                                // to
                                // top-level expectedDisbursementDate (same as non-multi path).
                                if (expectedDisbursementDate == null) {
                                    expectedDisbursementDate = this.fromApiJsonHelper
                                            .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
                                }
                                if (CollectionUtils.isEmpty(disbursementDetails)) {
                                    final BigDecimal percentageAppliedTo = resolveDownPaymentPercentageBase(principal, element,
                                            loanProduct);
                                    final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, percentageAppliedTo, amount,
                                            chargeTime, chargeCalculation, expectedDisbursementDate, chargePaymentModeEnum,
                                            numberOfRepayments, externalId);
                                    loanCharges.add(loanCharge);
                                } else {
                                    ExternalId chargeExternalId = externalId;
                                    for (LoanDisbursementDetails disbursementDetail : disbursementDetails) {
                                        final BigDecimal percentageAppliedTo = resolveDownPaymentPercentageBase(
                                                disbursementDetail.getPrincipal(), element, loanProduct);
                                        final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, percentageAppliedTo, amount,
                                                chargeTime, chargeCalculation, disbursementDetail.expectedDisbursementDateAsLocalDate(),
                                                chargePaymentModeEnum, numberOfRepayments, chargeExternalId);
                                        loanCharge.setDisbursementDetail(disbursementDetail);
                                        loanCharges.add(loanCharge);
                                        chargeExternalId = externalIdFactory.create();
                                    }
                                }
                            } else {
                                final LoanCharge loanCharge = createNewWithoutLoan(chargeDefinition, principal, amount, chargeTime,
                                        chargeCalculation, dueDate, chargePaymentModeEnum, numberOfRepayments, externalId);
                                loanCharges.add(loanCharge);
                            }
                        }
                    } else {
                        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));

                        if (!loanCharge.isTrancheDisbursementCharge() || disbursementChargeIds.contains(loanChargeId)) {
                            loanChargeService.update(loanCharge, amount, dueDate, numberOfRepayments);
                            loanCharges.add(loanCharge);
                        }

                    }
                }
            }
        }

        return loanCharges;
    }

    public Set<Charge> getNewLoanTrancheCharges(final JsonElement element) {
        final Set<Charge> associatedChargesForLoan = new HashSet<>();
        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();
                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
                        if (chargeDefinition.getChargeTimeType().equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
                            associatedChargesForLoan.add(chargeDefinition);
                        }
                    }
                }
            }
        }
        return associatedChargesForLoan;
    }

    public LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition, final JsonCommand command) {
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        if ((chargeDefinition.getChargeTimeType().equals(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                || chargeDefinition.isDownPaymentCharge()) && dueDate == null) {
            final String defaultUserMessage = "Loan charge is missing due date.";
            throw new LoanChargeWithoutMandatoryFieldException("loanCharge", "dueDate", defaultUserMessage, chargeDefinition.getId(),
                    chargeDefinition.getName());
        }
        return createNewFromJson(loan, chargeDefinition, command, dueDate);
    }

    public LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition, final JsonCommand command,
            final LocalDate dueDate) {
        if (chargeDefinition.isDownPaymentCharge()) {
            validateDownPaymentChargeApplicable(chargeDefinition, loan.getLoanRepaymentScheduleDetail().isEnableDownPayment(),
                    loan.getLoanRepaymentScheduleDetail().getLoanScheduleType());
        }

        final LoanDisbursementDetails downPaymentTranche = chargeDefinition.isDownPaymentCharge()
                ? loanChargeService.trancheForNewDownPaymentFee(loan, chargeDefinition, dueDate)
                : null;
        final Locale locale = command.extractLocale();
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount", locale);

        final ChargeTimeType chargeTime = null;
        final ChargeCalculationType chargeCalculation = null;
        final ChargePaymentMode chargePaymentMode = null;
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        switch (ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation())) {
            case PERCENT_OF_AMOUNT:
                if (chargeDefinition.isDownPaymentCharge()) {
                    amountPercentageAppliedTo = loanChargeService.calculateDownPaymentAmountBase(loan, downPaymentTranche);
                } else if (command.hasParameter("principal")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal");
                } else {
                    amountPercentageAppliedTo = loan.getPrincipal().getAmount();
                }
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                if (command.hasParameter("principal") && command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal")
                            .add(command.bigDecimalValueOfParameterNamed("interest"));
                } else {
                    amountPercentageAppliedTo = loan.getPrincipal().getAmount().add(loan.getTotalInterest());
                }
            break;
            case PERCENT_OF_INTEREST:
                if (command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("interest");
                } else {
                    amountPercentageAppliedTo = loan.getTotalInterest();
                }
            break;
            default:
            break;
        }

        BigDecimal loanCharge = BigDecimal.ZERO;
        if (ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType()).equals(ChargeTimeType.INSTALMENT_FEE)) {
            BigDecimal percentage = amount;
            if (percentage == null) {
                percentage = chargeDefinition.getAmount();
            }
            loanCharge = loanChargeService.calculatePerInstallmentChargeAmount(loan,
                    ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation()), percentage);
        }

        // If charge type is specified due date and loan is multi disburment
        // loan.
        // Then we need to get as of this loan charge due date how much amount
        // disbursed.
        if (chargeDefinition.getChargeTimeType().equals(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()) && loan.isMultiDisburmentLoan()) {
            amountPercentageAppliedTo = BigDecimal.ZERO;
            for (final LoanDisbursementDetails loanDisbursementDetails : loan.getDisbursementDetails()) {
                if (!DateUtils.isAfter(loanDisbursementDetails.expectedDisbursementDate(), dueDate)) {
                    amountPercentageAppliedTo = amountPercentageAppliedTo.add(loanDisbursementDetails.getPrincipal());
                }
            }
        }

        ExternalId externalId = externalIdFactory.createFromCommand(command, "externalId");
        final LoanCharge newLoanCharge = loanChargeService.create(loan, chargeDefinition, amountPercentageAppliedTo, amount, chargeTime,
                chargeCalculation, dueDate, chargePaymentMode, null, loanCharge, externalId);
        newLoanCharge.setDisbursementDetail(downPaymentTranche);
        return newLoanCharge;
    }

    /*
     * loanPrincipal is required for charges that are percentage based
     */
    public LoanCharge createNewWithoutLoan(final Charge chargeDefinition, final BigDecimal loanPrincipal, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments, final ExternalId externalId) {
        return loanChargeService.create(null, chargeDefinition, loanPrincipal, amount, chargeTime, chargeCalculation, dueDate,
                chargePaymentMode, numberOfRepayments, BigDecimal.ZERO, externalId);
    }

    /**
     * Loan level down payment settings override the product ones (see LoanScheduleAssembler), so they are resolved from
     * the application JSON first.
     */
    private BigDecimal resolveDownPaymentPercentageBase(final BigDecimal disbursementPrincipal, final JsonElement element,
            final LoanProduct loanProduct) {
        final LoanProductRelatedDetail relatedDetail = loanProduct.getLoanProductRelatedDetail();
        BigDecimal downPaymentPercentage = relatedDetail.getDisbursedAmountPercentageForDownPayment();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element)) {
            downPaymentPercentage = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, element);
        }
        return loanChargeService.calculateDownPaymentAmount(disbursementPrincipal, downPaymentPercentage,
                relatedDetail.getInstallmentAmountInMultiplesOf(), loanProduct.getCurrency());
    }

    private void validateDownPaymentChargeApplicable(final Charge chargeDefinition, final JsonElement element,
            final LoanProduct loanProduct) {
        final LoanProductRelatedDetail relatedDetail = loanProduct.getLoanProductRelatedDetail();
        boolean enableDownPayment = relatedDetail.isEnableDownPayment();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.ENABLE_DOWN_PAYMENT, element)) {
            enableDownPayment = Boolean.TRUE
                    .equals(this.fromApiJsonHelper.extractBooleanNamed(LoanProductConstants.ENABLE_DOWN_PAYMENT, element));
        }
        LoanScheduleType loanScheduleType = relatedDetail.getLoanScheduleType();
        if (this.fromApiJsonHelper.parameterExists(LoanProductConstants.LOAN_SCHEDULE_TYPE, element)) {
            loanScheduleType = LoanScheduleType
                    .valueOf(this.fromApiJsonHelper.extractStringNamed(LoanProductConstants.LOAN_SCHEDULE_TYPE, element));
        }
        validateDownPaymentChargeApplicable(chargeDefinition, enableDownPayment, loanScheduleType);
    }

    private void validateDownPaymentChargeApplicable(final Charge chargeDefinition, final boolean enableDownPayment,
            final LoanScheduleType loanScheduleType) {
        if (!enableDownPayment) {
            final String defaultUserMessage = "Down payment charge cannot be added because down payment is not enabled.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "down.payment.not.enabled", defaultUserMessage, null,
                    chargeDefinition.getName());
        }
        if (!LoanScheduleType.PROGRESSIVE.equals(loanScheduleType)) {
            final String defaultUserMessage = "Down payment charge can be added only to a loan with progressive schedule.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "down.payment.only.progressive", defaultUserMessage, null,
                    chargeDefinition.getName());
        }
    }
}
