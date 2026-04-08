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
package org.apache.fineract.infrastructure.core.debug;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

/**
 * Returns HTTP Request headers. Useful for debugging (e.g. for
 * <a href= "https://issues.apache.org/jira/browse/FINERACT-914">FINERACT-914</a>. Could later be replaced with <a href=
 * "https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-http-tracing">Spring
 * Boot's Actuator HTTP Tracing</a> (see also <a href="https://www.baeldung.com/spring-boot-actuator-http">related
 * tutorial on Baeldung.com</a>), but that exposes a lot more than just the current request out of the box, and would to
 * be properly authenticated for a dedicated new debug role.
 *
 * @author Michael Vorburger.ch
 */
@Component
@Path("/v1/echo")
public class EchoHeadersApiResource {

    @GET
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.TEXT_PLAIN })
    public String get(@Context HttpHeaders headers) {
        StringBuilder sb = new StringBuilder("Request Headers:\n");
        headers.getRequestHeaders().forEach((k, v) -> sb.append(k).append(" : ").append(v.get(0)).append("\n"));
        return sb.toString();
    }
}
