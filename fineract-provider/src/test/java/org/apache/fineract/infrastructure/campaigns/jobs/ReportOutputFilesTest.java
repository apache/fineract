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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportOutputFilesTest {

    @TempDir
    private Path root;

    @Test
    void plainReportNameResolvesBelowRoot() {
        assertEquals(root.resolve("Client Listing.pdf"), ReportOutputFiles.resolve(root, "Client Listing", "pdf"));
    }

    @Test
    void pathSeparatorsAreReplaced() {
        final Path resolved = ReportOutputFiles.resolve(root, "../../reports/monthly/job", "csv");
        assertEquals(root, resolved.getParent());
        assertEquals("_.._reports_monthly_job.csv", resolved.getFileName().toString());
    }

    @Test
    void backslashesAreReplaced() {
        final Path resolved = ReportOutputFiles.resolve(root, "..\\..\\summary", "xls");
        assertEquals(root, resolved.getParent());
        assertEquals("_.._summary.xls", resolved.getFileName().toString());
    }

    @Test
    void leadingDotsAreStripped() {
        assertEquals(root.resolve("hidden.pdf"), ReportOutputFiles.resolve(root, "...hidden", "pdf"));
    }

    @Test
    void emptyOrNullNamesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReportOutputFiles.resolve(root, null, "pdf"));
        assertThrows(IllegalArgumentException.class, () -> ReportOutputFiles.resolve(root, "", "pdf"));
        assertThrows(IllegalArgumentException.class, () -> ReportOutputFiles.resolve(root, "...", "pdf"));
    }

    @Test
    void invalidExtensionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReportOutputFiles.resolve(root, "report", "../"));
    }
}
