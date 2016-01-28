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
package org.apache.fineract.mix.api;

import java.sql.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.fineract.mix.data.XBRLData;
import org.apache.fineract.mix.service.XBRLBuilder;
import org.apache.fineract.mix.service.XBRLResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixreport")
@Component
@Scope("singleton")
public class MixReportApiResource {

    private final XBRLResultService xbrlResultService;
    private final XBRLBuilder xbrlBuilder;

    @Autowired
    public MixReportApiResource(final XBRLResultService xbrlResultService, final XBRLBuilder xbrlBuilder) {
        this.xbrlResultService = xbrlResultService;
        this.xbrlBuilder = xbrlBuilder;
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String retrieveXBRLReport(@QueryParam("startDate") final Date startDate, @QueryParam("endDate") final Date endDate,
            @QueryParam("currency") final String currency) {

        final XBRLData data = this.xbrlResultService.getXBRLResult(startDate, endDate, currency);

        return this.xbrlBuilder.build(data);
    }
}