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
package org.apache.fineract.portfolio.loanaccount.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface LoanApplicationWritePlatformService {

    CommandProcessingResult submitApplication(JsonCommand command);

    CommandProcessingResult modifyApplication(Long loanId, JsonCommand command);

    CommandProcessingResult deleteApplication(Long loanId);

    CommandProcessingResult approveApplication(Long loanId, JsonCommand command);

    CommandProcessingResult undoApplicationApproval(Long loanId, JsonCommand command);

    CommandProcessingResult rejectApplication(Long loanId, JsonCommand command);

    CommandProcessingResult applicantWithdrawsFromApplication(Long loanId, JsonCommand command);

    CommandProcessingResult approveGLIMLoanAppication(Long loanId, JsonCommand command);

    CommandProcessingResult undoGLIMLoanApplicationApproval(Long loanId, JsonCommand command);

    CommandProcessingResult rejectGLIMApplicationApproval(Long loanId, JsonCommand command);
}
