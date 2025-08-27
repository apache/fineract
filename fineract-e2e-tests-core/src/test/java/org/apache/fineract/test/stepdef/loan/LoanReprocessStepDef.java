package org.apache.fineract.test.stepdef.loan;

import io.cucumber.java.en.When;
import java.io.IOException;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.InternalCobApi;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class LoanReprocessStepDef extends AbstractStepDef {

    @Autowired
    private InternalCobApi internalCobApi;

    @When("Admin runs loan reprocess for Loan")
    public void admin_runs_inline_COB_job_for_loan() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        internalCobApi.loanReprocess(loanId).execute();
    }
}
