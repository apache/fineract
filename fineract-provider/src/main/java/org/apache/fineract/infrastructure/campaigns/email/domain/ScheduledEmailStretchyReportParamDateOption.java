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
package org.apache.fineract.infrastructure.campaigns.email.domain;

public enum ScheduledEmailStretchyReportParamDateOption {
    INVALID(0, "scheduledEmailStretchyReportParamDateOption.invalid", "invalid"),
    TODAY(1, "scheduledEmailStretchyReportParamDateOption.today", "today"),
    // YESTERDAY(2, "scheduledEmailStretchyReportParamDateOption.yesterday", "yesterday"),
    TOMORROW(3, "scheduledEmailStretchyReportParamDateOption.tomorrow", "tomorrow");

    private String code;
    private String value;
    private Integer id;

    /**
     * @param id
     * @param code
     * @param value
     */
    private ScheduledEmailStretchyReportParamDateOption(final Integer id, final String code, final String value) {
        this.value = value;
        this.code = code;
        this.id = id;
    }
    
    /**
     * @param value
     * @return
     */
    public static ScheduledEmailStretchyReportParamDateOption instance(final String value) {
        ScheduledEmailStretchyReportParamDateOption scheduledEmailStretchyReportParamDateOption = INVALID;
        
        switch (value) {
            case "today":
                scheduledEmailStretchyReportParamDateOption = TODAY;
                break;
                
            // case "yesterday":
                // scheduledEmailStretchyReportParamDateOption = YESTERDAY;
                // break;
                
            case "tomorrow":
                scheduledEmailStretchyReportParamDateOption = TOMORROW;
                break;
        }
        
        return scheduledEmailStretchyReportParamDateOption;
    }
    
    /**
     * @param id
     * @return
     */
    public static ScheduledEmailStretchyReportParamDateOption instance(final Integer id) {
        ScheduledEmailStretchyReportParamDateOption scheduledEmailStretchyReportParamDateOption = INVALID;
        
        switch (id) {
            case 1:
                scheduledEmailStretchyReportParamDateOption = TODAY;
                break;
                
            // case 2:
                // scheduledEmailStretchyReportParamDateOption = YESTERDAY;
                // break;
                
            case 3:
                scheduledEmailStretchyReportParamDateOption = TOMORROW;
                break;
        }
        
        return scheduledEmailStretchyReportParamDateOption;
    }
    
    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    
    /** 
     * @return list of valid ScheduledEmailAttachmentFileFormat values
     **/
    public static Object[] validValues() {
        return new Object[] { TODAY.value, TOMORROW.value };
    }
}
