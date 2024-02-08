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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.sql.Date;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.mix.data.XBRLData;
import org.apache.fineract.mix.service.XBRLBuilder;
import org.apache.fineract.mix.service.XBRLResultService;
import org.springframework.stereotype.Component;

@Path("/v1/mixreport")
@Component
@Tag(name = "Mix Report", description = "")
@RequiredArgsConstructor
public class MixReportApiResource {

    private final XBRLResultService xbrlResultService;
    private final XBRLBuilder xbrlBuilder;

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String retrieveXBRLReport(@QueryParam("startDate") final Date startDate, @QueryParam("endDate") final Date endDate,
            @QueryParam("currency") final String currency) {

        final XBRLData data = this.xbrlResultService.getXBRLResult(startDate, endDate, currency);

        return this.xbrlBuilder.build(data);
    }
}
