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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import com.google.gson.JsonElement;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;

/**
 * Shared rules for delinquency/breach grace days and start type that depend on a parent bucket/breach.
 * <p>
 * Without a parent these fields must not be set at all (any non-null value is rejected), matching the product rule that
 * they are part of the parent configuration.
 */
public final class WorkingCapitalDependentSettingsValidator {

    private WorkingCapitalDependentSettingsValidator() {}

    public static boolean isSet(final Integer value) {
        return value != null;
    }

    public static boolean isSet(final String value) {
        return value != null && !value.isBlank();
    }

    public static void rejectIfSetWithoutParent(final DataValidatorBuilder baseDataValidator, final String parameterName,
            final boolean valueSet, final Long parentId, final String failCode) {
        if (valueSet && parentId == null) {
            baseDataValidator.reset().parameter(parameterName).failWithCode(failCode);
        }
    }

    public static Long resolveEffectiveDelinquencyBucketId(final FromJsonHelper fromApiJsonHelper, final JsonElement element,
            final Long productBucketId, final Long loanBucketId) {
        if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
            return fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element);
        }
        if (loanBucketId != null) {
            return loanBucketId;
        }
        return productBucketId;
    }

    /**
     * Effective breach for grace/start-type checks. Matches {@code WorkingCapitalLoanAssemblerImpl}: present
     * {@code breachId} (including {@code null}) overrides; omitted inherits loan then product.
     */
    public static Long resolveEffectiveBreachId(final FromJsonHelper fromApiJsonHelper, final JsonElement element,
            final Long productBreachId, final Long loanBreachId) {
        if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachIdParamName, element)) {
            return fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.breachIdParamName, element);
        }
        if (loanBreachId != null) {
            return loanBreachId;
        }
        return productBreachId;
    }
}
