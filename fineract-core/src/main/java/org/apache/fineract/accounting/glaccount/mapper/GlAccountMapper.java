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
package org.apache.fineract.accounting.glaccount.mapper;

import java.util.List;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = { CodeValueMapper.class })
public interface GlAccountMapper {

    @Mapping(target = "usage", source = "glAccount.usage", qualifiedByName = "glAccountUsage")
    @Mapping(target = "type", source = "glAccount.type", qualifiedByName = "glAccountType")
    @Mapping(target = "name", source = "glAccount.name")
    @Mapping(target = "nameDecorated", source = "glAccount.name")
    @Mapping(target = "parentId", source = "glAccount.parent.id")
    @Mapping(target = "tagId", source = "glAccount.tagId")
    @Mapping(target = "accountTypeOptions", ignore = true)
    @Mapping(target = "usageOptions", ignore = true)
    @Mapping(target = "assetHeaderAccountOptions", ignore = true)
    @Mapping(target = "liabilityHeaderAccountOptions", ignore = true)
    @Mapping(target = "equityHeaderAccountOptions", ignore = true)
    @Mapping(target = "incomeHeaderAccountOptions", ignore = true)
    @Mapping(target = "expenseHeaderAccountOptions", ignore = true)
    @Mapping(target = "allowedAssetsTagOptions", ignore = true)
    @Mapping(target = "allowedLiabilitiesTagOptions", ignore = true)
    @Mapping(target = "allowedEquityTagOptions", ignore = true)
    @Mapping(target = "allowedIncomeTagOptions", ignore = true)
    @Mapping(target = "allowedExpensesTagOptions", ignore = true)
    @Mapping(target = "organizationRunningBalance", ignore = true)
    @Mapping(target = "rowIndex", ignore = true)
    GLAccountData map(GLAccount glAccount);

    List<GLAccountData> map(List<GLAccount> glAccounts);

    @Named("glAccountType")
    default EnumOptionData glAccountType(Integer typeId) {
        return GLAccountType.fromInt(typeId).toEnumOptionData();
    }

    @Named("glAccountUsage")
    default EnumOptionData glAccountUsage(Integer usageId) {
        return GLAccountUsage.fromInt(usageId).toEnumOptionData();
    }
}
