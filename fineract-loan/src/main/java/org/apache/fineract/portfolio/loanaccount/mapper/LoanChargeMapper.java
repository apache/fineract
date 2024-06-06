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
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.command.LoanChargeCommand;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface LoanChargeMapper {

    @Mapping(target = "id", source = "loanCharge.id")
    @Mapping(target = "chargeId", source = "loanCharge.charge.id")
    @Mapping(target = "amount", expression = "java(loanCharge.getAmount(currency).getAmount())")
    @Mapping(target = "chargeTimeType", source = "loanCharge.chargeTimeType.value")
    @Mapping(target = "chargeCalculationType", source = "loanCharge.chargeCalculation.value")
    @Mapping(target = "dueDate", source = "loanCharge.dueDate")
    LoanChargeCommand map(LoanCharge loanCharge, MonetaryCurrency currency);

    default Set<LoanChargeCommand> map(Set<LoanCharge> loanCharge, MonetaryCurrency currency) {
        return loanCharge.stream().map(lc -> map(lc, currency)).collect(Collectors.toSet());
    }
}
