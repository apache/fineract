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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.service;

import java.util.List;

import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;

public interface LoanRescheduleRequestReadPlatformService {

    /**
     * get all loan reschedule requests by loan ID
     * 
     * @param loanId
     *            the loan identifier
     * @return list of LoanRescheduleRequestData objects
     **/
    public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId);

    /**
     * get a single loan reschedule request by ID (primary key)
     * 
     * @param requestId
     *            the loan reschedule request identifier
     * @return a LoanRescheduleRequestData object
     **/
    public LoanRescheduleRequestData readLoanRescheduleRequest(Long requestId);

    /**
     * get all loan reschedule requests filter by loan ID and status enum
     * 
     * @param loanId
     *            the loan identifier
     * @return list of LoanRescheduleRequestData objects
     **/
    public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId, Integer statusEnum);

    /**
     * get all loan reschedule reasons
     * 
     * @param loanRescheduleReason
     *            the loan reschedule reason
     * @return list of LoanRescheduleRequestData objects
     **/
    public LoanRescheduleRequestData retrieveAllRescheduleReasons(String loanRescheduleReason);
    /**
     * get all loan reschedule request
     * 
     * @param command 
     * all/null - give all request
     * approved - give all approved request
     * pending - give all  request which is pending for approval
     * reject - give all rejected requests
     * @return list of LoanRescheduleRequestData objects
     **/
    public List<LoanRescheduleRequestData> retrieveAllRescheduleRequests(String command);
}
