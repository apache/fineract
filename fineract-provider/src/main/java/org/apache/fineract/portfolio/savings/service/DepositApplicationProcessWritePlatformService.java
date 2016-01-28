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
package org.apache.fineract.portfolio.savings.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.savings.DepositAccountType;

public interface DepositApplicationProcessWritePlatformService {

    CommandProcessingResult submitFDApplication(JsonCommand command);

    CommandProcessingResult submitRDApplication(JsonCommand command);

    CommandProcessingResult modifyFDApplication(Long accountId, JsonCommand command);

    CommandProcessingResult modifyRDApplication(Long accountId, JsonCommand command);

    CommandProcessingResult deleteApplication(Long accountId, DepositAccountType depositAccountType);

    CommandProcessingResult approveApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult undoApplicationApproval(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult rejectApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult applicantWithdrawsFromApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);
}