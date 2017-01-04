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
package org.apache.fineract.infrastructure.reportmailingjob.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum ReportMailingJobStretchyReportParamDateOption {
    INVALID(0, "ReportMailingJobStretchyReportParamDateOption.INVALID", "Invalid"),
    TODAY(1, "ReportMailingJobStretchyReportParamDateOption.TODAY", "Today"),
    YESTERDAY(2, "ReportMailingJobStretchyReportParamDateOption.YESTERDAY", "Yesterday"),
    TOMORROW(3, "ReportMailingJobStretchyReportParamDateOption.TOMORROW", "Tomorrow");
    
    private String code;
    private String value;
    private Integer id;
    
    /**
     * @param id
     * @param code
     * @param value
     */
    private ReportMailingJobStretchyReportParamDateOption(final Integer id, final String code, final String value) {
        this.value = value;
        this.code = code;
        this.id = id;
    }
    
    /**
     * Creates a new {@link ReportMailingJobStretchyReportParamDateOption} object
     * 
     * @param value value of the enum constant
     * @return {@link ReportMailingJobStretchyReportParamDateOption} object
     */
    public static ReportMailingJobStretchyReportParamDateOption newInstance(final String value) {
        ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption = INVALID;
        
        if (StringUtils.equalsIgnoreCase(value, TODAY.value)) {
            reportMailingJobStretchyReportParamDateOption = TODAY;
        } else if (StringUtils.equalsIgnoreCase(value, YESTERDAY.value)) {
            reportMailingJobStretchyReportParamDateOption = YESTERDAY;
        } else if (StringUtils.equalsIgnoreCase(value, TOMORROW.value)) {
            reportMailingJobStretchyReportParamDateOption = TOMORROW;
        }
        
        return reportMailingJobStretchyReportParamDateOption;
    }
    
    /**
     * Creates a new {@link ReportMailingJobStretchyReportParamDateOption} object
     * 
     * @param id id of the enum constant
     * @return {@link ReportMailingJobStretchyReportParamDateOption} object
     */
    public static ReportMailingJobStretchyReportParamDateOption newInstance(final Integer id) {
        ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption = INVALID;
        
        if (id == TODAY.id) {
            reportMailingJobStretchyReportParamDateOption = TODAY;
        } else if (id == YESTERDAY.id) {
            reportMailingJobStretchyReportParamDateOption = YESTERDAY;
        } else if (id == TOMORROW.id) {
            reportMailingJobStretchyReportParamDateOption = TOMORROW;
        }
        
        return reportMailingJobStretchyReportParamDateOption;
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
     * @return true if the enum is equals to "INVALID"
     */
    public boolean isInvalid() {
        return this.equals(INVALID);
    }
    
    /**
     * @return true if the enum is not equals to "INVALID"
     */
    public boolean isValid() {
        return !this.isInvalid();
    }
    
    /** 
     * @return list of valid ReportMailingJobStretchyReportParamDateOption values
     **/
    public static Object[] validValues() {
        List<Object> validValues = new ArrayList<>();
        
        for (ReportMailingJobStretchyReportParamDateOption constant :  ReportMailingJobStretchyReportParamDateOption.values()) {
            if (constant.isValid()) {
                validValues.add(constant.value);
            }
        }
        
        return validValues.toArray();
    }
    
    /**
     * get a {@link EnumOptionData} representation of the {@link ReportMailingJobStretchyReportParamDateOption} object
     * 
     * @return {@link EnumOptionData} object
     */
    public EnumOptionData toEnumOptionData() {
        // get the long value of the enum id
        final Long id = (this.id != null) ? this.id.longValue() : null;
        
        return new EnumOptionData(id, code, value);
    }
    
    /**
     * get the {@link EnumOptionData} representation of all valid {@link ReportMailingJobStretchyReportParamDateOption} objects
     * 
     * @return List of {@link EnumOptionData} objects
     */
    public static List<EnumOptionData> validOptions() {
        List<EnumOptionData> options = new ArrayList<>();
        
        for (ReportMailingJobStretchyReportParamDateOption constant : ReportMailingJobStretchyReportParamDateOption.values()) {
            if (constant.isValid()) {
                options.add(constant.toEnumOptionData());
            }
        }
        
        return options;
    }
}
