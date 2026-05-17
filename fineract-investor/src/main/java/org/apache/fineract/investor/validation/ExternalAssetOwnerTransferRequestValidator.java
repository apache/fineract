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
package org.apache.fineract.investor.validation;

import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.BUY_BACK_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.INTERMEDIARY_SALE_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.SALE_COMMAND_VALUE;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerSaleRequest;

public class ExternalAssetOwnerTransferRequestValidator
        implements ConstraintValidator<ExternalAssetOwnerTransferRequestConstraint, ExternalAssetOwnerSaleRequest> {

    @Override
    public boolean isValid(ExternalAssetOwnerSaleRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getCommand() == null) {
            return true;
        }

        return switch (value.getCommand()) {
            case SALE_COMMAND_VALUE, INTERMEDIARY_SALE_COMMAND_VALUE -> validateSaleLikeRequest(value, context);
            case BUY_BACK_COMMAND_VALUE -> true;
            default -> false;
        };
    }

    private boolean validateSaleLikeRequest(ExternalAssetOwnerSaleRequest value, ConstraintValidatorContext context) {
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (StringUtils.isBlank(value.getOwnerExternalId())) {
            context.buildConstraintViolationWithTemplate("{org.apache.fineract.investor.transfer.owner-external-id.not-blank}")
                    .addPropertyNode("ownerExternalId").addConstraintViolation();
            valid = false;
        }

        if (StringUtils.isBlank(value.getPurchasePriceRatio())) {
            context.buildConstraintViolationWithTemplate("{org.apache.fineract.investor.transfer.purchase-price-ratio.not-blank}")
                    .addPropertyNode("purchasePriceRatio").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
