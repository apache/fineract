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
package org.apache.fineract.portfolio.charge.service;

import static org.apache.fineract.portfolio.charge.domain.Charge.CHARGE_CALCULATION_TYPE_PARAM_NAME;
import static org.apache.fineract.portfolio.charge.domain.Charge.CHARGE_TIME_PARAM_NAME;
import static org.apache.fineract.portfolio.charge.domain.Charge.FEE_FREQUENCY_PARAM_NAME;
import static org.apache.fineract.portfolio.charge.domain.Charge.FEE_INTERVAL_PARAM_NAME;
import static org.apache.fineract.portfolio.charge.domain.Charge.FEE_ON_MONTH_DAY_PARAM_NAME;
import static org.apache.fineract.portfolio.charge.domain.Charge.LOCALE_PARAM_NAME;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.portfolio.charge.api.ChargesApiConstants;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeCreateResponse;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteRequest;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteResponse;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateResponse;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeMustBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.apache.fineract.portfolio.charge.exception.ChargeParameterUpdateNotSupportedException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.exception.PaymentTypeNotFoundException;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ChargeWriteServiceImpl implements ChargeWriteService {

    private static final String NAME_PARAM_NAME = "name";
    private static final String CURRENCY_CODE_PARAM_NAME = "currencyCode";
    private static final String AMOUNT_PARAM_NAME = "amount";
    private static final String PENALTY_PARAM_NAME = "penalty";
    private static final String ACTIVE_PARAM_NAME = "active";
    private static final String MIN_CAP_PARAM_NAME = "minCap";
    private static final String MAX_CAP_PARAM_NAME = "maxCap";
    private static final String CHARGE_PAYMENT_MODE_PARAM_NAME = "chargePaymentMode";
    private static final String PAYMENT_TYPE_ID_PARAM_NAME = "paymentTypeId";
    private static final String ENABLE_PAYMENT_TYPE_PARAM_NAME = "enablePaymentType";
    private static final String ENABLE_FREE_WITHDRAWAL_PARAM_NAME = "enableFreeWithdrawalCharge";
    private static final String FREE_WITHDRAWAL_FREQUENCY_PARAM_NAME = "freeWithdrawalFrequency";
    private static final String RESTART_COUNT_FREQUENCY_PARAM_NAME = "restartCountFrequency";
    private static final String COUNT_FREQUENCY_TYPE_PARAM_NAME = "countFrequencyType";

    private final Validator validator;
    private final ChargeRepository chargeRepository;
    private final LoanProductRepository loanProductRepository;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final GLAccountRepositoryWrapper glAccountRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepository;
    private final PaymentTypeRepository paymentTypeRepository;

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public ChargeCreateResponse create(final ChargeCreateRequest request) {
        try {
            validate(request);

            TaxGroup taxGroup = null;
            if (request.getTaxGroupId() != null) {
                taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(request.getTaxGroupId());
            }

            PaymentType paymentType = null;
            if (Boolean.TRUE.equals(request.getEnablePaymentType()) && request.getPaymentTypeId() != null) {
                paymentType = findPaymentTypeWithNotFoundDetection(request.getPaymentTypeId());
            }

            final Charge charge = buildCharge(request, taxGroup, paymentType);
            this.chargeRepository.saveAndFlush(charge);

            // check if the office specific products are enabled. If yes, then save this charge against a specific
            // office i.e. this charge is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES, charge.getId());

            return ChargeCreateResponse.builder().resourceId(charge.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return ChargeCreateResponse.builder().build();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return ChargeCreateResponse.builder().build();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public ChargeUpdateResponse update(final ChargeUpdateRequest request) {
        try {
            final Long chargeId = request.getId();
            final Charge chargeForUpdate = this.chargeRepository.findById(chargeId)
                    .orElseThrow(() -> new ChargeNotFoundException(chargeId));
            validate(request);

            final Map<String, Object> changes = applyChanges(chargeForUpdate, request);

            // MIFOSX-900: Check if the Charge has been active before and now is deactivated:
            if (changes.containsKey(ACTIVE_PARAM_NAME)) {
                // IF the key exists then it has changed (otherwise it would have been filtered), so check current
                // state:
                if (!chargeForUpdate.isActive()) {
                    final boolean isChargeExistWithLoans = chargeRepository.isAnyLoanProductAssociatedWithCharge(chargeId);
                    final boolean isChargeExistWithSavings = chargeRepository.isAnySavingsProductAssociatedWithCharge(chargeId);

                    if (isChargeExistWithLoans || isChargeExistWithSavings) {
                        throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.it.is.used.in.loan",
                                "This charge cannot be updated, it is used in loan");
                    }
                }
            } else if ((changes.containsKey(FEE_FREQUENCY_PARAM_NAME) || changes.containsKey(FEE_INTERVAL_PARAM_NAME))
                    && chargeForUpdate.isLoanCharge()) {
                if (chargeRepository.isAnyLoanProductAssociatedWithCharge(chargeId)) {
                    throw new ChargeCannotBeUpdatedException("error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan",
                            "This charge frequency cannot be updated, it is used in loan");
                }
            }

            // Has account Id been changed ?
            if (changes.containsKey(ChargesApiConstants.glAccountIdParamName)) {
                final Long newValue = request.getIncomeAccountId();
                GLAccount newIncomeAccount = null;
                if (newValue != null) {
                    newIncomeAccount = this.glAccountRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setAccount(newIncomeAccount);
            }

            if (changes.containsKey(PAYMENT_TYPE_ID_PARAM_NAME) && request.getPaymentTypeId() != null) {
                chargeForUpdate.setPaymentType(findPaymentTypeWithNotFoundDetection(request.getPaymentTypeId()));
            }

            if (changes.containsKey(ChargesApiConstants.taxGroupIdParamName)) {
                final Long newValue = request.getTaxGroupId();
                TaxGroup taxGroup = null;
                if (newValue != null) {
                    taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setTaxGroup(taxGroup);
            }

            if (!changes.isEmpty()) {
                this.chargeRepository.save(chargeForUpdate);
            }

            return ChargeUpdateResponse.builder().resourceId(chargeId).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return ChargeUpdateResponse.builder().build();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return ChargeUpdateResponse.builder().build();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public ChargeDeleteResponse delete(final ChargeDeleteRequest request) {
        final Long chargeId = request.getId();
        final Charge chargeForDelete = this.chargeRepository.findById(chargeId).orElseThrow(() -> new ChargeNotFoundException(chargeId));
        if (chargeForDelete.isDeleted()) {
            throw new ChargeNotFoundException(chargeId);
        }

        final Collection<LoanProduct> loanProducts = this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId);
        final boolean isChargeExistWithLoans = chargeRepository.isAnyActiveLoanChargeAssociatedWithCharge(chargeId);
        final boolean isChargeExistWithSavings = chargeRepository.isAnyActiveSavingsAccountChargeAssociatedWithCharge(chargeId);
        final boolean isChargeExistWithWorkingCapitalLoan = chargeRepository.isAnyWorkingCapitalLoansAssociateWithThisCharge(chargeId)
                .isPresent();

        // TODO: Change error messages around:
        if (!loanProducts.isEmpty() || isChargeExistWithLoans || isChargeExistWithSavings || isChargeExistWithWorkingCapitalLoan) {
            throw new ChargeCannotBeDeletedException("error.msg.charge.cannot.be.deleted.it.is.already.used.in.loan",
                    "This charge cannot be deleted, it is already used in loan");
        }

        chargeForDelete.delete();

        this.chargeRepository.save(chargeForDelete);

        return ChargeDeleteResponse.builder().resourceId(chargeForDelete.getId()).build();
    }

    private <T> void validate(final T request) {
        final Set<ConstraintViolation<T>> violations = this.validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private Charge buildCharge(final ChargeCreateRequest request, final TaxGroup taxGroup, final PaymentType paymentType) {
        final ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(request.getChargeAppliesTo());
        final ChargePaymentMode paymentMode = request.getChargePaymentMode() == null
                ? (chargeAppliesTo.isWorkingCapitalLoanCharge() ? ChargePaymentMode.REGULAR : null)
                : ChargePaymentMode.fromInt(request.getChargePaymentMode());
        final BigDecimal amount = request.getAmount() == null ? null : BigDecimal.valueOf(request.getAmount());

        // the create contract exposes neither an income account nor the free-withdrawal settings
        return new Charge(request.getName(), amount, request.getCurrencyCode(), chargeAppliesTo,
                ChargeTimeType.fromInt(request.getChargeTimeType()), ChargeCalculationType.fromInt(request.getChargeCalculationType()),
                Boolean.TRUE.equals(request.getPenalty()), Boolean.TRUE.equals(request.getActive()), paymentMode,
                request.feeOnMonthDayAsMonthDay(), request.getFeeInterval(), request.getMinCap(), request.getMaxCap(),
                request.getFeeFrequency(), false, null, null, null, null, taxGroup, Boolean.TRUE.equals(request.getEnablePaymentType()),
                paymentType);
    }

    /**
     * Applies the (partial) update request to the charge, recording every value that actually changed. Mirrors the
     * PATCH semantics of the legacy {@code Charge.update(JsonCommand)}: a field is only touched when it is present in
     * the request AND differs from the persisted value.
     */
    private Map<String, Object> applyChanges(final Charge charge, final ChargeUpdateRequest request) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);
        final String localeAsInput = localeLanguage(request);

        if (isChanged(request.getName(), charge.getName())) {
            charge.setName(request.getName());
            actualChanges.put(NAME_PARAM_NAME, request.getName());
        }

        if (isChanged(request.getCurrencyCode(), charge.getCurrencyCode())) {
            charge.setCurrencyCode(request.getCurrencyCode());
            actualChanges.put(CURRENCY_CODE_PARAM_NAME, request.getCurrencyCode());
        }

        final BigDecimal amount = request.getAmount() == null ? null : BigDecimal.valueOf(request.getAmount());
        if (isChangedAmount(amount, charge.getAmount())) {
            charge.setAmount(amount);
            actualChanges.put(AMOUNT_PARAM_NAME, amount);
            putLocale(actualChanges, localeAsInput);
        }

        if (isChanged(request.getChargeTimeType(), charge.getChargeTimeType())) {
            charge.setChargeTimeType(ChargeTimeType.fromInt(request.getChargeTimeType()).getValue());
            actualChanges.put(CHARGE_TIME_PARAM_NAME, request.getChargeTimeType());
            putLocale(actualChanges, localeAsInput);
        }

        if (isChanged(request.getFreeWithdrawalFrequency(), charge.getFreeWithdrawalFrequency())) {
            charge.setFreeWithdrawalFrequency(request.getFreeWithdrawalFrequency());
            actualChanges.put(FREE_WITHDRAWAL_FREQUENCY_PARAM_NAME, request.getFreeWithdrawalFrequency());
        }

        if (isChanged(request.getRestartCountFrequency(), charge.getRestartFrequency())) {
            charge.setRestartFrequency(request.getRestartCountFrequency());
            actualChanges.put(RESTART_COUNT_FREQUENCY_PARAM_NAME, request.getRestartCountFrequency());
        }

        if (isChanged(request.getCountFrequencyType(), charge.getRestartFrequencyEnum())) {
            // NOTE: the legacy update path resolved this through ChargeTimeType (not PeriodFrequencyType); kept as-is
            charge.setRestartFrequencyEnum(ChargeTimeType.fromInt(request.getCountFrequencyType()).getValue());
            actualChanges.put(COUNT_FREQUENCY_TYPE_PARAM_NAME, request.getCountFrequencyType());
        }

        if (isChanged(request.getEnableFreeWithdrawalCharge(), charge.isEnableFreeWithdrawal())) {
            charge.setEnableFreeWithdrawal(request.getEnableFreeWithdrawalCharge());
            actualChanges.put(ENABLE_FREE_WITHDRAWAL_PARAM_NAME, request.getEnableFreeWithdrawalCharge());
        }

        if (isChanged(request.getEnablePaymentType(), charge.isEnablePaymentType())) {
            charge.setEnablePaymentType(request.getEnablePaymentType());
            actualChanges.put(ENABLE_PAYMENT_TYPE_PARAM_NAME, request.getEnablePaymentType());
        }

        if (isChanged(request.getPaymentTypeId(), charge.getPaymentTypeId())) {
            // the payment type itself is resolved and set by the caller
            actualChanges.put(PAYMENT_TYPE_ID_PARAM_NAME, request.getPaymentTypeId());
        }

        if (isChanged(request.getChargeAppliesTo(), charge.getChargeAppliesTo())) {
            // AA: Do not allow to change chargeAppliesTo.
            throw new ChargeParameterUpdateNotSupportedException("charge.applies.to", "Update of Charge applies to is not supported");
        }

        if (isChanged(request.getChargeCalculationType(), charge.getChargeCalculation())) {
            charge.setChargeCalculation(ChargeCalculationType.fromInt(request.getChargeCalculationType()).getValue());
            actualChanges.put(CHARGE_CALCULATION_TYPE_PARAM_NAME, request.getChargeCalculationType());
            putLocale(actualChanges, localeAsInput);
        }

        // charge payment mode is only meaningful for loan charges
        if (charge.isLoanCharge() && isChanged(request.getChargePaymentMode(), charge.getChargePaymentMode())) {
            charge.setChargePaymentMode(ChargePaymentMode.fromInt(request.getChargePaymentMode()).getValue());
            actualChanges.put(CHARGE_PAYMENT_MODE_PARAM_NAME, request.getChargePaymentMode());
            putLocale(actualChanges, localeAsInput);
        }

        applyFeeOnMonthDayChange(charge, request, actualChanges, localeAsInput);

        if (isChanged(request.getFeeInterval(), charge.getFeeInterval())) {
            charge.setFeeInterval(request.getFeeInterval());
            actualChanges.put(FEE_INTERVAL_PARAM_NAME, request.getFeeInterval());
            putLocale(actualChanges, localeAsInput);
        }

        if (isChanged(request.getFeeFrequency(), charge.getFeeFrequency())) {
            charge.setFeeFrequency(request.getFeeFrequency());
            actualChanges.put(FEE_FREQUENCY_PARAM_NAME, request.getFeeFrequency());
            putLocale(actualChanges, localeAsInput);
        }

        if (isChanged(request.getPenalty(), charge.isPenalty())) {
            charge.setPenalty(request.getPenalty());
            actualChanges.put(PENALTY_PARAM_NAME, request.getPenalty());
        }

        if (isChanged(request.getActive(), charge.isActive())) {
            charge.setActive(request.getActive());
            actualChanges.put(ACTIVE_PARAM_NAME, request.getActive());
        }

        // allow min and max cap to be only added to PERCENT_OF_AMOUNT for now
        if (charge.isPercentageOfApprovedAmount()) {
            if (isChangedAmount(request.getMinCap(), charge.getMinCap())) {
                charge.setMinCap(request.getMinCap());
                actualChanges.put(MIN_CAP_PARAM_NAME, request.getMinCap());
                putLocale(actualChanges, localeAsInput);
            }
            if (isChangedAmount(request.getMaxCap(), charge.getMaxCap())) {
                charge.setMaxCap(request.getMaxCap());
                actualChanges.put(MAX_CAP_PARAM_NAME, request.getMaxCap());
                putLocale(actualChanges, localeAsInput);
            }
        }

        if (charge.isPenalty() && ChargeTimeType.fromInt(charge.getChargeTimeType()).isTimeOfDisbursement()) {
            throw new ChargeDueAtDisbursementCannotBePenaltyException(charge.getName());
        }
        if (!charge.isPenalty() && ChargeTimeType.fromInt(charge.getChargeTimeType()).isOverdueInstallment()) {
            throw new ChargeMustBePenaltyException(charge.getName());
        }

        if (isChanged(request.getIncomeAccountId(), charge.getIncomeAccountId())) {
            // the account itself is resolved and set by the caller
            actualChanges.put(ChargesApiConstants.glAccountIdParamName, request.getIncomeAccountId());
        }

        if (isChanged(request.getTaxGroupId(), charge.getTaxGroupId())) {
            // the tax group itself is resolved and set by the caller
            actualChanges.put(ChargesApiConstants.taxGroupIdParamName, request.getTaxGroupId());
        }

        return actualChanges;
    }

    private void applyFeeOnMonthDayChange(final Charge charge, final ChargeUpdateRequest request, final Map<String, Object> actualChanges,
            final String localeAsInput) {
        if (StringUtils.isBlank(request.getFeeOnMonthDay())) {
            return;
        }
        final MonthDay monthDay = request.feeOnMonthDayAsMonthDay();
        if (monthDay == null) {
            return;
        }
        if (!Objects.equals(charge.getFeeOnDay(), monthDay.getDayOfMonth())) {
            charge.setFeeOnDay(monthDay.getDayOfMonth());
            actualChanges.put(FEE_ON_MONTH_DAY_PARAM_NAME, request.getFeeOnMonthDay());
            putLocale(actualChanges, localeAsInput);
        }
        if (!Objects.equals(charge.getFeeOnMonth(), monthDay.getMonthValue())) {
            charge.setFeeOnMonth(monthDay.getMonthValue());
            actualChanges.put(FEE_ON_MONTH_DAY_PARAM_NAME, request.getFeeOnMonthDay());
            putLocale(actualChanges, localeAsInput);
        }
    }

    private static void putLocale(final Map<String, Object> actualChanges, final String localeAsInput) {
        if (localeAsInput != null) {
            actualChanges.put(LOCALE_PARAM_NAME, localeAsInput);
        }
    }

    private static String localeLanguage(final ChargeUpdateRequest request) {
        return StringUtils.isBlank(request.getLocale()) ? null : LocaleUtils.toLocale(request.getLocale()).getLanguage();
    }

    private static boolean isChanged(final Object requestValue, final Object currentValue) {
        return requestValue != null && !Objects.equals(requestValue, currentValue);
    }

    /**
     * Amounts must be compared by value, not by scale: {@code 10} and {@code 10.00} are the same amount.
     */
    private static boolean isChangedAmount(final BigDecimal requestValue, final BigDecimal currentValue) {
        return requestValue != null && (currentValue == null || requestValue.compareTo(currentValue) != 0);
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final String name, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains(NAME_PARAM_NAME)) {
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name `" + name + "` already exists",
                    NAME_PARAM_NAME, name);
        }

        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private PaymentType findPaymentTypeWithNotFoundDetection(final Long paymentTypeId) {
        return this.paymentTypeRepository.findById(paymentTypeId).orElseThrow(() -> new PaymentTypeNotFoundException(paymentTypeId));
    }

}
