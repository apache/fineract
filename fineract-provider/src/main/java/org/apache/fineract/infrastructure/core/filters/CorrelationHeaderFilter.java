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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.MDC;

import lombok.RequiredArgsConstructor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class CorrelationHeaderFilter implements Filter {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationHeaderFilter.class);
    
   
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //TODO        
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
                
        LOGGER.info("CORRELATION_ID_HEADER : " + RequestCorrelation.CORRELATION_ID_HEADER);

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        
        String currentCorrId = httpServletRequest.getHeader( RequestCorrelation.CORRELATION_ID_HEADER);

        if (currentCorrId == null) {
            currentCorrId = UUID.randomUUID().toString();
            LOGGER.info("No correlationId found in Header. Generated : " + currentCorrId);
        } else {
            LOGGER.info("Found correlationId in Header : " + currentCorrId.replaceAll("[\r\n]","") );
        }
        MDC.put("correlationId", currentCorrId);
        RequestCorrelation.setId(currentCorrId);
        RequestCorrelation.setCorrelationid(currentCorrId);

        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    	MDC.remove("correlationId");
    }

}