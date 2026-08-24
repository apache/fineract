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
package org.apache.fineract.portfolio.floatingrates.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateRequest;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
final class FloatingRateCreateValidator implements ConstraintValidator<ValidFloatingRateCreate, FloatingRateCreateRequest> {

    private final FloatingRateRepository floatingRateRepository;

    @Override
    public boolean isValid(final FloatingRateCreateRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        final boolean isBaseLendingRate = Boolean.TRUE.equals(request.getIsBaseLendingRate());
        final FloatingRate baseLendingRate = this.floatingRateRepository.retrieveBaseLendingRate();
        if (isBaseLendingRate && baseLendingRate != null) {
            context.buildConstraintViolationWithTemplate("{org.apache.fineract.portfolio.floatingrate.base-lending-rate.duplicate}")
                    .addPropertyNode("isBaseLendingRate").addConstraintViolation();
            valid = false;
        }

        valid &= FloatingRateRatePeriodsChecker.checkRatePeriods(request.getRatePeriods(), isBaseLendingRate, baseLendingRate != null,
                context);

        return valid;
    }
}
