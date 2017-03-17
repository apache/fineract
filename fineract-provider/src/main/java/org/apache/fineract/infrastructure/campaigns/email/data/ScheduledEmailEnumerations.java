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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.campaigns.email.domain.ScheduledEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.campaigns.email.domain.ScheduledEmailStretchyReportParamDateOption;

public class ScheduledEmailEnumerations {
    public static EnumOptionData emailAttachementFileFormat(final Integer emailAttachementFileFormatId) {
        return emailAttachementFileFormat(ScheduledEmailAttachmentFileFormat.instance(emailAttachementFileFormatId));
    }
    
    public static EnumOptionData emailAttachementFileFormat(final String emailAttachementFileFormatString) {
        return emailAttachementFileFormat(ScheduledEmailAttachmentFileFormat.instance(emailAttachementFileFormatString));
    }
    
    public static EnumOptionData emailAttachementFileFormat(final ScheduledEmailAttachmentFileFormat emailAttachementFileFormat) {
        EnumOptionData enumOptionData = null;
        
        if (emailAttachementFileFormat != null) {
            enumOptionData = new EnumOptionData(emailAttachementFileFormat.getId().longValue(), emailAttachementFileFormat.getCode(), 
                    emailAttachementFileFormat.getValue());
        }
        
        return enumOptionData;
    }
    
    public static EnumOptionData stretchyReportDateOption(final ScheduledEmailStretchyReportParamDateOption
            reportMailingJobStretchyReportParamDateOption) {
        EnumOptionData enumOptionData = null;
        
        if (reportMailingJobStretchyReportParamDateOption != null) {
            enumOptionData = new EnumOptionData(reportMailingJobStretchyReportParamDateOption.getId().longValue(), 
                    reportMailingJobStretchyReportParamDateOption.getCode(), reportMailingJobStretchyReportParamDateOption.getValue());
        }
        
        return enumOptionData;
    }
}
