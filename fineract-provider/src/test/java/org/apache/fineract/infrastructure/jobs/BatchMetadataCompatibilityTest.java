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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.infrastructure.jobs.config.FineractDataFieldMaxValueIncrementerFactory;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.jdbc.support.incrementer.AbstractDataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * Pins the two things that let a Spring Batch 5 and a Spring Batch 6 instance share one database, so that upgrading
 * needs no metadata migration and no coordinated restart.
 * <p>
 * If either breaks, old instances stop being able to create jobs, or silently lose every in-flight execution context.
 */
public class BatchMetadataCompatibilityTest {

    @ParameterizedTest(name = "{0} keeps the Spring Batch 5 job instance sequence name")
    @ValueSource(strings = { "POSTGRES", "MYSQL", "MARIADB" })
    public void keepsTheSpringBatch5JobInstanceSequenceName(String databaseType) {
        assertThat(incrementerName(databaseType, "BATCH_JOB_INSTANCE_SEQ"))
                .as("renaming the sequence would break every already-running instance the moment the migration landed")
                .isEqualTo("BATCH_JOB_SEQ");
    }

    @ParameterizedTest(name = "{0} leaves the other sequence names alone")
    @ValueSource(strings = { "POSTGRES", "MYSQL", "MARIADB" })
    public void leavesTheOtherSequenceNamesAlone(String databaseType) {
        assertThat(incrementerName(databaseType, "BATCH_JOB_EXECUTION_SEQ")).isEqualTo("BATCH_JOB_EXECUTION_SEQ");
        assertThat(incrementerName(databaseType, "BATCH_STEP_EXECUTION_SEQ")).isEqualTo("BATCH_STEP_EXECUTION_SEQ");
    }

    @Test
    public void writesExecutionContextsInTheSpringBatch5WireFormat() throws IOException {
        String json = serialize(cobContext());

        assertThat(json).as("Batch 5 tags the root map; the Batch 6 default does not, and its @class then reads back as a context key")
                .contains("\"@class\":\"java.util.LinkedHashMap\"");
        assertThat(json).as("type information must be an @class property, not a wrapper array")
                .contains("\"@class\":\"org.apache.fineract.cob.data.COBParameter\"");
        assertThat(json).doesNotContain("[\"org.apache.fineract.cob.data.COBParameter\"");
    }

    @Test
    public void springBatch5CanReadWhatThisBuildWrites() throws IOException {
        assertRestoresTheCobContext(readWithSpringBatch5Mapper(serialize(cobContext())));
    }

    @Test
    public void thisBuildCanReadWhatSpringBatch5Wrote() throws IOException {
        assertRestoresTheCobContext(deserialize(writeWithSpringBatch5Mapper(cobContext())));
    }

    /**
     * Asserted field by field rather than with {@code equals}: {@code BusinessStepNameAndOrder} carries no
     * {@code @EqualsAndHashCode}, so a faithfully restored instance is still unequal to the original.
     */
    private static void assertRestoresTheCobContext(Map<String, Object> restored) {
        assertThat(restored).containsEntry("partition", "partition_0").containsEntry("BusinessDate", "2026-09-15")
                .containsEntry("IS_CATCH_UP", "false").containsEntry("partitionCount", 3)
                .containsEntry("loanCobParameter", new COBParameter(1L, 100L));
        assertThat(restored.get("businessSteps")).asInstanceOf(InstanceOfAssertFactories.collection(BusinessStepNameAndOrder.class))
                .singleElement().satisfies(step -> {
                    assertThat(step.getStepName()).isEqualTo("APPLY_CHARGE_TO_OVERDUE_LOANS");
                    assertThat(step.getStepOrder()).isEqualTo(1L);
                });
    }

    private static Map<String, Object> cobContext() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("partition", "partition_0");
        context.put("BusinessDate", "2026-09-15");
        context.put("IS_CATCH_UP", "false");
        context.put("partitionCount", 3);
        context.put("loanCobParameter", new COBParameter(1L, 100L));
        context.put("businessSteps", Set.of(new BusinessStepNameAndOrder("APPLY_CHARGE_TO_OVERDUE_LOANS", 1L)));
        return context;
    }

    private static String serialize(Map<String, Object> context) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JacksonExecutionContextStringSerializer(ScheduledJobRunnerConfig.executionContextMapper()).serialize(context, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> deserialize(String json) throws IOException {
        return new JacksonExecutionContextStringSerializer(ScheduledJobRunnerConfig.executionContextMapper())
                .deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    /** Mirrors Jackson2ExecutionContextStringSerializer: NON_FINAL typing written as an @class property. */
    private static ObjectMapper springBatch5Mapper() {
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfSubType("").build(),
                        ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readWithSpringBatch5Mapper(String json) {
        try {
            return springBatch5Mapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Spring Batch 5 could not read a context written by this build", e);
        }
    }

    private static String writeWithSpringBatch5Mapper(Map<String, Object> context) {
        try {
            return springBatch5Mapper().writeValueAsString(context);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String incrementerName(String databaseType, String requestedName) {
        DataFieldMaxValueIncrementer incrementer = new FineractDataFieldMaxValueIncrementerFactory(mock(DataSource.class))
                .getIncrementer(databaseType, requestedName);
        return ((AbstractDataFieldMaxValueIncrementer) incrementer).getIncrementerName();
    }
}
