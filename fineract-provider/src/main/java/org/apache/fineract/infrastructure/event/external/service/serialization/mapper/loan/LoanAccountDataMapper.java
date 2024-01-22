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
package org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan;

import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroMapperConfig;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = AvroMapperConfig.class, uses = { LoanTransactionDataMapper.class, LoanChargeDataMapper.class,
        LoanDelinquencyPausePeriodMapper.class })
public interface LoanAccountDataMapper {

    // TODO: avoid prefix "is" in class attributes; I would recommend to fix this also in the Avro structures
    @Mapping(source = "loanProductLinkedToFloatingRate", target = "isLoanProductLinkedToFloatingRate")
    @Mapping(source = "floatingInterestRate", target = "isFloatingInterestRate")
    @Mapping(source = "topup", target = "isTopup")
    @Mapping(source = "interestRecalculationEnabled", target = "isInterestRecalculationEnabled")
    @Mapping(target = "externalOwnerId", ignore = true)
    @Mapping(target = "settlementDate", ignore = true)
    @Mapping(target = "purchasePriceRatio", ignore = true)
    @Mapping(target = "delinquent.installmentDelinquencyBuckets", ignore = true)
    LoanAccountDataV1 map(LoanAccountData source);

}
