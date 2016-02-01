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
package org.apache.fineract.portfolio.loanaccount.guarantor.service;

import java.util.List;

import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;

public interface GuarantorReadPlatformService {

    /**
     * Validates the passed in loanId before retrieving Guarantors for the same
     * 
     * @param loanId
     * @return
     */
    List<GuarantorData> retrieveGuarantorsForValidLoan(Long loanId);

    /**
     * Methods Returns all Guarantors for a Given loan Id (if the loan Id is
     * valid and Exists)
     * 
     * @param loanId
     * @return
     */
    List<GuarantorData> retrieveGuarantorsForLoan(Long loanId);

    GuarantorData retrieveGuarantor(Long loanId, Long guarantorId);

}