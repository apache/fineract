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

import org.springframework.http.HttpHeaders;

public class RequestCorrelation {
    
	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    
	private static String correlationId;

	private static final ThreadLocal<String> id = new ThreadLocal<String>();
	
	private static HttpHeaders headers = null;
	
	private RequestCorrelation() {
	    throw new IllegalStateException("Utility class");
    }


    public static HttpHeaders getHeaders() {
		return headers;
	}

	public static void setHeaders(HttpHeaders headers) {
		RequestCorrelation.headers = headers;
	}

	public static String getCorrelationid() {
		return correlationId;
	}
    
    public static void setCorrelationid(String correlId) {
		correlationId = correlId;
	}

    public static void setId(String correlationId) {
        id.set(correlationId);
    }

    public static String getId() {
        return id.get();
    }
    
    public static void unloadId() {
    	id.remove(); 
      }
}