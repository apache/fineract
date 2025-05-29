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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.charge.data.UpdateChargeRequest;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

public class UpdateChargeRequestValidator implements ConstraintValidator<UpdateChargeValidation, UpdateChargeRequest> {

    private static final String BASE_MESSAGE_KEY = "org.apache.fineract.portfolio.charge.data.update.charge.request";

    // Message keys
    public static final String CHARGE_APPLIES_TO_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeAppliesTo}";
    public static final String FREQUENCY_INVALID = "{" + BASE_MESSAGE_KEY + ".frequency}";
    public static final String PAYMENT_TYPE_ID_INVALID = "{" + BASE_MESSAGE_KEY + ".paymentTypeId}";
    public static final String CHARGE_TIME_TYPE_INVALID = "{" + BASE_MESSAGE_KEY + ".chargeTimeType}";

    @Override
    public boolean isValid(UpdateChargeRequest request, ConstraintValidatorContext context) {
        boolean isValidChargeApplies = Arrays.asList(ChargeAppliesTo.validValues()).contains(request.getChargeAppliesTo());

        if (request.getChargeAppliesTo() != null && !isValidChargeApplies) {
            addViolation(context, CHARGE_APPLIES_TO_INVALID);
            return false;
        }

        if (Boolean.TRUE.equals(request.getEnableFreeWithdrawalCharge())) {
            boolean isValidFrequency = MathUtil.isGreaterThanZero(request.getFreeWithdrawalFrequency())
                    && MathUtil.isGreaterThanZero(request.getRestartCountFrequency());

            if (!isValidFrequency) {
                addViolation(context, FREQUENCY_INVALID);
                return false;
            }

            if (Boolean.TRUE.equals(request.getEnablePaymentType())) {
                if (!MathUtil.isGreaterThanZero(request.getPaymentTypeId())) {
                    addViolation(context, PAYMENT_TYPE_ID_INVALID);
                    return false;
                }
            }
        }

        boolean isValidChargeTimeType = Optional.ofNullable(request.getChargeTimeType())
                .map(chargeTimeType -> Stream.of(ChargeTimeType.validLoanValues(), ChargeTimeType.validSavingsValues(),
                        ChargeTimeType.validClientValues(), ChargeTimeType.validShareValues()).flatMap(Arrays::stream)
                        .anyMatch(value -> value.equals(chargeTimeType)))
                .orElse(false);

        if (request.getChargeTimeType() != null && !isValidChargeTimeType) {
            addViolation(context, CHARGE_TIME_TYPE_INVALID);
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext ctx, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
