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
package org.apache.fineract.accounting.accrual.service;

import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_EXECUTION_ERROR_CODE;
import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME;
import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.accrueTillParamName;
import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.fromLoanId;
import static org.apache.fineract.accounting.accrual.api.AccrualAccountingConstants.toLoanId;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.accounting.accrual.serialization.AccrualAccountingDataValidator;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformServiceImpl;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccrualAccountingWritePlatformServiceImpl implements AccrualAccountingWritePlatformService {
	
	private final static Logger logger = LoggerFactory.getLogger(AccrualAccountingWritePlatformServiceImpl.class);
    private final LoanAccrualPlatformService loanAccrualPlatformService;
    private final AccrualAccountingDataValidator accountingDataValidator;

    @Autowired
    public AccrualAccountingWritePlatformServiceImpl(final LoanAccrualPlatformService loanAccrualPlatformService,
            final AccrualAccountingDataValidator accountingDataValidator) {
        this.loanAccrualPlatformService = loanAccrualPlatformService;
        this.accountingDataValidator = accountingDataValidator;
    }

    @Override
    public CommandProcessingResult executeLoansPeriodicAccrual(JsonCommand command) {
        this.accountingDataValidator.validateLoanPeriodicAccrualData(command.json());
        LocalDate tilldate = command.localDateValueOfParameterNamed(accrueTillParamName);
        Integer fromLoanIdInteger = command.integerValueOfParameterNamed(fromLoanId);
        Integer toLoanIdInteger = command.integerValueOfParameterNamed(toLoanId);
        String errorlog = this.loanAccrualPlatformService.addPeriodicAccruals(tilldate, fromLoanIdInteger, toLoanIdInteger);
        if (errorlog.length() > 0) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME);
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(PERIODIC_ACCRUAL_ACCOUNTING_EXECUTION_ERROR_CODE, errorlog);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        return CommandProcessingResult.empty();
    }

}
