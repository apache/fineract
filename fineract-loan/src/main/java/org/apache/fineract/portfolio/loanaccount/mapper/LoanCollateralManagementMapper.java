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
package org.apache.fineract.portfolio.loanaccount.mapper;

import java.util.Set;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface LoanCollateralManagementMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "clientCollateralId", source = "clientCollateralManagement.id")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "totalCollateral", ignore = true)
    LoanCollateralManagementData map(LoanCollateralManagement loanCollateralManagement);

    Set<LoanCollateralManagementData> map(Set<LoanCollateralManagement> loanCollateralManagements);
}
