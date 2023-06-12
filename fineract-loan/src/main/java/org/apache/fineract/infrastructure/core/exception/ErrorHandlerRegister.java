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
package org.apache.fineract.infrastructure.core.exception;

import jakarta.annotation.PostConstruct;
import org.apache.fineract.batch.exception.ErrorHandler;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformDomainRuleExceptionMapper;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlerRegister {

    @PostConstruct
    public void init() {
        ErrorHandler.registerNewErrorHandler(LoanIdsHardLockedException.class, runtimeException -> new ErrorInfo(HttpStatus.SC_CONFLICT,
                4090,
                ApiGlobalErrorResponse.loanIsLocked(((LoanIdsHardLockedException) runtimeException).getLoanIdFromRequest()).toJson()));
        ErrorHandler.registerNewErrorHandler(LinkedAccountRequiredException.class, new PlatformDomainRuleExceptionMapper(), 3002);
        ErrorHandler.registerNewErrorHandler(MultiDisbursementDataRequiredException.class, new PlatformDomainRuleExceptionMapper(), 3003);
    }
}
