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
package org.apache.fineract.commands.api.v2;

import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.request.AuditRequest;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditsV2ApiDelegate implements AuditsV2Api {

    private final AuditReadPlatformService auditReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Override
    public Page<AuditData> retrieveAllAudits(UriInfo uriInfo, AuditRequest auditRequest, Integer offset, Integer limit, String orderBy,
            String sortOrder, boolean includeJson) {
        final PaginationParameters parameters = PaginationParameters.builder().paged(true).limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        final SQLBuilder extraCriteria = getExtraCriteria(auditRequest);
        return auditReadPlatformService.retrievePaginatedAuditEntries(extraCriteria, includeJson, parameters);
    }

    private SQLBuilder getExtraCriteria(AuditRequest auditRequest) {
        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", auditRequest.getActionName());
        if (auditRequest.getEntityName() != null) {
            extraCriteria.addCriteria("aud.entity_name like", auditRequest.getEntityName() + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", auditRequest.getResourceId());
        extraCriteria.addNonNullCriteria("aud.maker_id = ", auditRequest.getMakerId());
        extraCriteria.addNonNullCriteria("aud.checker_id = ", auditRequest.getCheckerId());
        if (auditRequest.getMakerDateTimeFrom() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.made_on_date >= ", auditRequest.getMakerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.made_on_date_utc >= ", auditRequest.getMakerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getMakerDateTimeTo() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.made_on_date <= ", auditRequest.getMakerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.made_on_date_utc <= ", auditRequest.getMakerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getCheckerDateTimeFrom() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.checked_on_date >= ", auditRequest.getCheckerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.checked_on_date_utc >= ", auditRequest.getCheckerDateTimeFrom(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        if (auditRequest.getCheckerDateTimeTo() != null) {
            extraCriteria.addSubOperation((SQLBuilder criteria) -> {
                criteria.addNonNullCriteria("aud.checked_on_date <= ", auditRequest.getCheckerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.NONE);
                criteria.addNonNullCriteria("aud.checked_on_date_utc <= ", auditRequest.getCheckerDateTimeTo(),
                        SQLBuilder.WhereLogicalOperator.OR);
            });
        }
        extraCriteria.addNonNullCriteria("aud.status = ", auditRequest.getStatus());
        extraCriteria.addNonNullCriteria("aud.office_id = ", auditRequest.getOfficeId());
        extraCriteria.addNonNullCriteria("aud.group_id = ", auditRequest.getGroupId());
        extraCriteria.addNonNullCriteria("aud.client_id = ", auditRequest.getClientId());
        extraCriteria.addNonNullCriteria("aud.loan_id = ", auditRequest.getLoanId());
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", auditRequest.getSavingsAccountId());

        return extraCriteria;
    }
}
