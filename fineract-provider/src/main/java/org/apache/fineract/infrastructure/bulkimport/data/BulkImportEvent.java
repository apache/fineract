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

import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.domain.FineractEvent;
import org.apache.poi.ss.usermodel.Workbook;

public final class BulkImportEvent extends FineractEvent {

    private final Workbook workbook;

    private final Long importId;

    private final String locale;

    private final String dateFormat;

    private BulkImportEvent(final Object source, final Workbook workbook, final Long importId, final String locale, final String dateFormat,
            FineractContext context) {
        super(source, context);
        this.workbook = workbook;
        this.importId = importId;
        this.locale = locale;
        this.dateFormat = dateFormat;
    }

    public static BulkImportEvent instance(final Object source, final Workbook workbook, final Long importId, final String locale,
            final String dateFormat, FineractContext context) {
        return new BulkImportEvent(source, workbook, importId, locale, dateFormat, context);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Long getImportId() {
        return importId;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getLocale() {
        return locale;
    }

}
