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
package org.springframework.data.auditing;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.auditing.CustomDateTimeProvider;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.util.Assert;

/**
 * Due to the package-private visibility of the Auditor, temporarely The CustomAuditingHandler must be placed in the
 * same package. Later when we don't need to distinct the Auditable entities by interface anymore, it will be reworked.
 */
public class CustomAuditingHandler extends AuditingHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuditingHandler.class);
    private final AuditableBeanWrapperFactory factory;
    private boolean dateTimeForNow = true;
    private boolean modifyOnCreation = true;

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
        this.factory = new MappingAuditableBeanWrapperFactory(entities);
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

    private Optional<TemporalAccessor> touchDate(AuditableBeanWrapper<?> wrapper, boolean isNew) {

        Assert.notNull(wrapper, "AuditableBeanWrapper must not be null");

        DateTimeProvider dateTimeProvider = fetchDateTimeProvider(wrapper.getBean());
        Optional<TemporalAccessor> now = dateTimeProvider.getNow();

        Assert.notNull(now, () -> String.format("Now must not be null Returned by: %s", dateTimeProvider.getClass()));

        now.filter(__ -> isNew).ifPresent(wrapper::setCreatedDate);
        now.filter(__ -> !isNew || modifyOnCreation).ifPresent(wrapper::setLastModifiedDate);

        return now;
    }

    private DateTimeProvider fetchDateTimeProvider(Object bean) {
        return bean instanceof AbstractAuditableWithUTCDateTimeCustom ? CustomDateTimeProvider.TENANT : CustomDateTimeProvider.INSTANCE;
    }

    /**
     * Marks the given object as created.
     *
     * @param auditor
     *            can be {@literal null}.
     * @param source
     *            must not be {@literal null}.
     */
    @Override
    <T> T markCreated(Auditor auditor, T source) {

        Assert.notNull(source, "Source entity must not be null");

        return touch(auditor, source, true);
    }

    /**
     * Marks the given object as modified.
     *
     * @param auditor
     * @param source
     */
    @Override
    <T> T markModified(Auditor auditor, T source) {

        Assert.notNull(source, "Source entity must not be null");

        return touch(auditor, source, false);
    }

    private <T> T touch(Auditor auditor, T target, boolean isNew) {

        Optional<AuditableBeanWrapper<T>> wrapper = factory.getBeanWrapperFor(target);

        return wrapper.map(it -> {

            touchAuditor(auditor, it, isNew);
            Optional<TemporalAccessor> now = dateTimeForNow ? touchDate(it, isNew) : Optional.empty();

            if (LOG.isDebugEnabled()) {

                Object defaultedNow = now.map(Object::toString).orElse("not set");
                Object defaultedAuditor = auditor.isPresent() ? auditor.toString() : "unknown";

                LOG.debug("Touched {} - Last modification at {} by {}", target, defaultedNow, defaultedAuditor);
            }

            return it.getBean();
        }).orElse(target);
    }

    /**
     * Sets modifying and creating auditor. Creating auditor is only set on new auditables.
     *
     * @param auditor
     * @param wrapper
     * @param isNew
     * @return
     */
    private void touchAuditor(Auditor auditor, AuditableBeanWrapper<?> wrapper, boolean isNew) {

        if (!auditor.isPresent()) {
            return;
        }

        Assert.notNull(wrapper, "AuditableBeanWrapper must not be null");

        if (isNew) {
            wrapper.setCreatedBy(auditor.getValue());
        }

        if (!isNew || modifyOnCreation) {
            wrapper.setLastModifiedBy(auditor.getValue());
        }
    }
}
