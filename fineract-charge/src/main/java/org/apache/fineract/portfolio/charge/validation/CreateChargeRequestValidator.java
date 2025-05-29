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
package org.apache.fineract.portfolio.charge.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.charge.data.CreateChargeRequest;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

public class CreateChargeRequestValidator implements ConstraintValidator<CreateChargeValidation, CreateChargeRequest> {

    private static final String MONTH_DAY_FORMAT = "dd MMM";
    private static final String BASE_MESSAGE_KEY = "org.apache.fineract.portfolio.charge.data.create.charge.request";

    // Message keys
    public static final String CHARGE_TIME_TYPE_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeTimeType}";
    public static final String FREQUENCY_INVALID = "{" + BASE_MESSAGE_KEY + ".frequency}";
    public static final String PAYMENT_TYPE_ID_INVALID = "{" + BASE_MESSAGE_KEY + ".paymentTypeId}";
    public static final String INCOME_ACCOUNT_ID_INVALID = "{" + BASE_MESSAGE_KEY + ".incomeAccountId}";
    public static final String CHARGE_PAYMENT_MODE_INVALID = "{" + BASE_MESSAGE_KEY + ".chargePaymentMode}";
    public static final String CHARGE_CALCULATION_TYPE_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeCalculationType.validation}";
    public static final String SHARE_ACCOUNT_ACTIVATION_INVALID = "{" + BASE_MESSAGE_KEY
            + ".validation.chargeCalculationType.shareAccountActivation}";
    public static final String TRANCHE_DISBURSEMENT_INVALID = "{" + BASE_MESSAGE_KEY
            + ".validation.chargeCalculationType.TrancheDisbursement}";
    public static final String PERCENT_OF_DISBURSEMENT_INVALID = "{" + BASE_MESSAGE_KEY
            + ".validation.chargeCalculationType.PercentOfDisbursement}";
    public static final String SAVING_VALUE_INVALID = "{" + BASE_MESSAGE_KEY + ".validation.chargeTimeType.savingValue}";
    public static final String CLIENT_VALUE_INVALID = "{" + BASE_MESSAGE_KEY + ".validation.chargeTimeType.clientValue}";
    public static final String SHARE_VALUE_INVALID = "{" + BASE_MESSAGE_KEY + ".validation.chargeTimeType.shareValue}";
    public static final String CHARGE_CALCULATION_TYPE_SAVING_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeCalculationType.savingValue}";
    public static final String CHARGE_CALCULATION_TYPE_CLIENT_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeCalculationType.clientValue}";
    public static final String CHARGE_CALCULATION_TYPE_SHARE_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeCalculationType.shareValue}";
    public static final String WEEKLY_FEE_INVALID = "{" + BASE_MESSAGE_KEY + ".validation.chargeTimeType.weeklyFee}";
    public static final String FEE_ON_MONTH_DAY_REQUIRED = "{" + BASE_MESSAGE_KEY + ".feeOnMonthDay.notBlank}";
    public static final String FEE_INTERVAL_INVALID = "{" + BASE_MESSAGE_KEY + ".feeInterval.minMax.validation}";

    @Override
    public boolean isValid(CreateChargeRequest request, ConstraintValidatorContext context) {
        if (Boolean.TRUE.equals(request.getEnableFreeWithdrawalCharge())) {
            boolean isValidFrequency = MathUtil.isGreaterThanZero(request.getFreeWithdrawalFrequency())
                    && MathUtil.isGreaterThanZero(request.getRestartCountFrequency());

            if (!isValidFrequency) {
                addViolation(context, FREQUENCY_INVALID);
                return false;
            }
        }

        if (Boolean.TRUE.equals(request.getEnablePaymentType())) {
            if (!MathUtil.isGreaterThanZero(request.getPaymentTypeId())) {
                addViolation(context, PAYMENT_TYPE_ID_INVALID);
                return false;
            }
        }

        final ChargeAppliesTo appliesTo = ChargeAppliesTo.fromInt(request.getChargeAppliesTo());
        final Integer chargeTimeType = request.getChargeTimeType();
        final Locale defaultLocale = Optional.ofNullable(request.getLocale()).map(LocaleUtils::toLocale).orElse(Locale.getDefault());
        final String datePattern = StringUtils.isBlank(request.getMonthDayFormat()) ? MONTH_DAY_FORMAT : request.getMonthDayFormat();

        final DateTimeFormatter formatterDate = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                .appendPattern(datePattern).toFormatter(defaultLocale).withResolverStyle(ResolverStyle.STRICT);

        final Integer chargeCalculationType = request.getChargeCalculationType();

        if (appliesTo.isLoanCharge()) {
            if (!MathUtil.isGreaterThanZero(chargeTimeType)) {
                addViolation(context, CHARGE_TIME_TYPE_INVALID);
                return false;
            }

            if (!Arrays.asList(ChargePaymentMode.validValues()).contains(request.getChargePaymentMode())) {
                addViolation(context, CHARGE_PAYMENT_MODE_INVALID);
                return false;
            }

            if (!Arrays.asList(ChargeCalculationType.validValuesForLoan()).contains(chargeCalculationType)) {
                addViolation(context, CHARGE_CALCULATION_TYPE_INVALID);
                return false;
            }

            if (chargeCalculationType != null) {
                if (ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue().equals(chargeTimeType)) {
                    if (!Arrays.asList(ChargeCalculationType.validValuesForShareAccountActivation()).contains(chargeCalculationType)) {
                        addViolation(context, SHARE_ACCOUNT_ACTIVATION_INVALID);
                        return false;
                    }
                }

                if (ChargeTimeType.TRANCHE_DISBURSEMENT.getValue().equals(chargeTimeType)) {
                    if (!Arrays.asList(ChargeCalculationType.validValuesForTrancheDisbursement()).contains(chargeCalculationType)) {
                        addViolation(context, TRANCHE_DISBURSEMENT_INVALID);
                        return false;
                    }
                } else if (ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue().equals(chargeCalculationType)) {
                    addViolation(context, PERCENT_OF_DISBURSEMENT_INVALID);
                    return false;
                }
            }

        } else if (appliesTo.isSavingsCharge()) {
            if (!Arrays.asList(ChargeTimeType.validSavingsValues()).contains(chargeTimeType)) {
                addViolation(context, SAVING_VALUE_INVALID);
                return false;
            }

            final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);
            String monthDayStr = request.getFeeOnMonthDay();

            if (ctt.isWeeklyFee()) {
                boolean isValidMonthDay = StringUtils.isBlank(monthDayStr) && StringUtils.isNotBlank(chargeTimeType.toString());

                if (!isValidMonthDay) {
                    addViolation(context, WEEKLY_FEE_INVALID);
                    return false;
                }
            }

            if (ctt.isMonthlyFee()) {
                if (StringUtils.isBlank(monthDayStr)) {
                    addViolation(context, FEE_ON_MONTH_DAY_REQUIRED);
                    return false;
                }

                validateParseMonthDay(monthDayStr, formatterDate, defaultLocale);

                if (!MathUtil.isMinAndMax(request.getFeeInterval(), 1, 12)) {
                    addViolation(context, FEE_INTERVAL_INVALID);
                    return false;
                }
            }

            if (ctt.isAnnualFee()) {
                if (StringUtils.isBlank(monthDayStr)) {
                    addViolation(context, FEE_ON_MONTH_DAY_REQUIRED);
                    return false;
                }

                validateParseMonthDay(monthDayStr, formatterDate, defaultLocale);
            }

            if (!Arrays.asList(ChargeCalculationType.validValuesForSavings()).contains(chargeCalculationType)) {
                addViolation(context, CHARGE_CALCULATION_TYPE_SAVING_INVALID);
                return false;
            }

        } else if (appliesTo.isClientCharge()) {
            if (!Arrays.asList(ChargeTimeType.validClientValues()).contains(chargeTimeType)) {
                addViolation(context, CLIENT_VALUE_INVALID);
                return false;
            }

            if (!Arrays.asList(ChargeCalculationType.validValuesForClients()).contains(chargeCalculationType)) {
                addViolation(context, CHARGE_CALCULATION_TYPE_CLIENT_INVALID);
                return false;
            }

            Long incomeAccountId = request.getIncomeAccountId();
            if (incomeAccountId != null && !MathUtil.isGreaterThanZero(incomeAccountId)) {
                addViolation(context, INCOME_ACCOUNT_ID_INVALID);
                return false;
            }

        } else if (appliesTo.isSharesCharge()) {
            if (!Arrays.asList(ChargeTimeType.validShareValues()).contains(chargeTimeType)) {
                addViolation(context, SHARE_VALUE_INVALID);
                return false;
            }

            if (!Arrays.asList(ChargeCalculationType.validValuesForShares()).contains(chargeCalculationType)) {
                addViolation(context, CHARGE_CALCULATION_TYPE_SHARE_INVALID);
                return false;
            }

            if (ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue().equals(chargeTimeType) && chargeCalculationType != null
                    && !Arrays.asList(ChargeCalculationType.validValuesForShareAccountActivation()).contains(chargeCalculationType)) {
                addViolation(context, SHARE_ACCOUNT_ACTIVATION_INVALID);
                return false;
            }
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext ctx, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private void validateParseMonthDay(String monthDayStr, DateTimeFormatter formatterDate, Locale defaultLocale) {
        try {
            MonthDay.parse(monthDayStr, formatterDate);
        } catch (DateTimeParseException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError(
                    "validation.msg.invalid.month.day", "The parameter `feeOnMonthDay` is invalid based on the monthDayFormat: `"
                            + formatterDate + "` and locale: `" + defaultLocale + "` provided:",
                    "feeOnMonthDay", monthDayStr, formatterDate);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }
}
