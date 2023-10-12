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

import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.auditing.AuditableBeanWrapper;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.util.Assert;

public class CustomAuditingHandler extends AuditingHandler {

    /**
     * Creates a new {@link AuditableBeanWrapper} using the given {@link PersistentEntities} when looking up auditing
     * metadata via reflection.
     *
     * @param entities
     *            must not be {@literal null}.
     * @since 1.10
     */
    public CustomAuditingHandler(PersistentEntities entities) {
        super(entities);
    }

    /**
     * Creates a new {@link AuditableBeanWrapper} using the given {@link MappingContext} when looking up auditing
     * metadata via reflection.
     *
     * @param mappingContext
     *            must not be {@literal null}.
     * @since 1.8
     * @deprecated use {@link AuditingHandler(PersistentEntities)} instead.
     */
    public CustomAuditingHandler(MappingContext<? extends PersistentEntity<?, ?>, ? extends PersistentProperty<?>> mappingContext,
            AuditorAware<?> auditorAware) {
        this(PersistentEntities.of(mappingContext));
        setAuditorAware(auditorAware);
    }

    private DateTimeProvider fetchDateTimeProvider(Object bean) {
        if (bean instanceof AbstractAuditableWithUTCDateTimeCustom) {
            return CustomDateTimeProvider.UTC;
        } else {
            return CustomDateTimeProvider.INSTANCE;
        }
    }

    /**
     * Marks the given object as created.
     *
     * @param source
     *            must not be {@literal null}.
     */
    @NotNull
    @Override
    public <T> T markCreated(@NotNull T source) {
        Assert.notNull(source, "Source entity must not be null");
        setDateTimeProvider(fetchDateTimeProvider(source));
        return super.markCreated(source);
    }

    /**
     * Marks the given object as modified.
     *
     * @param source
     *            must not be {@literal null}.
     */
    @NotNull
    @Override
    public <T> T markModified(@NotNull T source) {
        Assert.notNull(source, "Source entity must not be null");
        setDateTimeProvider(fetchDateTimeProvider(source));
        return super.markModified(source);
    }
}
