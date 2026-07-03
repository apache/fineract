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
package org.apache.fineract.infrastructure.bulkimport.populator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class AbstractWorkbookPopulatorSanitizeTest {

    // setSanitized is protected; expose it through a minimal concrete subclass.
    private static final class TestPopulator extends AbstractWorkbookPopulator {

        @Override
        public void populate(Workbook workbook, String dateFormat) {
            // no-op
        }

        void sanitize(Name name, String roughName) {
            setSanitized(name, roughName);
        }
    }

    @Test
    void setSanitized_stripsApostropheIntoValidExcelName() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            Name name = workbook.createName();
            TestPopulator populator = new TestPopulator();
            // A client display name with an apostrophe used to produce 'Account_IRE'S_LIMITED_181_', which POI rejects.
            assertDoesNotThrow(() -> populator.sanitize(name, "Account_IRE'S LIMITED_181_"));
            String sanitized = name.getNameName();
            assertFalse(sanitized.contains("'"), "apostrophe must be stripped: " + sanitized);
            assertFalse(sanitized.contains(" "), "space must be stripped: " + sanitized);
            assertTrue(sanitized.matches("[\\p{L}\\p{N}._]+"), "only letter/digit/period/underscore allowed: " + sanitized);
        }
    }

    @Test
    void setSanitized_keepsUnicodeLetters() throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            Name name = workbook.createName();
            assertDoesNotThrow(() -> new TestPopulator().sanitize(name, "Client_Zoé_42_"));
            assertTrue(name.getNameName().contains("Zoé"), "accented letters must be preserved: " + name.getNameName());
        }
    }
}
