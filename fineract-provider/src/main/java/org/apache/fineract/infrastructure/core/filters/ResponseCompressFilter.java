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
package org.apache.fineract.infrastructure.core.filters;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.apache.fineract.infrastructure.core.writer.GZipResponseWriter;

import javax.ws.rs.core.HttpHeaders;

/**
 * Filter that compresses the response using gzip
 */

public class ResponseCompressFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if (request.getRequestHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
            response.getHttpHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
            response.setContainerResponseWriter( new GZipResponseWriter(response.getContainerResponseWriter()));
        }

        return response;
    }
}
