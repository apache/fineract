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
package org.apache.fineract.infrastructure.dataqueries.service.export;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.jetbrains.annotations.NotNull;

public final class DatatableExportUtil {

    private DatatableExportUtil() {}

    public static String normalizeFolderName(String folderName) {
        if (StringUtils.isBlank(folderName)) {
            return "";
        }
        String contentNormalizer = folderName.trim() //
                .replaceAll("[^a-zA-Z0-9!\\-_.'()$/]", "_") // replace special characters
                .replaceAll("/+", "/") // replace multiply / with a single /
                .replaceAll("^[./]+", ""); // remove leading . and /

        return contentNormalizer.endsWith("/") ? contentNormalizer.substring(0, contentNormalizer.length() - 1) : contentNormalizer;
    }

    public static String generatePlainExportFileName(int maxLength, String extension, String reportName, Map<String, String> reportParams) {
        exportBasicValidation(extension, reportName);
        return generateReportFileName(maxLength, "", extension, reportName, reportParams);
    }

    private static void exportBasicValidation(String extension, String reportName) {
        if (StringUtils.isBlank(extension)) {
            throw new IllegalArgumentException("The extension is required");
        }
        if (StringUtils.isBlank(reportName)) {
            throw new IllegalArgumentException("The report name is required");
        }
    }

    public static String generateS3DatatableExportFileName(int maxLength, String folder, String extension, String reportName,
            Map<String, String> reportParams) {
        exportBasicValidation(extension, reportName);
        if (maxLength < 30) {
            throw new IllegalArgumentException("The maximum length must be greater than 30");
        }
        folder = normalizeFolderName(folder);
        String reportFinalName = generateReportFileName(maxLength, folder, extension, reportName, reportParams);
        if (StringUtils.isBlank(folder)) {
            return reportFinalName;
        } else {
            return folder + "/" + reportFinalName;
        }
    }

    @NotNull
    private static String generateReportFileName(int maxLength, String folder, String extension, String reportName,
            Map<String, String> reportParams) {
        String extensionWithDot = extension.startsWith(".") ? extension : "." + extension;
        String timestamp = "_" + DateUtils.getOffsetDateTimeOfTenant().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int reportMaximumFileName = maxLength - folder.length() - timestamp.length() - extensionWithDot.length() - 1;
        if (reportMaximumFileName < 0) {
            throw new IllegalArgumentException("The folder name is too long");
        }
        String normalizedFileName = reportName.trim().replaceAll("[^a-zA-Z0-9!\\-_.'()$]", "_");
        if (reportParams != null) {
            normalizedFileName += "(" + reportParams.entrySet().stream()
                    .map(entry -> extractReportParameterKey(entry.getKey()) + "_" + entry.getValue()).collect(Collectors.joining(";"))
                    + ")";
        }
        String reportFinalName = normalizedFileName.substring(0, Math.min(normalizedFileName.length(), reportMaximumFileName)) + timestamp
                + extensionWithDot;
        return reportFinalName;
    }

    private static String extractReportParameterKey(String key) {
        return key.startsWith("${") && key.endsWith("}") ? key.substring(2, key.length() - 1) : key;
    }
}
