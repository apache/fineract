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
package org.apache.fineract.infrastructure.sms.data;

import java.util.List;

import com.google.gson.Gson;

/** 
 * Immutable data object representing the API request body sent in the POST request to
 * the "/report" resource 
 **/
public class SmsMessageApiReportResourceData {
	private List<Long> externalIds;
	private String mifosTenantIdentifier;
	
	/** 
	 * SmsMessageApiReportResourceData constructor 
	 **/
	private SmsMessageApiReportResourceData(List<Long> externalIds, String mifosTenantIdentifier) {
		this.externalIds = externalIds;
		this.mifosTenantIdentifier = mifosTenantIdentifier;
	}
	
	/** 
	 * SmsMessageApiReportResourceData constructor 
	 **/
	protected SmsMessageApiReportResourceData() {}
	
	/** 
	 * @return new instance of the SmsMessageApiReportResourceData class
	 **/
	public static final SmsMessageApiReportResourceData instance(List<Long> externalIds, String mifosTenantIdentifier) {
		return new SmsMessageApiReportResourceData(externalIds, mifosTenantIdentifier);
	}

	/**
	 * @return the externalIds
	 */
	public List<Long> getExternalIds() {
		return externalIds;
	}

	/**
	 * @return the mifosTenantIdentifier
	 */
	public String getMifosTenantIdentifier() {
		return mifosTenantIdentifier;
	}
	
	/** 
	 * @return JSON representation of the object 
	 **/
	public String toJsonString() {
		Gson gson = new Gson();
		
		return gson.toJson(this);
	}
}
