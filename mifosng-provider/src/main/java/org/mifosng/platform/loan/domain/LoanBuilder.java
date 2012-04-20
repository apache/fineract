package org.mifosng.platform.loan.domain;

import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.organisation.domain.Organisation;

public class LoanBuilder {

    private Organisation organisation;
    private Client       client;
    private LoanProduct  loanProduct;
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;
    private LoanStatus loanStatus;
    private String externalSystemId;

    public Loan build() {
        Loan loan = new Loan(this.organisation, this.client, this.loanProduct, this.loanRepaymentScheduleDetail, this.loanStatus);
        loan.setExternalId(this.externalSystemId);
        return loan;
    }

    public LoanBuilder with(final Organisation withOrg) {
        this.organisation = withOrg;
        return this;
    }

    public LoanBuilder with(final LoanProduct withLoanProduct) {
        this.loanProduct = withLoanProduct;
        return this;
    }

    public LoanBuilder with(final Client withClient) {
        this.client = withClient;
        return this;
    }

    public LoanBuilder with(final LoanProductRelatedDetail withLoanRepaymentScheduleDetail) {
        this.loanRepaymentScheduleDetail = withLoanRepaymentScheduleDetail;
        return this;
    }

    public LoanBuilder withExternalSystemId(final String withExternalSystemId) {
        this.externalSystemId = withExternalSystemId;
        return this;
    }
}