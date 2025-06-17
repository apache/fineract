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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.idParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.taxGroupIdParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;

@RequiredArgsConstructor
public class SavingsProductBaseAssembler {

    protected final ChargeRepositoryWrapper chargeRepository;
    protected final TaxGroupRepositoryWrapper taxGroupRepository;

    public Set<Charge> assembleListOfSavingsProductCharges(final JsonCommand command, final String savingsProductCurrencyCode,
            final String chargesParameterName) {

        final Set<Charge> charges = new HashSet<>();

        if (command.parameterExists(chargesParameterName)) {
            final JsonArray chargesArray = command.arrayOfParameterNamed(chargesParameterName);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has(idParamName)) {
                        final Long id = jsonObject.get(idParamName).getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!charge.isSavingsCharge()) {
                            final String errorMessage = "Charge with identifier " + charge.getId()
                                    + " cannot be applied to Savings product.";
                            throw new ChargeCannotBeAppliedToException("savings.product", errorMessage, charge.getId());
                        }

                        if (!savingsProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Savings Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.savings.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }

        return charges;
    }

    public TaxGroup assembleTaxGroup(final JsonCommand command) {
        final Long taxGroupId = command.longValueOfParameterNamed(taxGroupIdParamName);
        TaxGroup taxGroup = null;
        if (taxGroupId != null) {
            taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(taxGroupId);
        }
        return taxGroup;
    }
}
