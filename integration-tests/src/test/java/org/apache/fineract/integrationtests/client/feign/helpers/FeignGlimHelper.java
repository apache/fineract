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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;

/**
 * Typed Feign helper for GLIM (Group Loan Individual Monitoring) operations. Standalone by design: nothing here is
 * added to {@code FeignLoanTestBase} (see FEIGN_BASE_MODULARIZATION.md).
 * <p>
 * The disburse / undo-disbursal / undo-approval / reject commands are typed via {@code LoansApi.handleCommandsGlimLoan}
 * → {@link PostLoansLoanIdResponse}. Three things stay raw for reasons intrinsic to the flow, not shortcuts:
 * <ul>
 * <li><b>Application</b> — POST {@code /loans} for a GLIM returns {@code glimId}/{@code loanId}, which the generated
 * {@code PostLoansResponse} does not model, and the request needs GLIM-only fields ({@code loanType=glim},
 * {@code totalLoan}, {@code isParentAccount}) absent from {@code PostLoansRequest}; the caller supplies the built JSON
 * (sanctioned raw fallback, pr_review_lessons_learned #8).</li>
 * <li><b>Approve</b> — the server expects a per-child {@code approvalFormData} array that
 * {@code PostLoansLoanIdRequest} can't express (the typed command 500s with only a parent-level date).</li>
 * <li><b>Retrieval</b> — {@code GroupsApi.retrieveglimAccountsUniversal} and {@code LoansApi.getGlimRepaymentTemplate}
 * return a bare {@code String} <i>in the generated SDK itself</i>, so JSON is the only representation available.</li>
 * </ul>
 */
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

    /**
     * Submits a GLIM application (raw HTTP — see class javadoc) from the caller-built {@code glimApplicationJson} and
     * returns the parent {@code glimId} and child {@code loanId} from the response.
     */
    public GlimApplication applyGlim(String glimApplicationJson) {
        JsonObject response = JsonParser.parseString(FeignRawHttpHelper.post("/loans", glimApplicationJson)).getAsJsonObject();
        return new GlimApplication(response.get("glimId").getAsLong(), response.get("loanId").getAsLong());
    }

    /**
     * Approves a GLIM application. Raw HTTP because the server expects a per-child {@code approvalFormData} array
     * ({@code loanId}/{@code approvedOnDate} per child) plus a {@code glimPrincipal} (which populates the GLIM
     * account's principal) — neither expressible via {@code PostLoansLoanIdRequest} (#8); the typed command carries
     * only a single parent-level {@code approvedOnDate} and 500s / violates a not-null constraint.
     */
    public String approveGlim(Long glimId, Long childLoanId, String approvedOnDate, String glimPrincipal) {
        JsonObject entry = new JsonObject();
        entry.addProperty("loanId", childLoanId);
        entry.addProperty("approvedOnDate", approvedOnDate);
        entry.addProperty("dateFormat", LoanTestData.DATETIME_PATTERN);
        entry.addProperty("locale", LoanTestData.LOCALE);
        JsonArray approvalFormData = new JsonArray();
        approvalFormData.add(entry);
        JsonObject body = new JsonObject();
        body.add("approvalFormData", approvalFormData);
        body.addProperty("glimPrincipal", glimPrincipal);
        body.addProperty("locale", LoanTestData.LOCALE);
        return FeignRawHttpHelper.post("/loans/glimAccount/" + glimId + "?command=" + APPROVE_COMMAND, body.toString());
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

    /**
     * Raw JSON array of the GLIM accounts under a group. Raw HTTP because {@code GroupsApi} exposes only a deprecated
     * no-parameter variant (the supported typed one requires a {@code parentLoanAccountNo}), and it returns a bare
     * {@code String} regardless.
     */
    public String retrieveGlimAccountsByGroup(Long groupId) {
        return FeignRawHttpHelper.get("/groups/" + groupId + "/glimaccounts");
    }

    /** Raw JSON of a single GLIM account by {@code glimId} (the generated SDK returns a bare {@code String}). */
    public String retrieveGlimAccountByGlimId(Long glimId) {
        return ok(() -> fineractClient.loans().getGlimRepaymentTemplate(glimId));
    }
}
