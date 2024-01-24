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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.service.export.DatatableExportUtil;
import org.junit.Assert;
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
        Assert.assertEquals("", DatatableExportUtil.normalizeFolderName(""));
        Assert.assertEquals("", DatatableExportUtil.normalizeFolderName("/"));
        Assert.assertEquals("", DatatableExportUtil.normalizeFolderName(null));
    }

    @Test
    public void specialCharacterFolderTest() {
        Assert.assertEquals("_", DatatableExportUtil.normalizeFolderName("Á"));
        Assert.assertEquals("_", DatatableExportUtil.normalizeFolderName("="));
        Assert.assertEquals("_", DatatableExportUtil.normalizeFolderName("\\"));
        Assert.assertEquals("_", DatatableExportUtil.normalizeFolderName("@"));
    }

    @Test
    public void normalizedFolderNameTest() {
        Assert.assertEquals("$", DatatableExportUtil.normalizeFolderName("$"));
        Assert.assertEquals("reports", DatatableExportUtil.normalizeFolderName("reports"));
        Assert.assertEquals("reports", DatatableExportUtil.normalizeFolderName("reports/"));
        Assert.assertEquals("reports", DatatableExportUtil.normalizeFolderName("/reports/"));
        Assert.assertEquals("reports/content", DatatableExportUtil.normalizeFolderName("reports/content"));
        Assert.assertEquals("reports/content", DatatableExportUtil.normalizeFolderName("reports/////content"));
    }

    @Test
    public void generateDatatableExportFileNameSuccessTest() {
        String reportName = "reportName";
        Map<String, String> reportParams = Collections.synchronizedSortedMap(new TreeMap<>(Map.of("param1", "value1", "param2", "value2")));
        String fileName = DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder", "csv", reportName, reportParams);
        Assert.assertTrue(fileName.matches("folder/reportName\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
    }

    @Test
    public void generateDatatableExportFileNameComplexTest() {
        String reportName = "reportName";
        Map<String, String> reportParams = Collections.synchronizedSortedMap(new TreeMap<>(Map.of("param1", "value1", "param2", "value2")));
        Assert.assertTrue(DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name///", "csv", reportName, reportParams)
                .matches("folder/name/reportName\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
        IllegalArgumentException folderTooLongException = Assert.assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", "csv", reportName, reportParams);
        });
        Assert.assertEquals("The folder name is too long", folderTooLongException.getMessage());

        IllegalArgumentException maximumLengthException = Assert.assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(29, "folder///name/", "csv", reportName, reportParams);
        });
        Assert.assertEquals("The maximum length must be greater than 30", maximumLengthException.getMessage());

        IllegalArgumentException extensionRequired = Assert.assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", null, reportName, reportParams);
        });
        Assert.assertEquals("The extension is required", extensionRequired.getMessage());

        IllegalArgumentException reportNameRequired = Assert.assertThrows(IllegalArgumentException.class, () -> {
            DatatableExportUtil.generateS3DatatableExportFileName(30, "too_long_folder_name_test", "csv", null, reportParams);
        });
        Assert.assertEquals("The report name is required", reportNameRequired.getMessage());

        Assert.assertTrue(DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name/", ".csv", reportName, null)
                .matches("folder/name/reportName_\\d{14}.csv"));

        Assert.assertTrue(
                DatatableExportUtil.generateS3DatatableExportFileName(1024, "folder///name/", "csv", "report/with/slash", reportParams)
                        .matches("folder/name/report_with_slash\\(param1_value1;param2_value2\\)_\\d{14}.csv"));
    }

}
