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
package org.apache.fineract.accounting.financialactivityaccount.domain;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QAccGlFinancialActivityAccount.accGlFinancialActivityAccount;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;

public final class FinancialActivityAccountProjection extends MappingProjection<FinancialActivityAccountData> {

    public FinancialActivityAccountProjection() {
        super(FinancialActivityAccountData.class, accGlFinancialActivityAccount.id, accGlFinancialActivityAccount.financialActivityType,
                accGlAccount.id, accGlAccount.name, accGlAccount.glCode);
    }

    @Override
    protected FinancialActivityAccountData map(Tuple row) {
        var glAccount = new GLAccountData().setId(row.get(accGlAccount.id)).setName(row.get(accGlAccount.name))
                .setGlCode(row.get(accGlAccount.glCode));

        var financialActivityId = row.get(accGlFinancialActivityAccount.financialActivityType);

        var activity = financialActivityId == null ? null
                : AccountingConstants.FinancialActivity.toFinancialActivityData(financialActivityId);

        return FinancialActivityAccountData.builder().id(row.get(accGlFinancialActivityAccount.id)).financialActivityData(activity)
                .glAccountData(glAccount).build();
    }
}
