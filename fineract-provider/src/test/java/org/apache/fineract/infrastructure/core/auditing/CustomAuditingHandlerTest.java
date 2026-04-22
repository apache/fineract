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
package org.apache.fineract.infrastructure.core.auditing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;

public class CustomAuditingHandlerTest {

    @BeforeEach
    public void init() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.of("Asia/Kolkata")))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void markCreated() {
        MappingContext mappingContext = Mockito.mock(MappingContext.class);
        CustomAuditingHandler testInstance = new CustomAuditingHandler(PersistentEntities.of(mappingContext));
        AbstractAuditableWithUTCDateTimeCustom<Long> targetObject = Mockito.spy(AbstractAuditableWithUTCDateTimeCustom.class);
        targetObject = testInstance.markCreated(targetObject);
        OffsetDateTime now = DateUtils.getAuditOffsetDateTime();

        assertTrue(targetObject.getCreatedDate().isPresent());
        assertEquals(now.getYear(), targetObject.getCreatedDate().get().getYear());
        assertEquals(now.getMonth(), targetObject.getCreatedDate().get().getMonth());
        assertEquals(now.getDayOfMonth(), targetObject.getCreatedDate().get().getDayOfMonth());
        assertEquals(now.getHour(), targetObject.getCreatedDate().get().getHour());
        assertEquals(now.getMinute(), targetObject.getCreatedDate().get().getMinute());
    }

    @Test
    public void markModified() {
        MappingContext mappingContext = Mockito.mock(MappingContext.class);
        CustomAuditingHandler testInstance = new CustomAuditingHandler(PersistentEntities.of(mappingContext));
        AbstractAuditableWithUTCDateTimeCustom<Long> targetObject = Mockito.spy(AbstractAuditableWithUTCDateTimeCustom.class);
        targetObject = testInstance.markModified(targetObject);
        OffsetDateTime now = DateUtils.getAuditOffsetDateTime();

        assertTrue(targetObject.getLastModifiedDate().isPresent());
        assertEquals(now.getYear(), targetObject.getLastModifiedDate().get().getYear());
        assertEquals(now.getMonth(), targetObject.getLastModifiedDate().get().getMonth());
        assertEquals(now.getDayOfMonth(), targetObject.getLastModifiedDate().get().getDayOfMonth());
        assertEquals(now.getHour(), targetObject.getLastModifiedDate().get().getHour());
        assertEquals(now.getMinute(), targetObject.getLastModifiedDate().get().getMinute());
    }

    @Test
    public void markModifiedOldDateTimeProvider() {
        MappingContext mappingContext = Mockito.mock(MappingContext.class);
        CustomAuditingHandler testInstance = new CustomAuditingHandler(PersistentEntities.of(mappingContext));
        AbstractAuditableCustom targetObject = Mockito.spy(AbstractAuditableCustom.class);
        targetObject = testInstance.markModified(targetObject);
        LocalDateTime now = DateUtils.getLocalDateTimeOfSystem();

        assertTrue(targetObject.getLastModifiedDate().isPresent());
        assertEquals(now.getYear(), targetObject.getLastModifiedDate().get().getYear());
        assertEquals(now.getMonth(), targetObject.getLastModifiedDate().get().getMonth());
        assertEquals(now.getDayOfMonth(), targetObject.getLastModifiedDate().get().getDayOfMonth());
        assertEquals(now.getHour(), targetObject.getLastModifiedDate().get().getHour());
        assertEquals(now.getMinute(), targetObject.getLastModifiedDate().get().getMinute());
    }

    @Test
    public void markCreatedOldDateTimeProvider() {
        MappingContext mappingContext = Mockito.mock(MappingContext.class);
        CustomAuditingHandler testInstance = new CustomAuditingHandler(PersistentEntities.of(mappingContext));
        AbstractAuditableCustom targetObject = Mockito.spy(AbstractAuditableCustom.class);
        targetObject = testInstance.markCreated(targetObject);
        LocalDateTime now = DateUtils.getLocalDateTimeOfSystem();

        assertTrue(targetObject.getCreatedDate().isPresent());
        assertEquals(now.getYear(), targetObject.getCreatedDate().get().getYear());
        assertEquals(now.getMonth(), targetObject.getCreatedDate().get().getMonth());
        assertEquals(now.getDayOfMonth(), targetObject.getCreatedDate().get().getDayOfMonth());
        assertEquals(now.getHour(), targetObject.getCreatedDate().get().getHour());
        assertEquals(now.getMinute(), targetObject.getCreatedDate().get().getMinute());
    }
}
