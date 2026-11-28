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
package org.apache.fineract.infrastructure.campaigns.jobs;

import java.nio.file.Path;

public final class ReportOutputFiles {

    private ReportOutputFiles() {}

    /**
     * Resolves the output file for a generated report below the given root folder. Only letters, digits, space, dot,
     * underscore and hyphen are kept from the report name (anything else becomes an underscore) and leading dots are
     * stripped.
     *
     * @throws IllegalArgumentException
     *             if no usable file name remains or the resolved path would leave the root folder
     */
    public static Path resolve(final Path rootFolder, final String reportName, final String extension) {
        if (reportName == null) {
            throw new IllegalArgumentException("Report name is required");
        }
        final String safeName = reportName.replaceAll("[^A-Za-z0-9 ._-]", "_").replaceFirst("^\\.+", "");
        final String safeExtension = extension == null ? "" : extension.replaceAll("[^A-Za-z0-9]", "");
        if (safeName.isEmpty() || safeExtension.isEmpty()) {
            throw new IllegalArgumentException("Invalid report output file name");
        }
        final Path root = rootFolder.toAbsolutePath().normalize();
        final Path resolved = root.resolve(safeName + "." + safeExtension).normalize();
        if (!resolved.startsWith(root) || resolved.equals(root)) {
            throw new IllegalArgumentException("Report output file must be located within the content root folder");
        }
        return resolved;
    }
}
