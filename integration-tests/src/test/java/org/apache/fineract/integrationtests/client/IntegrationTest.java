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

import com.google.common.truth.BigDecimalSubject;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.ComparableSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.FineractClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Integration Test for /documents API.
 *
 * @author Michael Vorburger.ch
 */
// Allow keeping state between tests
@TestInstance(Lifecycle.PER_CLASS)
// TODO Remove @TestMethodOrder when https://github.com/junit-team/junit5/issues/1919 is available
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class IntegrationTest {

    private static final SecureRandom random = new SecureRandom();

    private FineractClient fineract;

    protected FineractClient fineract() {
        if (fineract == null) {
            String url = System.getProperty("fineract.it.url", "https://localhost:8443/fineract-provider/api/v1/");
            // insecure(true) should *ONLY* ever be used for https://localhost:8443, NOT in real clients!!
            fineract = FineractClient.builder().insecure(true).baseURL(url).tenant("default").basicAuth("mifos", "password")
                    .logging(Level.NONE).build();
        }
        return fineract;
    }

    /**
     * See {@link FineractClient#DATE_FORMAT}.
     */
    protected String dateFormat() {
        return FineractClient.DATE_FORMAT;
    }

    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    protected String random() {
        return Long.toString(random.nextLong());
    }

    // This method just makes it easier to use Calls.ok() in tests (it avoids having to static import)
    protected <T> T ok(Call<T> call) {
        return Calls.ok(call);
    }

    protected <T> Response<T> okR(Call<T> call) {
        return Calls.okR(call);
    }

    // as above, avoids import static CallSubject.assertThat
    protected <T> CallSubject assertThat(Call<T> call) {
        return CallSubject.assertThat(call);
    }

    // as above, this avoids issues with e.g. the Eclipse compiler getting confused which assertThat is which
    public static IterableSubject assertThat(Iterable<?> actual) {
        return Truth.assertThat(actual);
    }

    public static <T extends Comparable<?>> ComparableSubject<T> assertThat(T actual) {
        return Truth.assertThat(actual);
    }

    public static BigDecimalSubject assertThat(BigDecimal actual) {
        return Truth.assertThat(actual);
    }

    public static Subject assertThat(Object actual) {
        return Truth.assertThat(actual);
    }

    public static LongSubject assertThat(Long actual) {
        return Truth.assertThat(actual);
    }

    public static DoubleSubject assertThat(Double actual) {
        return Truth.assertThat(actual);
    }

    public static FloatSubject assertThat(Float actual) {
        return Truth.assertThat(actual);
    }

    public static IntegerSubject assertThat(Integer actual) {
        return Truth.assertThat(actual);
    }

    public static BooleanSubject assertThat(Boolean actual) {
        return Truth.assertThat(actual);
    }

    public static StringSubject assertThat(String actual) {
        return Truth.assertThat(actual);
    }

    // from truth-java8-extension
    public static OptionalSubject assertThat(Optional<?> actual) {
        return Truth8.assertThat(actual);
    }
}
