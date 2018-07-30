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
package org.apache.fineract.portfolio.rate.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.rate.domain.RateRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateAssembler {

  private final FromJsonHelper fromApiJsonHelper;
  private final RateRepositoryWrapper rateRepository;

  @Autowired
  public RateAssembler(final FromJsonHelper fromApiJsonHelper,
      final RateRepositoryWrapper rateRepository) {
    this.fromApiJsonHelper = fromApiJsonHelper;
    this.rateRepository = rateRepository;
  }

  public List<Rate> fromParsedJson(final JsonElement element) {

    final List<Rate> rateItems = new ArrayList<>();

    if (element.isJsonObject()) {
      final JsonObject topLevelJsonElement = element.getAsJsonObject();
      final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

      if (topLevelJsonElement.has(LoanProductConstants.ratesParamName) && topLevelJsonElement
          .get(LoanProductConstants.ratesParamName)
          .isJsonArray()) {
        final JsonArray array = topLevelJsonElement.get(LoanProductConstants.ratesParamName)
            .getAsJsonArray();
        List<Long> idList = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {

          final JsonObject rateElement = array.get(i).getAsJsonObject();

          final Long id = this.fromApiJsonHelper.extractLongNamed("id", rateElement);

          if (id != null) {
            final Long rateId = this.fromApiJsonHelper.extractLongNamed("id", rateElement);
            idList.add(rateId);
          }
        }
        rateItems.addAll(rateRepository.findMultipleWithNotFoundDetection(idList));
      }
    }

    return rateItems;
  }

}
