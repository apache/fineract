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
package org.apache.fineract.integrationtests.client;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.assertj.core.api.AbstractBigDecimalAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Base Integration Test class for Feign-based client
 *
 * @author Apache Fineract
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class FeignIntegrationTest {

    protected FineractFeignClient fineractClient() {
        return FineractFeignClientHelper.getFineractFeignClient();
    }

    protected <T> T ok(Supplier<T> call) {
        return FeignCalls.ok(call);
    }

    protected void executeVoid(Runnable call) {
        FeignCalls.executeVoid(call);
    }

    public static IterableAssert<?> assertThat(Iterable<?> actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractBigDecimalAssert<?> assertThat(BigDecimal actual) {
        return Assertions.assertThat(actual);
    }

    public static <T> ObjectAssert<T> assertThat(T actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractLongAssert<?> assertThat(Long actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractDoubleAssert<?> assertThat(Double actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractFloatAssert<?> assertThat(Float actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractIntegerAssert<?> assertThat(Integer actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractBooleanAssert<?> assertThat(Boolean actual) {
        return Assertions.assertThat(actual);
    }

    public static AbstractStringAssert<?> assertThat(String actual) {
        return Assertions.assertThat(actual);
    }

    public static <T> OptionalAssert<T> assertThat(Optional<T> actual) {
        return Assertions.assertThat(actual);
    }
}
