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
package org.apache.fineract.integrationtests.common.loans;

import java.util.HashMap;

import com.google.gson.Gson;

public class LoanRescheduleRequestTestBuilder {

    private String rescheduleFromDate = "04 December 2014";
    private String graceOnPrincipal = "2";
    private String graceOnInterest = "2";
    private String extraTerms = "2";
    private Boolean recalculateInterest = false;
    private String newInterestRate = null;
    private String adjustedDueDate = null;
    private String rescheduleReasonId = "1";
    private String rescheduleReasonComment = null;
    private String submittedOnDate = "04 September 2014";

    public String build(final String loanId) {
        final HashMap<String, Object> map = new HashMap<>();

        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("loanId", loanId);
        map.put("submittedOnDate", submittedOnDate);
        map.put("rescheduleFromDate", rescheduleFromDate);

        if (graceOnPrincipal != null) {
            map.put("graceOnPrincipal", graceOnPrincipal);
        }

        if (graceOnInterest != null) {
            map.put("graceOnInterest", graceOnInterest);
        }

        if (extraTerms != null) {
            map.put("extraTerms", extraTerms);
        }

        map.put("recalculateInterest", recalculateInterest);

        if (newInterestRate != null) {
            map.put("newInterestRate", newInterestRate);
        }

        if (adjustedDueDate != null) {
            map.put("adjustedDueDate", adjustedDueDate);
        }

        map.put("rescheduleReasonId", rescheduleReasonId);

        if (rescheduleReasonComment != null) {
            map.put("rescheduleReasonComment", rescheduleReasonComment);
        }

        return new Gson().toJson(map);
    }

    public LoanRescheduleRequestTestBuilder updateRescheduleFromDate(final String rescheduleFromDate) {
        if (rescheduleFromDate != null) {
            this.rescheduleFromDate = rescheduleFromDate;
        }

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateGraceOnPrincipal(final String graceOnPrincipal) {
        this.graceOnPrincipal = graceOnPrincipal;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateGraceOnInterest(final String graceOnInterest) {
        this.graceOnInterest = graceOnInterest;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateExtraTerms(final String extraTerms) {
        this.extraTerms = extraTerms;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateRecalculateInterest(final Boolean recalculateInterest) {
        this.recalculateInterest = recalculateInterest;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateNewInterestRate(final String newInterestRate) {
        this.newInterestRate = newInterestRate;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateAdjustedDueDate(final String adjustedDueDate) {
        this.adjustedDueDate = adjustedDueDate;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateRescheduleReasonId(final String rescheduleReasonId) {
        this.rescheduleReasonId = rescheduleReasonId;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateRescheduleReasonComment(final String rescheduleReasonComment) {
        this.rescheduleReasonComment = rescheduleReasonComment;

        return this;
    }

    public LoanRescheduleRequestTestBuilder updateSubmittedOnDate(final String submittedOnDate) {
        this.submittedOnDate = submittedOnDate;

        return this;
    }

    public String getRejectLoanRescheduleRequestJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("rejectedOnDate", submittedOnDate);
        return new Gson().toJson(map);
    }

    public String getApproveLoanRescheduleRequestJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedOnDate", submittedOnDate);
        return new Gson().toJson(map);
    }
}
