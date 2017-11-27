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

public enum ScheduledEmailAttachmentFileFormat {
    INVALID(0, "EmailAttachmentFileFormat.invalid", "invalid"),
    XLS(1, "EmailAttachmentFileFormat.xls", "xls"),
    PDF(2, "EmailAttachmentFileFormat.pdf", "pdf"),
    CSV(3, "EmailAttachmentFileFormat.csv", "csv");

    private String code;
    private String value;
    private Integer id;

    ScheduledEmailAttachmentFileFormat(final Integer id, final String code, final String value) {
        this.value = value;
        this.code = code;
        this.id = id;
    }
    
    public static ScheduledEmailAttachmentFileFormat instance(final String value) {
        ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat = INVALID;
        
        switch (value) {
            case "xls":
                emailAttachmentFileFormat = XLS;
                break;
                
            case "pdf":
                emailAttachmentFileFormat = PDF;
                break;
                
            case "csv":
                emailAttachmentFileFormat = CSV;
                break;
                
            default:
                break;
        }
        
        return emailAttachmentFileFormat;
    }
    
    public static ScheduledEmailAttachmentFileFormat instance(final Integer id) {
        ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat = INVALID;
        
        switch (id) {
            case 1:
                emailAttachmentFileFormat = XLS;
                break;
                
            case 2:
                emailAttachmentFileFormat = PDF;
                break;
                
            case 3:
                emailAttachmentFileFormat = CSV;
                break;
                
            default:
                break;
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
     * @return list of valid ScheduledEmailAttachmentFileFormat ids
     **/
    public static Object[] validValues() {
        return new Object[] { XLS.getId(), PDF.getId(), CSV.getId() };
    }
}
