package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes" })
public class SchedulerJobHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public SchedulerJobHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList getAllSchedulerJobs(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_ALL_SCHEDULER_JOBS_URL = "/mifosng-provider/api/v1/jobs?tenantIdentifier=default";
        System.out.println("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        final ArrayList response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_SCHEDULER_JOBS_URL, "");
        return response;
    }

    public static HashMap getSchedulerJobById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/mifosng-provider/api/v1/jobs/" + jobId + "?tenantIdentifier=default";
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        return response;
    }
    
    public static HashMap getSchedulerStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_SCHEDULER_STATUS_URL = "/mifosng-provider/api/v1/scheduler?tenantIdentifier=default";
        System.out.println("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_SCHEDULER_STATUS_URL, "");
        return response;
    }

    public static HashMap updateSchedulerJob(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jobId, final String active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/mifosng-provider/api/v1/jobs/" + jobId + "?tenantIdentifier=default";
        System.out.println("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final HashMap response = Utils.performServerPut(requestSpec, responseSpec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    public static String updateSchedulerJobAsJSON(final String active) {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("active", active);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

}