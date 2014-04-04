package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.SchedulerJobHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.charges.ChargesHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsAccountHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsProductHelper;
import org.mifosplatform.integrationtests.common.savings.SavingsStatusChecker;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "unchecked", "rawtypes", "static-access", "cast" })
public class SchedulerJobsTestResults {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpecForSchedulerJob;
    private SchedulerJobHelper schedulerJobHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("X-Mifos-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForSchedulerJob = new ResponseSpecBuilder().expectStatusCode(202).build();
    }

    @Test
    public void testApplyAnnualFeeForSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer annualFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsAnnualFeeJSON());
        Assert.assertNotNull(annualFeeChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, annualFeeChargeId);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(1, chargesPendingState.size());

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        ArrayList<HashMap> allSchedulerJobsData = this.schedulerJobHelper.getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allSchedulerJobsData);

        Integer jobId = (Integer) allSchedulerJobsData.get(3).get("jobId");

        HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        // Executing Scheduler Job
        this.schedulerJobHelper.runSchedulerJob(this.requestSpec, this.responseSpecForSchedulerJob, jobId.toString());
        ArrayList<HashMap> jobHistoryData = this.schedulerJobHelper.getSchedulerJobHistory(this.requestSpec, this.responseSpec,
                jobId.toString());

        // Retrieving Scheduler Job by ID
        HashMap schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
        Assert.assertNotNull(schedulerJob);

        // Waiting for Job to Complete
        while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
            Thread.sleep(120000);
            schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            Assert.assertNotNull(schedulerJob);
            System.out.println("Job is Still Running");
        }

        // Verifying the Status of the Recently executed Scheduler Job
        Assert.assertEquals("Verifying Last Scheduler Job Status", "success",
                jobHistoryData.get(((jobHistoryData.size()) - 1)).get("status"));
        final HashMap chargeData = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, annualFeeChargeId);

        Float chargeAmount = (Float) chargeData.get("amount");

        final HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assert.assertEquals("Verifying Annual Fee after Running Scheduler Job for Apply Anual Fee", chargeAmount,
                (Float) summaryAfter.get("totalAnnualFees"));

    }

    @Test
    public void testInterestPostingForSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        ArrayList<HashMap> allSchedulerJobsData = this.schedulerJobHelper.getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allSchedulerJobsData);

        Integer jobId = (Integer) allSchedulerJobsData.get(5).get("jobId");

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        // Executing Scheduler Job
        this.schedulerJobHelper.runSchedulerJob(this.requestSpec, this.responseSpecForSchedulerJob, jobId.toString());
        ArrayList<HashMap> jobHistoryData = this.schedulerJobHelper.getSchedulerJobHistory(this.requestSpec, this.responseSpec,
                jobId.toString());

        // Retrieving Scheduler Job by ID
        HashMap schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
        Assert.assertNotNull(schedulerJob);

        // Waiting for Job to complete
        while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
            Thread.sleep(120000);
            schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            Assert.assertNotNull(schedulerJob);
            System.out.println("Job is Still Running");
        }

        // Verifying the Status of the Recently executed Scheduler Job
        Assert.assertEquals("Verifying Last Scheduler Job Status", "success",
                jobHistoryData.get(((jobHistoryData.size()) - 1)).get("status"));

        final HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        Assert.assertNotSame("Verifying the Balance after running Post Interest for Savings Job", summaryBefore.get("accountBalance"),
                summaryAfter.get("accountBalance"));
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }
}
