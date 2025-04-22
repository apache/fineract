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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "currencies")
public interface ApplicationCurrencyRepository
        extends JpaRepository<ApplicationCurrency, Long>, JpaSpecificationExecutor<ApplicationCurrency> {

    String FIND_CURRENCY_DETAILS = "SELECT new org.apache.fineract.organisation.monetary.data.CurrencyData(ac.code, ac.name, ac.decimalPlaces, ac.inMultiplesOf, ac.displaySymbol, ac.nameCode) FROM ApplicationCurrency ac ";

    @Cacheable(key = "'entity_' + #currencyCode")
    ApplicationCurrency findOneByCode(String currencyCode);

    @Cacheable(key = "'data_' + #currencyCode")
    @Query(FIND_CURRENCY_DETAILS + " WHERE ac.code = :code")
    CurrencyData findCurrencyDataByCode(@Param("code") String currencyCode);

    @Cacheable
    @Query(FIND_CURRENCY_DETAILS)
    List<CurrencyData> findAllSorted(Sort sort);

    /**
     * Override save method with cache eviction
     */
    @Override
    @CacheEvict(allEntries = true)
    <S extends ApplicationCurrency> S save(S entity);

    /**
     * Override saveAll method with cache eviction
     */
    @Override
    @CacheEvict(allEntries = true)
    <S extends ApplicationCurrency> List<S> saveAll(Iterable<S> entities);

    /**
     * Override delete methods with cache eviction
     */
    @Override
    @CacheEvict(allEntries = true)
    void delete(ApplicationCurrency entity);

    @Override
    @CacheEvict(allEntries = true)
    void deleteAll();

    @Override
    @CacheEvict(allEntries = true)
    void deleteAll(Iterable<? extends ApplicationCurrency> entities);

    @Override
    @CacheEvict(allEntries = true)
    void deleteById(Long id);

    @Override
    @CacheEvict(allEntries = true)
    void deleteAllById(Iterable<? extends Long> ids);

    @Override
    @CacheEvict(allEntries = true)
    <S extends ApplicationCurrency> S saveAndFlush(S entity);
}
