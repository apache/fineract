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
package org.apache.fineract.organisation.teller.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/cashiers")
@Component
@Tag(name = "Cashiers", description = "")
@RequiredArgsConstructor
public class CashierApiResource {

    private final DefaultToApiJsonSerializer<CashierData> jsonSerializer;
    private final TellerManagementReadPlatformService readPlatformService;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String getCashierData(@QueryParam("officeId") final Long officeId, @QueryParam("tellerId") final Long tellerId,
            @QueryParam("staffId") final Long staffId, @QueryParam("date") final String date) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;

        final LocalDate dateRestriction = (date != null ? LocalDate.parse(date, dateFormatter) : DateUtils.getBusinessLocalDate());

        final Collection<CashierData> allCashiers = this.readPlatformService.getCashierData(officeId, tellerId, staffId, dateRestriction);

        return this.jsonSerializer.serialize(allCashiers);
    }
}
