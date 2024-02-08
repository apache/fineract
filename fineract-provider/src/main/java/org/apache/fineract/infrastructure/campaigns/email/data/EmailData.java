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

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;

/**
 * Immutable data object representing a SMS message.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class EmailData {

    private Long id;
    private Long groupId;
    private Long clientId;
    private Long staffId;
    private EnumOptionData status;
    private String emailAddress;
    private String emailSubject;
    private String emailMessage;
    private EnumOptionData emailAttachmentFileFormat;
    private ReportData stretchyReport;
    private String stretchyReportParamMap;
    private List<EnumOptionData> emailAttachmentFileFormatOptions;
    private List<EnumOptionData> stretchyReportParamDateOptions;
    private String campaignName;
    private LocalDate sentDate;
    private String errorMessage;

    public static EmailData instance(final Long id, final Long groupId, final Long clientId, final Long staffId,
            final EnumOptionData status, final String emailAddress, final String emailSubject, final String message,
            final EnumOptionData emailAttachmentFileFormat, final ReportData stretchyReport, final String stretchyReportParamMap,
            final List<EnumOptionData> emailAttachmentFileFormatOptions, final List<EnumOptionData> stretchyReportParamDateOptions,
            final String campaignName, final LocalDate sentDate, final String errorMessage) {
        return new EmailData().setId(id).setGroupId(groupId).setClientId(clientId).setStaffId(staffId).setStatus(status)
                .setEmailAddress(emailAddress).setEmailSubject(emailSubject).setEmailMessage(message)
                .setEmailAttachmentFileFormat(emailAttachmentFileFormat).setStretchyReport(stretchyReport)
                .setStretchyReportParamMap(stretchyReportParamMap).setEmailAttachmentFileFormatOptions(emailAttachmentFileFormatOptions)
                .setStretchyReportParamDateOptions(stretchyReportParamDateOptions).setCampaignName(campaignName).setSentDate(sentDate)
                .setErrorMessage(errorMessage);
    }
}
