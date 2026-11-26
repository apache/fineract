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
package org.apache.fineract.organisation.monetary.service;

import static org.apache.fineract.query.QMCurrency.mCurrency;
import static org.apache.fineract.query.QMOrganisationCurrency.mOrganisationCurrency;

import com.querydsl.sql.SQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.CurrencyDataProjection;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyReadPlatformServiceImpl implements CurrencyReadPlatformService {

    protected final JdbcTemplate jdbcTemplate;
    protected final SQLQueryFactory queryFactory;

    @Override
    public List<CurrencyData> retrieveAllowedCurrencies() {
        return queryFactory.select(CurrencyDataProjection.forOrganisationCurrency()).from(mOrganisationCurrency)
                .orderBy(mOrganisationCurrency.name.asc()).fetch();
    }

    @Override
    public List<CurrencyData> retrieveAllPlatformCurrencies() {
        return queryFactory.select(new CurrencyDataProjection()).from(mCurrency).orderBy(mCurrency.name.asc()).fetch();
    }

    @Override
    public CurrencyData retrieveCurrency(final String code) {
        final CurrencyData currency = queryFactory.select(new CurrencyDataProjection()).from(mCurrency).where(mCurrency.code.eq(code))
                .orderBy(mCurrency.name.asc()).fetchOne();

        if (currency == null) {
            // keep the contract of the former JdbcTemplate.queryForObject call
            throw new EmptyResultDataAccessException(1);
        }

        return currency;
    }
}
