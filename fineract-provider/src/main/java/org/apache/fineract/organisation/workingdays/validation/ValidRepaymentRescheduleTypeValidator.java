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
package org.apache.fineract.organisation.workingdays.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class ValidRepaymentRescheduleTypeValidator implements ConstraintValidator<ValidRepaymentRescheduleType, EnumOptionData> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidRepaymentRescheduleType constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(EnumOptionData value, ConstraintValidatorContext context) {
        if (value == null) return true;

        try {
            int type = Integer.parseInt(value.getCode());
            return type >= this.min && type <= this.max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
