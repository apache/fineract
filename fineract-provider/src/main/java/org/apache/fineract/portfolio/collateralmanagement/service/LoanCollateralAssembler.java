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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.LoanCollateralManagementNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidAmountOfCollateralQuantity;

@RequiredArgsConstructor
public class LoanCollateralAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanCollateralManagementRepository loanCollateralRepository;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

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
                    collateralItems.add(LoanCollateralManagement.from(clientCollateral, quantity));
                } else {
                    LoanCollateralManagement loanCollateralManagement = this.loanCollateralRepository.findById(id)
                            .orElseThrow(() -> new LoanCollateralManagementNotFoundException(id));

                    if (loanCollateralManagement.getQuantity().compareTo(quantity) != 0) {
                        updatedClientQuantity = clientCollateral.getQuantity().add(loanCollateralManagement.getQuantity())
                                .subtract(quantity);
                        if (BigDecimal.ZERO.compareTo(updatedClientQuantity) > 0) {
                            throw new InvalidAmountOfCollateralQuantity(quantity);
                        }
                    } else {
                        updatedClientQuantity = quantity;
                    }

                    clientCollateral.updateQuantity(updatedClientQuantity);
                    collateralItems
                            .add(LoanCollateralManagement.fromExisting(clientCollateral, quantity, loanCollateralManagement.getLoanData(),
                                    loanCollateralManagement.getLoanTransaction(), loanCollateralManagement.getId()));
                }
            }
        }
        return collateralItems;
    }
}
