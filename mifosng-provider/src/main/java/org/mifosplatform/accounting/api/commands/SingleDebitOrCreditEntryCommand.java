package org.mifosplatform.accounting.api.commands;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable command for adding a "Credit" entry to the Journal
 */
public class SingleDebitOrCreditEntryCommand {

    private final Long glAccountId;
    private final BigDecimal amount;
    private final String comments;

    private final Set<String> parametersPassedInRequest;

    public SingleDebitOrCreditEntryCommand(Set<String> parametersPassedInRequest, Long glAccountId, BigDecimal amount, String comments) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.glAccountId = glAccountId;
        this.amount = amount;
        this.comments = comments;
    }

    public boolean isGlAccountIdChanged() {
        return this.parametersPassedInRequest.contains("glAccountId");
    }

    public boolean isGlAmountChanged() {
        return this.parametersPassedInRequest.contains("amount");
    }

    public boolean isCommentsChanged() {
        return this.parametersPassedInRequest.contains("comments");
    }

    public Long getGlAccountId() {
        return this.glAccountId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getComments() {
        return this.comments;
    }

    public Set<String> getParametersPassedInRequest() {
        return this.parametersPassedInRequest;
    }

}