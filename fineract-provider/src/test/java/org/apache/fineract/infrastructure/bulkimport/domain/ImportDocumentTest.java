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
package org.apache.fineract.infrastructure.bulkimport.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportDocumentTest {

    private static final LocalDateTime IMPORT_TIME = LocalDateTime.of(2026, 11, 5, 10, 0);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void newImportHasNoEndTimeUntilItFinishes() {
        final ImportDocument document = ImportDocument.instance(1L, IMPORT_TIME, 3, mock(AppUser.class), 100_000);

        assertThat(document.getImportTime()).isEqualTo(IMPORT_TIME);
        assertThat(document.getEndTime()).isNull();
        assertThat(document.getCompleted()).isFalse();
        assertThat(document.getSuccessCount()).isZero();
        assertThat(document.getFailureCount()).isZero();
    }

    @Test
    void finishingAnImportSetsEndTimeAndCounts() {
        final ImportDocument document = ImportDocument.instance(1L, IMPORT_TIME, 3, mock(AppUser.class), 100_000);
        final LocalDateTime endTime = IMPORT_TIME.plusMinutes(30);

        document.update(endTime, 99_990, 10);

        assertThat(document.getEndTime()).isEqualTo(endTime);
        assertThat(document.getCompleted()).isTrue();
        assertThat(document.getSuccessCount()).isEqualTo(99_990);
        assertThat(document.getFailureCount()).isEqualTo(10);
    }
}
