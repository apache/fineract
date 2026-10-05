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
package org.apache.fineract.infrastructure.jobs;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.apache.fineract.infrastructure.jobs.config.FineractDataFieldMaxValueIncrementerFactory;
import org.apache.fineract.infrastructure.springbatch.FineractJobRepository;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.infrastructure.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration(proxyBeanMethods = false)
@EnableBatchProcessing
public class ScheduledJobRunnerConfig {

    // mirrors the serializer's default type validator, extended with Fineract types, because
    // execution contexts carry e.g. the COB business-step set and partition parameters
    private static final List<String> ALLOWED_EXECUTION_CONTEXT_SUBTYPES = List.of(//
            "java.util.", //
            "java.sql.", //
            "java.lang.", //
            "java.math.", //
            "java.time.", //
            "java.net.", //
            "java.xml.", //
            "org.springframework.batch.", //
            "org.apache.fineract.");

    @Bean
    public JobRegistry jobRegistry() {
        // @EnableBatchProcessing's registrar wires its jobOperator against the bean named
        // "jobRegistry" but does not define one itself
        return new MapJobRegistry();
    }

    @Bean
    public JacksonExecutionContextStringSerializer executionContextSerializer() {
        return new JacksonExecutionContextStringSerializer(executionContextMapper());
    }

    /**
     * Writes and reads execution contexts in the Spring Batch 5 wire format, so that Batch 5 and Batch 6 instances can
     * share a database and no context has to be discarded on upgrade.
     * <p>
     * Batch 5's {@code Jackson2ExecutionContextStringSerializer} used {@code NON_FINAL} typing written as an
     * {@code @class} property. Batch 6 defaults to {@code OBJECT_AND_NON_CONCRETE} written as a wrapper array, which
     * differs twice over: type information moves out of the object, and the root map loses its own tag - so a Batch 6
     * reader sees a Batch 5 row's {@code @class} as an ordinary context key and quietly pollutes the context, while the
     * nested values COB stores fail outright. Keeping Batch 5's settings makes the two byte-identical.
     */
    static JsonMapper executionContextMapper() {
        return JsonMapper.builder()
                .activateDefaultTyping(executionContextTypeValidator(), DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY).build();
    }

    private static PolymorphicTypeValidator executionContextTypeValidator() {
        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();
        ALLOWED_EXECUTION_CONTEXT_SUBTYPES.forEach(builder::allowIfSubType);
        return builder.build();
    }

    @Bean
    public DataFieldMaxValueIncrementerFactory incrementerFactory(RoutingDataSource routingDataSource) {
        // The DefaultDataFieldMaxValueIncrementerFactory has to be overridden because Spring 6 introduced
        // a new MariaDB incrementer that's incompatible with Spring Batch 4.x
        return new FineractDataFieldMaxValueIncrementerFactory(routingDataSource);
    }

    @Bean
    public JobRepository jobRepository(RoutingDataSource routingDataSource,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
            JacksonExecutionContextStringSerializer executionContextSerializer, DataFieldMaxValueIncrementerFactory incrementerFactory)
            throws Exception {
        JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
        factory.setDataSource(routingDataSource);
        factory.setTransactionManager(transactionManager);
        // Metadata checkpoints must join the JPA chunk transaction, hence the JPA transaction manager above: a JDBC
        // transaction manager treats the connection exposed by JpaTransactionManager as a new transaction and commits
        // business writes early.
        //
        // ISOLATION_DEFAULT then follows from that choice and is NOT a tidy-up of the previous explicit
        // ISOLATION_READ_COMMITTED. ExtendedJpaTransactionManager#doBegin rejects every non-default isolation level
        // (the lock-free EclipseLink dialect is only safe while all transactions share the pool baseline), so any
        // explicit level here would make createJobExecution fail outright rather than merely behave differently.
        //
        // The practical effect is that create* and getLastJobExecution* inherit the pool baseline, which
        // application.properties sets to TRANSACTION_REPEATABLE_READ - a stronger level than the READ_COMMITTED this
        // used to request. That is safe here because the create path has no read-modify-write for REPEATABLE READ to
        // abort: it only INSERTs rows it creates itself (job instance, job execution, its parameters and its execution
        // context), getLastJobExecution* is read-only, and id generation runs outside the transaction on both
        // supported databases (PostgresSequenceMaxValueIncrementer uses a non-transactional nextval;
        // MySQLMaxValueIncrementer is configured with useNewConnection - see
        // FineractDataFieldMaxValueIncrementerFactory). The MySQL gap locking that distinguishes the two levels
        // applies to locking reads and range UPDATE/DELETE, none of which occur here.
        //
        // Concurrent creation of the SAME job instance is the one visible difference: on PostgreSQL it can surface as
        // a serialization failure instead of a duplicate-key violation. It needs identical job parameters, which the
        // run.id increment in NextJobParametersResolver normally prevents, and duplicate scheduled launches are
        // guarded by the pessimistic job lock in SchedularWritePlatformService#processJobDetailForExecution - not by
        // the repository's isolation level, which is why Spring Batch's SERIALIZABLE default was dropped in the first
        // place.
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.setSerializer(executionContextSerializer);
        factory.setIncrementerFactory(incrementerFactory);
        factory.afterPropertiesSet();
        // Decorated rather than substituted inside the factory bean: see FineractJobRepository.
        return FineractJobRepository.withCheckpointRollback(factory.getObject(), transactionManager);
    }
}
