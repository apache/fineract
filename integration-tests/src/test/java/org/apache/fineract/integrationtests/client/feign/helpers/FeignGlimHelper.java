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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostLoansLoanIdGlimApprovalData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;

/** Typed Feign helper for GLIM (Group Loan Individual Monitoring) operations. */
public class FeignGlimHelper {

    private static final String APPROVE_COMMAND = "approve";
    private static final String UNDO_APPROVAL_COMMAND = "undoApproval";
    private static final String DISBURSE_COMMAND = "disburse";
    private static final String UNDO_DISBURSAL_COMMAND = "undoDisbursal";
    private static final String REJECT_COMMAND = "reject";

    private final FineractFeignClient fineractClient;

    public FeignGlimHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /** Result of a GLIM application: the parent {@code glimId} and the child {@code loanId}. */
    public record GlimApplication(Long glimId, Long loanId) {
    }

    /** Submits a GLIM application and returns the parent {@code glimId} and child {@code loanId}. */
    public GlimApplication applyGlim(PostLoansRequest request) {
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
        return new GlimApplication(response.getGlimId(), response.getLoanId());
    }

    /** Approves a GLIM application. */
    public PostLoansLoanIdResponse approveGlim(Long glimId, Long childLoanId, String approvedOnDate, BigDecimal glimPrincipal) {
        PostLoansLoanIdGlimApprovalData approval = new PostLoansLoanIdGlimApprovalData()//
                .loanId(childLoanId)//
                .approvedOnDate(approvedOnDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest()//
                .approvalFormData(List.of(approval))//
                .glimPrincipal(glimPrincipal)//
                .locale(LoanTestData.LOCALE);
        return command(glimId, request, APPROVE_COMMAND);
    }

    public PostLoansLoanIdResponse disburseGlim(Long glimId, String actualDisbursementDate) {
        return command(glimId, dated(new PostLoansLoanIdRequest().actualDisbursementDate(actualDisbursementDate)), DISBURSE_COMMAND);
    }

    public PostLoansLoanIdResponse undoDisbursalGlim(Long glimId) {
        return command(glimId, new PostLoansLoanIdRequest(), UNDO_DISBURSAL_COMMAND);
    }

    public PostLoansLoanIdResponse undoApprovalGlim(Long glimId) {
        return command(glimId, new PostLoansLoanIdRequest(), UNDO_APPROVAL_COMMAND);
    }

    public PostLoansLoanIdResponse rejectGlim(Long glimId, String rejectedOnDate) {
        return command(glimId, dated(new PostLoansLoanIdRequest().rejectedOnDate(rejectedOnDate)), REJECT_COMMAND);
    }

    private PostLoansLoanIdResponse command(Long glimId, PostLoansLoanIdRequest request, String command) {
        return ok(() -> fineractClient.loans().handleCommandsGlimLoan(glimId, request, command));
    }

    private static PostLoansLoanIdRequest dated(PostLoansLoanIdRequest request) {
        return request.dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE);
    }

    /** JSON array of the GLIM accounts under a group; the endpoint declares no OpenAPI response schema. */
    public String retrieveGlimAccountsByGroup(Long groupId) {
        return ok(() -> fineractClient.groups().retrieveGlimAccountsGroup(groupId, Map.of()));
    }

    /** JSON of a single GLIM account; the endpoint declares no OpenAPI response schema. */
    public String retrieveGlimAccountByGlimId(Long glimId) {
        return ok(() -> fineractClient.loans().getGlimRepaymentTemplate(glimId));
    }
}
