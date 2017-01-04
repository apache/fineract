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

public enum ReportMailingJobEmailAttachmentFileFormat {
    INVALID(0, "ReportMailingJobEmailAttachmentFileFormat.INVALID", "Invalid"),
    XLS(1, "ReportMailingJobEmailAttachmentFileFormat.XLS", "XLS"),
    PDF(2, "ReportMailingJobEmailAttachmentFileFormat.PDF", "PDF"),
    CSV(3, "ReportMailingJobEmailAttachmentFileFormat.CSV", "CSV");
    
    private String code;
    private String value;
    private Integer id;
    
    private ReportMailingJobEmailAttachmentFileFormat(final Integer id, final String code, final String value) {
        this.value = value;
        this.code = code;
        this.id = id;
    }
    
    /**
     * Creates a new {@link ReportMailingJobEmailAttachmentFileFormat} object
     * 
     * @param value the value of the enum constant
     * @return {@link ReportMailingJobEmailAttachmentFileFormat} object
     */
    public static ReportMailingJobEmailAttachmentFileFormat newInstance(final String value) {
        ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = INVALID;
        
        if (StringUtils.equalsIgnoreCase(value, XLS.value)) {
            emailAttachmentFileFormat = XLS;
        } else if (StringUtils.equalsIgnoreCase(value, PDF.value)) {
            emailAttachmentFileFormat = PDF;
        } else if (StringUtils.equalsIgnoreCase(value, CSV.value)) {
            emailAttachmentFileFormat = CSV;
        }
        
        return emailAttachmentFileFormat;
    }
    
    /**
     * Creates a new {@link ReportMailingJobEmailAttachmentFileFormat} object
     * 
     * @param id the id of the enum constant
     * @return {@link ReportMailingJobEmailAttachmentFileFormat} object
     */
    public static ReportMailingJobEmailAttachmentFileFormat newInstance(final Integer id) {
        ReportMailingJobEmailAttachmentFileFormat emailAttachmentFileFormat = INVALID;
        
        if (id == XLS.id) {
            emailAttachmentFileFormat = XLS;
        } else if (id == PDF.id) {
            emailAttachmentFileFormat = PDF;
        } else if (id == CSV.id) {
            emailAttachmentFileFormat = CSV;
        }
        
        return emailAttachmentFileFormat;
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
     * @return list of valid ReportMailingJobEmailAttachmentFileFormat values
     **/
    public static Object[] validValues() {
        List<Object> validValues = new ArrayList<>();
        
        for (ReportMailingJobEmailAttachmentFileFormat constant : ReportMailingJobEmailAttachmentFileFormat.values()) {
            if (constant.isValid()) {
                validValues.add(constant.value);
            }
        }
        
        return validValues.toArray();
    }
    
    /** 
     * @return list of valid ReportMailingJobEmailAttachmentFileFormat values
     **/
    public static Object[] validIds() {
        List<Object> validValues = new ArrayList<>();
        
        for (ReportMailingJobEmailAttachmentFileFormat constant : ReportMailingJobEmailAttachmentFileFormat.values()) {
            if (constant.isValid()) {
                validValues.add(constant.id);
            }
        }
        
        return validValues.toArray();
    }
    
    /**
     * get a {@link EnumOptionData} representation of the {@link ReportMailingJobEmailAttachmentFileFormat} object
     * 
     * @return {@link EnumOptionData} object
     */
    public EnumOptionData toEnumOptionData() {
        // get the long value of the enum id
        final Long id = (this.id != null) ? this.id.longValue() : null;
        
        return new EnumOptionData(id, code, value);
    }
    
    /**
     * get the {@link EnumOptionData} representation of all valid {@link ReportMailingJobEmailAttachmentFileFormat} objects
     * 
     * @return List of {@link EnumOptionData} objects
     */
    public static List<EnumOptionData> validOptions() {
        List<EnumOptionData> options = new ArrayList<>();
        
        for (ReportMailingJobEmailAttachmentFileFormat constant : ReportMailingJobEmailAttachmentFileFormat.values()) {
            if (constant.isValid()) {
                options.add(constant.toEnumOptionData());
            }
        }
        
        return options;
    }
}
