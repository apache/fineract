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
package org.apache.fineract.portfolio.loanaccount.data;

import lombok.Data;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.mapstruct.Mapping;

@Data
public class LoanPointInTimeData {

    // Loan attributes
    private Long id;
    private String accountNo;
    private LoanStatusEnumData status;
    private String externalId;
    private CurrencyData currency;
    private LoanPrincipalData principal;
    private LoanInterestData interest;
    private LoanFeeData fee;
    private LoanPenaltyData penalty;
    private LoanTotalAmountData total;

    // Client attributes
    private Long clientId;
    private String clientAccountNo;
    private String clientExternalId;
    private String clientDisplayName;
    private Long clientOfficeId;

    // Loan product attributes
    private Long loanProductId;
    private String loanProductName;

    @org.mapstruct.Mapper(config = MapstructMapperConfig.class, uses = { LoanStatusEnumData.Mapper.class, CurrencyMapper.class,
            LoanPrincipalData.Mapper.class, LoanInterestData.Mapper.class, LoanFeeData.Mapper.class, LoanPenaltyData.Mapper.class,
            LoanTotalAmountData.Mapper.class })
    public interface Mapper {

        @Mapping(source = "accountNumber", target = "accountNo")
        @Mapping(source = "source", target = "status")
        @Mapping(source = "client.id", target = "clientId")
        @Mapping(source = "client.accountNumber", target = "clientAccountNo")
        @Mapping(source = "client.externalId", target = "clientExternalId")
        @Mapping(source = "client.displayName", target = "clientDisplayName")
        @Mapping(source = "client.office.id", target = "clientOfficeId")
        @Mapping(source = "summary", target = "principal")
        @Mapping(source = "summary", target = "interest")
        @Mapping(source = "summary", target = "fee")
        @Mapping(source = "summary", target = "penalty")
        @Mapping(source = "summary", target = "total")
        @Mapping(source = "loanProduct.id", target = "loanProductId")
        @Mapping(source = "loanProduct.name", target = "loanProductName")
        LoanPointInTimeData map(Loan source);
    }
}
