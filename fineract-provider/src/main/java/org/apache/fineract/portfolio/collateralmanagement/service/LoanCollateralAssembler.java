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
package org.apache.fineract.portfolio.collateralmanagement.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.LoanCollateralManagementNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollateralQuantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanCollateralAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanCollateralManagementRepository loanCollateralRepository;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

    @Autowired
    public LoanCollateralAssembler(final FromJsonHelper fromApiJsonHelper, final CodeValueRepositoryWrapper codeValueRepository,
            final LoanCollateralManagementRepository loanCollateralRepository,
            final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.codeValueRepository = codeValueRepository;
        this.loanCollateralRepository = loanCollateralRepository;
        this.clientCollateralManagementRepositoryWrapper = clientCollateralManagementRepositoryWrapper;
    }

    public Set<LoanCollateralManagement> fromParsedJson(final JsonElement element) {

        final Set<LoanCollateralManagement> collateralItems = new HashSet<>();

        JsonObject jsonObject = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(jsonObject);

        if (jsonObject.has("collateral") && jsonObject.get("collateral").isJsonArray()) {
            JsonArray collaterals = jsonObject.get("collateral").getAsJsonArray();

            for (int i = 0; i < collaterals.size(); i++) {
                final JsonObject collateralItemElement = collaterals.get(i).getAsJsonObject();
                final Long id = this.fromApiJsonHelper.extractLongNamed("id", collateralItemElement);
                final Long collateralId = this.fromApiJsonHelper.extractLongNamed("clientCollateralId", collateralItemElement);
                final ClientCollateralManagement clientCollateral = this.clientCollateralManagementRepositoryWrapper
                        .getCollateral(collateralId);
                final BigDecimal quantity = this.fromApiJsonHelper.extractBigDecimalNamed("quantity", collateralItemElement, locale);
                BigDecimal updatedClientQuantity = null;

                if (id == null) {
                    updatedClientQuantity = clientCollateral.getQuantity().subtract(quantity);
                    if (BigDecimal.ZERO.compareTo(updatedClientQuantity) > 0) {
                        throw new InvalidAmountOfCollateralQuantity(quantity);
                    }
                    clientCollateral.updateQuantity(updatedClientQuantity);
                    // this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateral);
                    collateralItems.add(LoanCollateralManagement.from(clientCollateral, quantity));
                } else {
                    LoanCollateralManagement loanCollateralManagement = this.loanCollateralRepository.findById(id)
                            .orElseThrow(() -> new LoanCollateralManagementNotFoundException(id));
                    updatedClientQuantity = clientCollateral.getQuantity().add(loanCollateralManagement.getQuantity()).subtract(quantity);
                    if (BigDecimal.ZERO.compareTo(updatedClientQuantity) > 0) {
                        throw new InvalidAmountOfCollateralQuantity(quantity);
                    }

                    // loanCollateralManagement.setQuantity(quantity);
                    clientCollateral.updateQuantity(updatedClientQuantity);
                    // loanCollateralManagement.setClientCollateralManagement(clientCollateral);
                    // this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateral);
                    collateralItems
                            .add(LoanCollateralManagement.fromExisting(clientCollateral, quantity, loanCollateralManagement.getLoanData(),
                                    loanCollateralManagement.getLoanTransaction(), loanCollateralManagement.getId()));
                }
            }
        }

        // if (element.isJsonObject()) {
        // final JsonObject topLevelJsonElement = element.getAsJsonObject();
        //
        // if (topLevelJsonElement.has("collateral") && topLevelJsonElement.get("collateral").isJsonArray()) {
        // final JsonArray array = topLevelJsonElement.get("collateral").getAsJsonArray();
        // final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        // for (int i = 0; i < array.size(); i++) {
        //
        // final JsonObject collateralItemElement = array.get(i).getAsJsonObject();
        //
        // final Long id = this.fromApiJsonHelper.extractLongNamed("clientCollateralId", collateralItemElement);
        // final ClientCollateralManagement clientCollateral =
        // this.clientCollateralManagementRepositoryWrapper.getCollateral(id);
        // final BigDecimal quantity = this.fromApiJsonHelper.extractBigDecimalNamed("quantity", collateralItemElement,
        // locale);
        // collateralItems.add(LoanCollateralManagement.from(clientCollateral, quantity));
        // }
        // }
        //
        // }

        return collateralItems;
    }
}
