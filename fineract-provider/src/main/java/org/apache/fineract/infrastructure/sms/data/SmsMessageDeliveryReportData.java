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

/** 
 * Immutable data object representing an outbound SMS message delivery report data 
 **/
public class SmsMessageDeliveryReportData {
	private Long id;
	private String externalId;
	private String addedOnDate;
	private String deliveredOnDate;
	private Integer deliveryStatus;
	private Boolean hasError;
	private String errorMessage;
	
	/** 
	 * SmsMessageDeliveryReportData constructor
	 * 
	 *  
	 **/
	private SmsMessageDeliveryReportData(Long id, String externalId, String addedOnDate, String deliveredOnDate, 
			Integer deliveryStatus, Boolean hasError, String errorMessage) {
		this.id = id;
		this.externalId = externalId;
		this.addedOnDate = addedOnDate;
		this.deliveredOnDate = deliveredOnDate;
		this.deliveryStatus = deliveryStatus;
		this.hasError = hasError;
		this.errorMessage = errorMessage;
	}
	
	/** 
	 * Default SmsMessageDeliveryReportData constructor 
	 * 
	 * 
	 **/
	protected SmsMessageDeliveryReportData() {}
	
	/** 
	 * @return an instance of the SmsMessageDeliveryReportData class
	 **/
	public static SmsMessageDeliveryReportData getInstance(Long id, String externalId, String addedOnDate, String deliveredOnDate, 
			Integer deliveryStatus, Boolean hasError, String errorMessage) {
		
		return new SmsMessageDeliveryReportData(id, externalId, addedOnDate, deliveredOnDate, deliveryStatus, hasError, errorMessage);
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * @return the addedOnDate
	 */
	public String getAddedOnDate() {
		return addedOnDate;
	}

	/**
	 * @return the deliveredOnDate
	 */
	public String getDeliveredOnDate() {
		return deliveredOnDate;
	}

	/**
	 * @return the deliveryStatus
	 */
	public Integer getDeliveryStatus() {
		return deliveryStatus;
	}

	/**
	 * @return the hasError
	 */
	public Boolean getHasError() {
		return hasError;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
