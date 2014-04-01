package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.SchedulerJobHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes", "unchecked", "static-access" })
public class SchedulerJobsTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testSchedulerJobs() {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        ArrayList<HashMap> allSchedulerJobsData = this.schedulerJobHelper.getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allSchedulerJobsData);

        Integer jobId = Utils.randomValueGenerator(1, 9);

        HashMap schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
        Assert.assertNotNull(schedulerJob);

        Boolean active = (Boolean) schedulerJob.get("active");

        if (active == true) {
            active = false;
        } else {
            active = true;
        }

        HashMap changes = this.schedulerJobHelper.updateSchedulerJob(this.requestSpec, this.responseSpec, jobId.toString(),
                active.toString());
        Assert.assertEquals("Verifying Scheduler Job Updation", active, changes.get("active"));

        HashMap schedulerStatus = this.schedulerJobHelper.getSchedulerStatus(this.requestSpec, this.responseSpec);
        Boolean status = (Boolean) schedulerStatus.get("active");

    }
}