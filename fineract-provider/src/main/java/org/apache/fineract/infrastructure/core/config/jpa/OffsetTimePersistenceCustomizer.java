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
package org.apache.fineract.infrastructure.core.config.jpa;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionCustomizer;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

@Component
public class OffsetTimePersistenceCustomizer implements EntityManagerFactoryCustomizer {

    @Override
    public Map<String, Object> additionalVendorProperties() {
        return Map.of(PersistenceUnitProperties.SESSION_CUSTOMIZER, (SessionCustomizer) this::customize);
    }

    private void customize(Session session) {
        configureTransactionTime(session.getDescriptor(SavingsAccountTransaction.class));
        configureTransactionTime(session.getDescriptor(AccountTransferTransaction.class));
    }

    private void configureTransactionTime(ClassDescriptor descriptor) {
        DatabaseMapping mapping = descriptor.getMappingForAttributeName("transactionTime");
        if (!(mapping instanceof AbstractDirectMapping directMapping)) {
            throw new IllegalStateException("transactionTime must be mapped to a database column");
        }
        directMapping.setFieldClassification(Object.class);
        directMapping.setConverter(new OffsetTimeConverter());
    }

    static final class OffsetTimeConverter implements Converter {

        @Override
        public Object convertObjectValueToDataValue(Object value, Session session) {
            if (value == null && session.getPlatform() instanceof PostgreSQLPlatform) {
                return postgreSqlTimeWithTimeZone(null);
            }
            if (!(value instanceof OffsetTime offsetTime)) {
                return value;
            }
            if (session.getPlatform() instanceof PostgreSQLPlatform) {
                return postgreSqlTimeWithTimeZone(offsetTime.toString());
            }
            return Time.valueOf(offsetTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime());
        }

        private PGobject postgreSqlTimeWithTimeZone(String value) {
            PGobject result = new PGobject();
            result.setType("timetz");
            try {
                result.setValue(value);
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to convert OffsetTime to PostgreSQL timetz", exception);
            }
            return result;
        }

        @Override
        public OffsetTime convertDataValueToObjectValue(Object value, Session session) {
            return switch (value) {
                case null -> null;
                case OffsetTime offsetTime -> offsetTime;
                case LocalTime localTime -> localTime.atOffset(ZoneOffset.UTC);
                case Time time -> time.toLocalTime().atOffset(ZoneOffset.UTC);
                default -> OffsetTime.parse(value.toString());
            };
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public void initialize(DatabaseMapping mapping, Session session) {}
    }
}
