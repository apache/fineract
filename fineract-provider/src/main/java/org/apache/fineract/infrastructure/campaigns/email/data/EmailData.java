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
package org.apache.fineract.infrastructure.campaigns.email.data;

import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;

import java.util.List;

/**
 * Immutable data object representing a SMS message.
 */
public class EmailData {

    private final Long id;
    private final Long groupId;
    private final Long clientId;
    private final Long staffId;
    private final EnumOptionData status;
    private final String emailAddress;
	private final String emailSubject;
    private final String emailMessage;
	private final EnumOptionData emailAttachmentFileFormat;
	private final ReportData stretchyReport;
	private final String stretchyReportParamMap;
	private final List<EnumOptionData> emailAttachmentFileFormatOptions;
	private final List<EnumOptionData> stretchyReportParamDateOptions;
    private final String campaignName;
	private final LocalDate sentDate;
	private final String errorMessage;


	public static EmailData instance(final Long id,final Long groupId, final Long clientId, final Long staffId, final EnumOptionData status,
								     final String emailAddress, final String emailSubject,
									 final String message, final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport,
									 final String stretchyReportParamMap, final List<EnumOptionData> emailAttachmentFileFormatOptions,
									 final List<EnumOptionData> stretchyReportParamDateOptions, final String campaignName, final LocalDate sentDate,final String errorMessage) {
        return new EmailData(id, groupId, clientId, staffId, status, emailAddress, emailSubject, message,
				emailAttachmentFileFormat,stretchyReport,stretchyReportParamMap,emailAttachmentFileFormatOptions,
				stretchyReportParamDateOptions,campaignName,sentDate,errorMessage);
    }

    private EmailData(final Long id,final Long groupId, final Long clientId, final Long staffId, final EnumOptionData status, final String emailAddress, final String emailSubject, final String message,
					  final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport, final String stretchyReportParamMap,
					  final List<EnumOptionData> emailAttachmentFileFormatOptions, final List<EnumOptionData> stretchyReportParamDateOptions,
					  final String campaignName,final LocalDate sentDate,final String errorMessage) {
        this.id = id;
        this.groupId = groupId;
        this.clientId = clientId;
        this.staffId = staffId;
        this.status = status;
        this.emailAddress = emailAddress;
		this.emailSubject = emailSubject;
        this.emailMessage = message;
		this.emailAttachmentFileFormat = emailAttachmentFileFormat;
		this.stretchyReport = stretchyReport;
		this.stretchyReportParamMap = stretchyReportParamMap;
		this.emailAttachmentFileFormatOptions = emailAttachmentFileFormatOptions;
		this.stretchyReportParamDateOptions = stretchyReportParamDateOptions;
        this.campaignName = campaignName;
		this.sentDate = sentDate;
		this.errorMessage = errorMessage;
    }

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the groupId
	 */
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @return the clientId
	 */
	public Long getClientId() {
		return clientId;
	}

	/**
	 * @return the staffId
	 */
	public Long getStaffId() {
		return staffId;
	}

	/**
	 * @return the status
	 */
	public EnumOptionData getStatus() {
		return status;
	}

	public String getErrorMessage() {return this.errorMessage;}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return emailMessage;
	}

	public String getCampaignName() {return this.campaignName;}

	public LocalDate getSentDate() { return this.sentDate; }

	public String getEmailSubject() {
		return emailSubject;
	}

	public EnumOptionData getEmailAttachmentFileFormat() {
		return emailAttachmentFileFormat;
	}

	public ReportData getStretchyReport() {
		return stretchyReport;
	}

	public String getStretchyReportParamMap() {
		return stretchyReportParamMap;
	}

	public List<EnumOptionData> getEmailAttachmentFileFormatOptions() {
		return emailAttachmentFileFormatOptions;
	}

	public List<EnumOptionData> getStretchyReportParamDateOptions() {
		return stretchyReportParamDateOptions;
	}
}