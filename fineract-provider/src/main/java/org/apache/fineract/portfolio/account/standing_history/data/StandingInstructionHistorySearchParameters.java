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
package org.apache.fineract.portfolio.account.standing_history.data;

import jakarta.ws.rs.QueryParam;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.validation.ExternalIdFormat;
import org.apache.fineract.validation.OrderByFormat;
import org.apache.fineract.validation.SortOrderFormat;

/**
 * Search parameters for standing instruction history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandingInstructionHistorySearchParameters {

    @QueryParam("externalId")
    @ExternalIdFormat
    private String externalId;

    @QueryParam("offset")
    private Integer offset;

    @QueryParam("limit")
    private Integer limit;

    @QueryParam("orderBy")
    @OrderByFormat
    private String orderBy;

    @QueryParam("sortOrder")
    @SortOrderFormat
    private String sortOrder;

    @QueryParam("transferType")
    private Integer transferType;

    @QueryParam("clientName")
    private String clientName;

    @QueryParam("clientId")
    private Long clientId;

    @QueryParam("fromAccountId")
    private Long fromAccount;

    @QueryParam("fromAccountType")
    private Integer fromAccountType;

    @QueryParam("locale")
    private String locale;

    @QueryParam("dateFormat")
    private String dateFormat;

    @QueryParam("fromDate")
    private DateParam fromDate;

    @QueryParam("toDate")
    private DateParam toDate;

    public StandingInstructionDTO toDto() {
        SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();

        LocalDate startDateRange = fromDate != null ? fromDate.getDate("fromDate", new DateFormat(dateFormat), locale) : null;

        LocalDate endDateRange = toDate != null ? toDate.getDate("toDate", new DateFormat(dateFormat), locale) : null;

        return StandingInstructionDTO.builder().searchParameters(searchParameters).transferType(transferType).clientName(clientName)
                .clientId(clientId).fromAccount(fromAccount).fromAccountType(fromAccountType).startDateRange(startDateRange)
                .endDateRange(endDateRange).build();
    }
}
