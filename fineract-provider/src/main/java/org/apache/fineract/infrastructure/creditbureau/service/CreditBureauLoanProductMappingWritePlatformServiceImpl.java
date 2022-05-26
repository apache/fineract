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
package org.apache.fineract.infrastructure.creditbureau.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauLoanProductMapping;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauLoanProductMappingRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauLoanProductCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditBureauLoanProductMappingWritePlatformServiceImpl implements CreditBureauLoanProductMappingWritePlatformService {

    private final PlatformSecurityContext context;

    private final CreditBureauLoanProductMappingRepository creditBureauLoanProductMappingRepository;

    private final OrganisationCreditBureauRepository organisationCreditBureauRepository;

    private final LoanProductRepository loanProductRepository;

    private final CreditBureauLoanProductCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CreditBureauLoanProductMappingWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CreditBureauLoanProductMappingRepository creditBureauLoanProductMappingRepository,
            final OrganisationCreditBureauRepository organisationCreditBureauRepository, LoanProductRepository loanProductRepository,
            final CreditBureauLoanProductCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.creditBureauLoanProductMappingRepository = creditBureauLoanProductMappingRepository;
        this.organisationCreditBureauRepository = organisationCreditBureauRepository;
        this.loanProductRepository = loanProductRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;

    }

    @Transactional
    @Override
    public CommandProcessingResult addCreditBureauLoanProductMapping(Long organisationCreditBureauId, JsonCommand command) {
        this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForCreate(command.json(), organisationCreditBureauId);

        final long lpid = command.longValueOfParameterNamed("loanProductId");

        final OrganisationCreditBureau orgcb = this.organisationCreditBureauRepository.getById(organisationCreditBureauId);

        final LoanProduct lp = this.loanProductRepository.getById(lpid);

        final CreditBureauLoanProductMapping cb_lp = CreditBureauLoanProductMapping.fromJson(command, orgcb, lp);

        this.creditBureauLoanProductMappingRepository.saveAndFlush(cb_lp);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(cb_lp.getId()).build();

    }

    @Override
    public CommandProcessingResult updateCreditBureauLoanProductMapping(JsonCommand command) {

        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final Long mappingid = command.longValueOfParameterNamed("creditbureauLoanProductMappingId");
        final boolean isActive = command.booleanPrimitiveValueOfParameterNamed("isActive");
        final CreditBureauLoanProductMapping cblpmapping = this.creditBureauLoanProductMappingRepository.getById(mappingid);
        cblpmapping.setIs_active(isActive);
        this.creditBureauLoanProductMappingRepository.saveAndFlush(cblpmapping);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(cblpmapping.getId()).build();
    }
}
