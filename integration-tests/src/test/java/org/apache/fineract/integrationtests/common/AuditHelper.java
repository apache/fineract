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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.AuditSearchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Manthan Surkar
 *
 */
public final class AuditHelper {

    private AuditHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(AuditHelper.class);

    // GET /v1/audits can return either a plain array (paged=false) or a
    // {totalFilteredRecords, pageItems} wrapper (paged=true), so the server resource's
    // return type is raw String rather than a typed schema (see FINERACT dev ML thread
    // pending resolution). We always request paged=true and parse out "pageItems" here,
    // using the same Jackson ObjectMapper the feign client itself uses for every other
    // successful response (see FineractFeignClientConfig), so date/enum handling stays
    // consistent with typed calls elsewhere in this file.
    private static final ObjectMapper MAPPER = ObjectMapperFactory.getShared();
    private static final TypeReference<List<AuditData>> AUDIT_DATA_LIST_TYPE = new TypeReference<>() {};

    public static List<AuditData> getAuditDetails(final Integer resourceId, final String actionName, final String entityName) {
        return pageItems(ok(() -> FineractFeignClientHelper.getFineractFeignClient().audits().retrieveAuditEntries(actionName, // actionName
                entityName, // entityName
                resourceId == null ? null : resourceId.longValue(), // resourceId
                null, // makerId
                null, // makerDateTimeFrom
                null, // makerDateTimeTo
                null, // checkerId
                null, // checkerDateTimeFrom
                null, // checkerDateTimeTo
                null, // status
                null, // clientId
                null, // loanId
                null, // officeId
                null, // groupId
                null, // savingsAccountId
                null, // processingResult
                null, // dateFormat
                null, // locale
                null, // offset
                null, // limit
                "id", // orderBy
                "DESC", // sortOrder
                true // paged
        )));
    }

    public static List<AuditData> getAuditDetails(final int limit) {
        return pageItems(ok(() -> FineractFeignClientHelper.getFineractFeignClient().audits().retrieveAuditEntries(null, // actionName
                null, // entityName
                null, // resourceId
                null, // makerId
                null, // makerDateTimeFrom
                null, // makerDateTimeTo
                null, // checkerId
                null, // checkerDateTimeFrom
                null, // checkerDateTimeTo
                null, // status
                null, // clientId
                null, // loanId
                null, // officeId
                null, // groupId
                null, // savingsAccountId
                null, // processingResult
                null, // dateFormat
                null, // locale
                null, // offset
                limit, // limit
                null, // orderBy
                null, // sortOrder
                true // paged
        )));
    }

    private static List<AuditData> pageItems(final String response) {
        try {
            final JsonNode pageItemsNode = MAPPER.readTree(response).get("pageItems");
            return MAPPER.convertValue(pageItemsNode, AUDIT_DATA_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to parse audits list response: " + response, e);
        }
    }

    public static AuditSearchData getAuditSearchTemplate() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().audits().retrieveAuditSearchTemplate());
    }

    /**
     * Some audit actions can only be done once Eg: Creation of a client with id 123, hence we verify number of audits
     * For such operations is "equal" to 1 always
     */
    public static void verifyOneAuditOnly(final List<AuditData> auditsToCheck, final Integer id, final String actionName,
            final String entityType) {
        LOG.info("------------------------------CHECK IF AUDIT CREATED------------------------------------\n");
        assertEquals(1, auditsToCheck.size(), "More than one audit created");
        final AuditData auditToCheck = auditsToCheck.get(0);
        final String actual = auditToCheck.getActionName() + " is done on " + auditToCheck.getEntityName() + " with id "
                + auditToCheck.getResourceId();
        final String expected = actionName + " is done on " + entityType + " with id " + id;
        assertEquals(expected, actual, "Error in creating audit!");
    }

    public static void verifyMultipleAuditsOnserver(final List<AuditData> auditsRecievedInitial, final List<AuditData> auditsRecieved,
            final Integer id, final String actionName, final String entityType) {
        LOG.info("------------------------------CHECK IF AUDIT CREATED------------------------------------\n");
        assertEquals(auditsRecievedInitial.size() + 1, auditsRecieved.size(), "Audit is not Created");

        final Comparator<AuditData> compareById = Comparator.comparing(AuditData::getId);
        Collections.sort(auditsRecieved, compareById.reversed());

        // First element is new audit created(Sorted DESC by Id)
        final AuditData auditToCheck = auditsRecieved.get(0);
        final String actual = auditToCheck.getActionName() + " is done on " + auditToCheck.getEntityName() + " with id "
                + auditToCheck.getResourceId();
        final String expected = actionName + " is done on " + entityType + " with id " + id;
        assertEquals(expected, actual, "Error in creating audit!");
    }

    public static void verifyLimitParameterfor(final int limit) {
        assertEquals(limit, getAuditDetails(limit).size(), "Incorrect number of audits recieved for limit: " + Integer.toString(limit));
    }

    public static void verifyOrderBysupported(final String orderByValue) {
        ok(() -> FineractFeignClientHelper.getFineractFeignClient().audits().retrieveAuditEntries(null, // actionName
                null, // entityName
                null, // resourceId
                null, // makerId
                null, // makerDateTimeFrom
                null, // makerDateTimeTo
                null, // checkerId
                null, // checkerDateTimeFrom
                null, // checkerDateTimeTo
                null, // status
                null, // clientId
                null, // loanId
                null, // officeId
                null, // groupId
                null, // savingsAccountId
                null, // processingResult
                null, // dateFormat
                null, // locale
                null, // offset
                null, // limit
                orderByValue, // orderBy
                null, // sortOrder
                true // paged
        ));
    }

}
