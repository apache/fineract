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
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.FineractClient;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
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
import retrofit2.Call;
import retrofit2.Response;

/**
 * Base Integration Test class
 *
 * @author Michael Vorburger.ch
 */
// Allow keeping state between tests
@TestInstance(Lifecycle.PER_CLASS)
// TODO Remove @TestMethodOrder when https://github.com/junit-team/junit5/issues/1919 is available
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class IntegrationTest {

    protected FineractClient fineractClient() {
        return FineractClientHelper.getFineractClient();
    }

    protected FineractClient newFineractClient(String username, String password) {
        return FineractClientHelper.createNewFineractClient(username, password, this::customizeFineractClient);
    }

    /**
     * Callback to customize FineractClient
     *
     * @param builder
     *            FineractClient.Builder.
     */
    protected void customizeFineractClient(FineractClient.Builder builder) {

    }

    // This method just makes it easier to use Calls.ok() in tests (it avoids having to static import)
    protected <T> T ok(Call<T> call) {
        return Calls.ok(call);
    }

    protected <T> Response<T> okR(Call<T> call) {
        return Calls.okR(call);
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
