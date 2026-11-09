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
package org.apache.fineract.accounting.financialactivityaccount.service;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QAccGlFinancialActivityAccount.accGlFinancialActivityAccount;

import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountProjection;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialActivityAccountReadPlatformServiceImpl implements FinancialActivityAccountReadPlatformService {

    private final SQLQueryFactory queryFactory;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;

    @Override
    public List<FinancialActivityAccountData> retrieveAll() {
        return baseQuery().fetch();
    }

    @Override
    public FinancialActivityAccountData retrieve(Long financialActivityAccountId) {
        var data = baseQuery().where(accGlFinancialActivityAccount.id.eq(financialActivityAccountId)).fetchOne();

        if (data == null) {
            throw new FinancialActivityAccountNotFoundException(financialActivityAccountId);
        }

        return data;
    }

    @Override
    public FinancialActivityAccountData addTemplateDetails(FinancialActivityAccountData financialActivityAccountData) {
        var accountOptions = accountingDropdownReadPlatformService.retrieveAccountMappingOptions();
        financialActivityAccountData.setGlAccountOptions(accountOptions);
        financialActivityAccountData.setFinancialActivityOptions(FinancialActivity.getAllFinancialActivities());

        return financialActivityAccountData;
    }

    @Override
    public FinancialActivityAccountData getFinancialActivityAccountTemplate() {
        return addTemplateDetails(new FinancialActivityAccountData());
    }

    private SQLQuery<FinancialActivityAccountData> baseQuery() {
        return queryFactory.select(new FinancialActivityAccountProjection()).from(accGlFinancialActivityAccount).join(accGlAccount)
                .on(accGlAccount.id.eq(accGlFinancialActivityAccount.glAccountId));
    }
}
