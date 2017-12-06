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

import org.joda.time.LocalDate;

public class ImportData {

    @SuppressWarnings("unused")
    private Long importId;
    @SuppressWarnings("unused")
    private Long documentId;
    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private LocalDate importTime;
    @SuppressWarnings("unused")
    private LocalDate endTime;
    @SuppressWarnings("unused")
    private Boolean completed;
    @SuppressWarnings("unused")
    private Long createdBy;
    @SuppressWarnings("unused")
    private Integer totalRecords;
    @SuppressWarnings("unused")
    private Integer successCount;
    @SuppressWarnings("unused")
    private Integer failureCount;

    public static ImportData instance(final Long importId, final Long documentId,
                                      final LocalDate importTime, final LocalDate endTime,
                                      final Boolean completed, final String name,
                                      final Long createdBy, final Integer totalRecords, final Integer successCount,
                                      final Integer failureCount) {
        return new ImportData(importId, documentId, importTime, endTime,
                completed, name, createdBy, totalRecords, successCount,
                failureCount);
    }

    public  static ImportData instance(final Long importId){
        return new ImportData(importId,null,null,
                null,null,null,null,null,
                null,null);
    }

    private ImportData(final Long importId, final Long documentId,
                       final LocalDate importTime, final LocalDate endTime,
                       final Boolean completed, final String name,
                       final Long createdBy, final Integer totalRecords, final Integer successCount,
                       final Integer failureCount) {
        this.importId = importId;
        this.documentId = documentId;
        this.name = name;
        this.importTime = importTime;
        this.endTime = endTime;
        this.completed = completed;
        this.createdBy = createdBy;
        this.totalRecords = totalRecords;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }


}