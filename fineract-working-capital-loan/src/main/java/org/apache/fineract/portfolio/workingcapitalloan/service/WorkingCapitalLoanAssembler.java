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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;

/**
 * Assembles {@link WorkingCapitalLoan} from repository (by id or external id) or from API JSON command.
 */
public interface WorkingCapitalLoanAssembler {

    /**
     * Assembles a new WorkingCapitalLoan from the create loan application JSON command.
     */
    WorkingCapitalLoan assembleFrom(JsonCommand command);

    /**
     * Applies update (modify) parameters from the command to the loan. Returns map of changed field names to new
     * values.
     */
    Map<String, Object> updateFrom(JsonCommand command, WorkingCapitalLoan loan);

    /**
     * If account number was not provided in the request, generates one and sets it on the loan (e.g. after save when id
     * is available). Aligned with {@code LoanAssembler.accountNumberGeneration}.
     */
    void accountNumberGeneration(JsonCommand command, WorkingCapitalLoan loan);
}
