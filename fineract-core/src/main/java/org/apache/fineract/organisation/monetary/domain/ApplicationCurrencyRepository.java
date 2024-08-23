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
package org.apache.fineract.organisation.monetary.domain;

import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationCurrencyRepository
        extends JpaRepository<ApplicationCurrency, Long>, JpaSpecificationExecutor<ApplicationCurrency> {

    String FIND_CURRENCY_DETAILS = "SELECT new org.apache.fineract.organisation.monetary.data.CurrencyData(ac.code, ac.name, ac.decimalPlaces, ac.inMultiplesOf, ac.displaySymbol, ac.nameCode) FROM ApplicationCurrency ac ";

    ApplicationCurrency findOneByCode(String currencyCode);

    @Query(FIND_CURRENCY_DETAILS + " WHERE ac.code = :code")
    CurrencyData findCurrencyDataByCode(@Param("code") String currencyCode);

    @Query(FIND_CURRENCY_DETAILS)
    List<CurrencyData> findAllSorted(Sort sort);
}
