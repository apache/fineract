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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AuditSearchData;

public class FeignAuditHelper {

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuditEntryItem {

        private Long id;
        private String actionName;
        private String entityName;
        private Long resourceId;
        private Long subresourceId;
        private String maker;
        private String madeOnDate;
        private String checker;
        private String checkedOnDate;
        private String processingResult;
        private String commandAsJson;
        private String officeName;
        private String groupLevelName;
        private String groupName;
        private String clientName;
        private String loanAccountNo;
        private String savingsAccountNo;
        private Long clientId;
        private Long loanId;
        private String url;
        private String ip;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedAuditResponse {

        private Integer totalFilteredRecords;
        private List<AuditEntryItem> pageItems;
    }

    interface PagedAuditsApi {

        @RequestLine("GET /v1/audits?actionName={actionName}&entityName={entityName}&resourceId={resourceId}&makerId={makerId}&makerDateTimeFrom={makerDateTimeFrom}&makerDateTimeTo={makerDateTimeTo}&checkerId={checkerId}&checkerDateTimeFrom={checkerDateTimeFrom}&checkerDateTimeTo={checkerDateTimeTo}&status={status}&clientId={clientId}&loanId={loanId}&officeId={officeId}&groupId={groupId}&savingsAccountId={savingsAccountId}&processingResult={processingResult}&dateFormat={dateFormat}&locale={locale}&timeZone={timeZone}&offset={offset}&limit={limit}&orderBy={orderBy}&sortOrder={sortOrder}")
        @Headers({ "Accept: application/json" })
        PagedAuditResponse retrieveAuditEntries(@Param("actionName") String actionName, @Param("entityName") String entityName,
                @Param("resourceId") Long resourceId, @Param("makerId") Long makerId, @Param("makerDateTimeFrom") String makerDateTimeFrom,
                @Param("makerDateTimeTo") String makerDateTimeTo, @Param("checkerId") Long checkerId,
                @Param("checkerDateTimeFrom") String checkerDateTimeFrom, @Param("checkerDateTimeTo") String checkerDateTimeTo,
                @Param("status") String status, @Param("clientId") Long clientId, @Param("loanId") Long loanId,
                @Param("officeId") Long officeId, @Param("groupId") Long groupId, @Param("savingsAccountId") Long savingsAccountId,
                @Param("processingResult") String processingResult, @Param("dateFormat") String dateFormat, @Param("locale") String locale,
                @Param("timeZone") String timeZone, @Param("offset") Integer offset, @Param("limit") Integer limit,
                @Param("orderBy") String orderBy, @Param("sortOrder") String sortOrder);
    }

    private final FineractFeignClient fineractClient;
    private final PagedAuditsApi pagedAuditsApi;

    public FeignAuditHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.pagedAuditsApi = fineractClient.create(PagedAuditsApi.class);
    }

    public PagedAuditResponse getAudits(Integer limit) {
        return ok(() -> pagedAuditsApi.retrieveAuditEntries(null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, limit, null, null));
    }

    public List<AuditEntryItem> getAuditsByResourceAndAction(Long resourceId, String actionName, String entityName) {
        PagedAuditResponse response = ok(() -> pagedAuditsApi.retrieveAuditEntries(actionName, entityName, resourceId, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, 100, "id", "DESC"));
        return response.getPageItems();
    }

    public PagedAuditResponse getAuditsWithDateTimeRange(String makerDateTimeFrom, String makerDateTimeTo, String dateFormat,
            String timeZone) {
        return ok(() -> pagedAuditsApi.retrieveAuditEntries(null, null, null, null, makerDateTimeFrom, makerDateTimeTo, null, null, null,
                null, null, null, null, null, null, null, dateFormat, null, timeZone, null, 10, null, null));
    }

    public List<AuditEntryItem> getAuditsPageItems(String makerDateTimeFrom, String makerDateTimeTo, String dateFormat, String timeZone) {
        PagedAuditResponse response = getAuditsWithDateTimeRange(makerDateTimeFrom, makerDateTimeTo, dateFormat, timeZone);
        return response.getPageItems();
    }

    public PagedAuditResponse getAuditsWithOrderBy(String orderBy) {
        return ok(() -> pagedAuditsApi.retrieveAuditEntries(null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, 10, orderBy, null));
    }

    public AuditSearchData getAuditSearchTemplate() {
        return ok(() -> fineractClient.audits().retrieveAuditSearchTemplate(Collections.emptyMap()));
    }

    public CallFailedRuntimeException getAuditsExpectingError(String makerDateTimeFrom, String makerDateTimeTo, String dateFormat,
            String timeZone) {
        return fail(() -> pagedAuditsApi.retrieveAuditEntries(null, null, null, null, makerDateTimeFrom, makerDateTimeTo, null, null, null,
                null, null, null, null, null, null, null, dateFormat, null, timeZone, null, 10, null, null));
    }
}
