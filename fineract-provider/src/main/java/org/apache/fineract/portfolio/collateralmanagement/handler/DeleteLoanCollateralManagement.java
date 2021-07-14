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
package org.apache.fineract.portfolio.collateralmanagement.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "LOAN_COLLATERAL_PRODUCT", action = "DELETE")
public class DeleteLoanCollateralManagement implements NewCommandSourceHandler {

    private final LoanCollateralManagementWritePlatformService loanCollateralManagementWritePlatformService;

    @Autowired
    public DeleteLoanCollateralManagement(final LoanCollateralManagementWritePlatformService loanCollateralManagement) {
        this.loanCollateralManagementWritePlatformService = loanCollateralManagement;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand jsonCommand) {
        return this.loanCollateralManagementWritePlatformService.deleteLoanCollateral(jsonCommand);
    }
}
