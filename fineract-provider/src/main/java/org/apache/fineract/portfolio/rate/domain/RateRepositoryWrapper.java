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
package org.apache.fineract.portfolio.rate.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.portfolio.rate.exception.RateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateRepositoryWrapper {

  private final RateRepository repository;

  @Autowired
  public RateRepositoryWrapper(final RateRepository repository) {
    this.repository = repository;
  }

  public Rate findOneWithNotFoundDetection(final Long rateId) {
    final Rate rate = this.repository.findById(rateId).orElseThrow(() -> new RateNotFoundException(rateId));
    return rate;
  }

  public List<Rate> findMultipleWithNotFoundDetection(final List<Long> rateIds) {
    List<Rate> rates = new ArrayList<>();
    if (rateIds != null && !rateIds.isEmpty()) {
      final List<Rate> foundRates = this.repository.findAllById(rateIds);
      for (Long rateId : rateIds) {
        Boolean found = false;
        for (Rate foundRate : foundRates) {
          if (Objects.equals(foundRate.getId(),
              rateId)) {
            found = true;
            break;
          }
        }
        if (!found) {
          throw new RateNotFoundException(rateId);
        }
      }
      rates.addAll(foundRates);
    }
    return rates;
  }

}
