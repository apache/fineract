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

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("cashiers")
@Component
@Scope("singleton")
public class CashierApiResource {

    private final DefaultToApiJsonSerializer<CashierData> jsonSerializer;
    private final TellerManagementReadPlatformService readPlatformService;

    @Autowired
    public CashierApiResource(DefaultToApiJsonSerializer<CashierData> jsonSerializer,
            TellerManagementReadPlatformService readPlatformService) {
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
    }

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String getCashierData(@QueryParam("officeId") final Long officeId, @QueryParam("tellerId") final Long tellerId,
            @QueryParam("staffId") final Long staffId, @QueryParam("date") final String date) {
        final DateTimeFormatter dateFormatter = ISODateTimeFormat.basicDate();

        final Date dateRestriction = (date != null ? dateFormatter.parseDateTime(date).toDate() : new Date());

        final Collection<CashierData> allCashiers = this.readPlatformService.getCashierData(officeId, tellerId, staffId, dateRestriction);

        return this.jsonSerializer.serialize(allCashiers);
    }
}
