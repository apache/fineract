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
package org.apache.fineract.infrastructure.bulkimport.data;

import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;

public enum ImportFormatType {

    XLSX ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XLS ("application/vnd.ms-excel"),
    ODS ("application/vnd.oasis.opendocument.spreadsheet");


    private final String format;

    private ImportFormatType(String format) {
        this.format= format;
    }

    public String getFormat() {
        return format;
    }

    public static ImportFormatType of(String name) {
        for(ImportFormatType type : ImportFormatType.values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new GeneralPlatformDomainRuleException("error.msg.invalid.file.extension",
                "Uploaded file extension is not recognized.");
    }
}