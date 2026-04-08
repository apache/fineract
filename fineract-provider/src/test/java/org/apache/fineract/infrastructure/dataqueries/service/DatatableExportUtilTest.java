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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.service.export.DatatableExportUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatatableExportUtilTest {

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void emptyFolderTest() {
        assertEquals("", DatatableExportUtil.normalizeFolderName(""));
        assertEquals("", DatatableExportUtil.normalizeFolderName("/"));
        assertEquals("", DatatableExportUtil.normalizeFolderName(null));
    }

    @Test
    public void specialCharacterFolderTest() {
        assertEquals("_", DatatableExportUtil.normalizeFolderName("Á"));
        assertEquals("_", DatatableExportUtil.normalizeFolderName("="));
        assertEquals("_", DatatableExportUtil.normalizeFolderName("\\"));
        assertEquals("_", DatatableExportUtil.normalizeFolderName("@"));
    }

    @Test
    public void normalizedFolderNameTest() {
        assertEquals("$", DatatableExportUtil.normalizeFolderName("$"));
        assertEquals("reports", DatatableExportUtil.normalizeFolderName("reports"));
        assertEquals("reports", DatatableExportUtil.normalizeFolderName("reports/"));
        assertEquals("reports", DatatableExportUtil.normalizeFolderName("/reports/"));
        assertEquals("reports/content", DatatableExportUtil.normalizeFolderName("reports/content"));
        assertEquals("reports/content", DatatableExportUtil.normalizeFolderName("reports/////content"));
    }

    @Test
    public void generateDatatableExportFileNameSuccessTest() {
        String reportName = "reportName";
        Map<String, String> reportParams = Collections.synchronizedSortedMap(new TreeMap<>(Map.of("param1", "value1", "param2", "value2")));
        String fileName = DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder", "csv", reportName, reportParams);
        assertTrue(fileName.matches("folder/reportName\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
    }

    @Test
    public void generateDatatableExportFileNameComplexTest() {
        String reportName = "reportName";
        Map<String, String> reportParams = Collections.synchronizedSortedMap(new TreeMap<>(Map.of("param1", "value1", "param2", "value2")));
        assertTrue(DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name///", "csv", reportName, reportParams)
                .matches("folder/name/reportName\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
        IllegalArgumentException folderTooLongException = assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", "csv", reportName, reportParams);
        });
        assertEquals("The folder name is too long", folderTooLongException.getMessage());

        IllegalArgumentException maximumLengthException = assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(29, "folder///name/", "csv", reportName, reportParams);
        });
        assertEquals("The maximum length must be greater than 30", maximumLengthException.getMessage());

        IllegalArgumentException extensionRequired = assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", null, reportName, reportParams);
        });
        assertEquals("The extension is required", extensionRequired.getMessage());

        IllegalArgumentException reportNameRequired = assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", "csv", null, reportParams);
        });
        assertEquals("The report name is required", reportNameRequired.getMessage());

        assertTrue(DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name/", ".csv", reportName, null)
                .matches("folder/name/reportName_\\d{14}.csv"));

        assertTrue(DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name/", "csv", "report/with/slash", reportParams)
                .matches("folder/name/report_with_slash\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
    }

}
