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

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class CorrelationHeaderFilter implements Filter {

    private String CORRELATION_ID_HEADER;

    @Autowired
    public CorrelationHeaderFilter(Environment env) {
        CORRELATION_ID_HEADER = env.getRequiredProperty("fineract.logging.http.correlation-id.header-name");        
    }
	
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //TODO        
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
                
        log.debug("CORRELATION_ID_HEADER : " + CORRELATION_ID_HEADER);

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        
        String currentCorrId = httpServletRequest.getHeader( CORRELATION_ID_HEADER);

        log.debug("Found correlationId in Header : {}", currentCorrId.replaceAll("[\r\n]","") );
        
        MDC.put("correlationId", currentCorrId);

        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    	MDC.remove("correlationId");
    }

}