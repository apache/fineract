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

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.integrationtests.common.Utils;

public final class LoanOriginatorHelper {

    private static final String LOAN_ORIGINATOR_URL = "/fineract-provider/api/v1/loan-originators";

    private LoanOriginatorHelper() {}

    // ========== CREATE ==========

    public static Integer createOriginator(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId) {
        return createOriginator(requestSpec, responseSpec, externalId, null, null, null, null);
    }

    public static Integer createOriginator(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId, final String name, final String status, final Long originatorTypeId, final Long channelTypeId) {
        final String json = buildCreateJson(externalId, name, status, originatorTypeId, channelTypeId);
        return Utils.performServerPost(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "?" + Utils.TENANT_IDENTIFIER, json, "resourceId");
    }

    public static HashMap<String, Object> createOriginatorWithFullResponse(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        final String json = buildCreateJson(externalId, null, null, null, null);
        return Utils.performServerPost(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "?" + Utils.TENANT_IDENTIFIER, json, "");
    }

    public static List<HashMap<String, Object>> createOriginatorExpectingError(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        return Utils.performServerPost(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "?" + Utils.TENANT_IDENTIFIER, json, "errors");
    }

    // ========== READ ==========

    public static HashMap<String, Object> getOriginatorById(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer originatorId) {
        return Utils.performServerGet(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "/" + originatorId + "?" + Utils.TENANT_IDENTIFIER,
                "");
    }

    public static HashMap<String, Object> getOriginatorByExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId) {
        return Utils.performServerGet(requestSpec, responseSpec,
                LOAN_ORIGINATOR_URL + "/external-id/" + externalId + "?" + Utils.TENANT_IDENTIFIER, "");
    }

    public static List<HashMap<String, Object>> getAllOriginators(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerGet(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "?" + Utils.TENANT_IDENTIFIER, "");
    }

    // ========== UPDATE ==========

    public static HashMap<String, Object> updateOriginator(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer originatorId, final String name, final String status, final Long originatorTypeId, final Long channelTypeId) {
        final String json = buildUpdateJson(name, status, originatorTypeId, channelTypeId);
        return Utils.performServerPut(requestSpec, responseSpec, LOAN_ORIGINATOR_URL + "/" + originatorId + "?" + Utils.TENANT_IDENTIFIER,
                json, "");
    }

    public static HashMap<String, Object> updateOriginatorByExternalId(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String externalId, final String name, final String status,
            final Long originatorTypeId, final Long channelTypeId) {
        final String json = buildUpdateJson(name, status, originatorTypeId, channelTypeId);
        return Utils.performServerPut(requestSpec, responseSpec,
                LOAN_ORIGINATOR_URL + "/external-id/" + externalId + "?" + Utils.TENANT_IDENTIFIER, json, "");
    }

    // ========== DELETE ==========

    public static Integer deleteOriginator(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer originatorId) {
        return Utils.performServerDelete(requestSpec, responseSpec,
                LOAN_ORIGINATOR_URL + "/" + originatorId + "?" + Utils.TENANT_IDENTIFIER, "resourceId");
    }

    public static Integer deleteOriginatorByExternalId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String externalId) {
        return Utils.performServerDelete(requestSpec, responseSpec,
                LOAN_ORIGINATOR_URL + "/external-id/" + externalId + "?" + Utils.TENANT_IDENTIFIER, "resourceId");
    }

    // ========== JSON BUILDERS ==========

    private static String buildCreateJson(final String externalId, final String name, final String status, final Long originatorTypeId,
            final Long channelTypeId) {
        final StringBuilder json = new StringBuilder("{");
        json.append("\"externalId\": \"").append(externalId).append("\"");
        if (name != null) {
            json.append(", \"name\": \"").append(name).append("\"");
        }
        if (status != null) {
            json.append(", \"status\": \"").append(status).append("\"");
        }
        if (originatorTypeId != null) {
            json.append(", \"originatorTypeId\": ").append(originatorTypeId);
        }
        if (channelTypeId != null) {
            json.append(", \"channelTypeId\": ").append(channelTypeId);
        }
        json.append("}");
        return json.toString();
    }

    private static String buildUpdateJson(final String name, final String status, final Long originatorTypeId, final Long channelTypeId) {
        final StringBuilder json = new StringBuilder("{");
        boolean first = true;
        if (name != null) {
            json.append("\"name\": \"").append(name).append("\"");
            first = false;
        }
        if (status != null) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"status\": \"").append(status).append("\"");
            first = false;
        }
        if (originatorTypeId != null) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"originatorTypeId\": ").append(originatorTypeId);
            first = false;
        }
        if (channelTypeId != null) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"channelTypeId\": ").append(channelTypeId);
        }
        json.append("}");
        return json.toString();
    }

    // ========== UTILITIES ==========

    public static String generateUniqueExternalId() {
        return "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
