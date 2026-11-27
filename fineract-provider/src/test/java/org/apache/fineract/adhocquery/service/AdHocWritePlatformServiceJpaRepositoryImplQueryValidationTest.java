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
package org.apache.fineract.adhocquery.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.adhocquery.domain.AdHoc;
import org.apache.fineract.adhocquery.domain.AdHocRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdHocWritePlatformServiceJpaRepositoryImplQueryValidationTest {

    private static final Long ADHOC_ID = 7L;
    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AdHocRepository adHocRepository;
    @Mock
    private AdHocDataValidator adHocDataValidator;
    @Mock
    private SqlValidator sqlValidator;

    @InjectMocks
    private AdHocWritePlatformServiceJpaRepositoryImpl service;

    @Test
    void updateValidatesTheQueryWhenPresent() {
        existing();

        service.updateAdHocQuery(ADHOC_ID, command("{\"query\":\"select 2\"}"));

        verify(sqlValidator).validate("adhoc", "select 2");
    }

    @Test
    void updateRejectsAnInvalidQueryWithoutSaving() {
        doThrow(new IllegalArgumentException("invalid")).when(sqlValidator).validate("adhoc", "select 2; drop table t");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateAdHocQuery(ADHOC_ID, command("{\"query\":\"select 2; drop table t\"}")));

        verify(adHocRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateWithoutAQueryDoesNotValidateSql() {
        existing();

        service.updateAdHocQuery(ADHOC_ID, command("{\"name\":\"renamed\"}"));

        verify(sqlValidator, never()).validate(any(), any());
    }

    private AdHoc existing() {
        final AdHoc adHoc = new AdHoc().setName("q").setQuery("select 1").setTableName("t").setTableFields("a").setEmail("ops@example.com")
                .setReportRunFrequency(2L).setReportRunEvery(3L).setActive(true);
        when(adHocRepository.findById(ADHOC_ID)).thenReturn(Optional.of(adHoc));
        return adHoc;
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
