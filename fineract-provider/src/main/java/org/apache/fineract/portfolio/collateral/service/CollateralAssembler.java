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
package org.apache.fineract.portfolio.collateral.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateralRepository;
import org.apache.fineract.portfolio.collateral.exception.CollateralNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class CollateralAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanCollateralRepository loanCollateralRepository;

    @Autowired
    public CollateralAssembler(final FromJsonHelper fromApiJsonHelper, final CodeValueRepositoryWrapper codeValueRepository,
            final LoanCollateralRepository loanCollateralRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.codeValueRepository = codeValueRepository;
        this.loanCollateralRepository = loanCollateralRepository;
    }

    public Set<LoanCollateral> fromParsedJson(final JsonElement element) {

        final Set<LoanCollateral> collateralItems = new HashSet<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();

            if (topLevelJsonElement.has("collateral") && topLevelJsonElement.get("collateral").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
                final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject collateralItemElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed("id", collateralItemElement);
                    final Long collateralTypeId = this.fromApiJsonHelper.extractLongNamed("type", collateralItemElement);
                    final CodeValue collateralType = this.codeValueRepository.findOneWithNotFoundDetection(collateralTypeId);
                    final String description = this.fromApiJsonHelper.extractStringNamed("description", collateralItemElement);
                    final BigDecimal value = this.fromApiJsonHelper.extractBigDecimalNamed("value", collateralItemElement, locale);

                    if (id == null) {
                        collateralItems.add(LoanCollateral.from(collateralType, value, description));
                    } else {
                        final LoanCollateral loanCollateralItem = this.loanCollateralRepository.findOne(id);
                        if (loanCollateralItem == null) { throw new CollateralNotFoundException(id); }

                        loanCollateralItem.assembleFrom(collateralType, value, description);

                        collateralItems.add(loanCollateralItem);
                    }
                }
            } else {
                // no collaterals passed, use existing ones against loan
            }

        }

        return collateralItems;
    }
}