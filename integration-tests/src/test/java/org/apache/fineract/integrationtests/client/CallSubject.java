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

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import javax.annotation.Nullable;
import org.apache.fineract.client.util.Calls;
import retrofit2.Call;

/**
 * Google Truth Extension for Retrofit Call.
 *
 * @author Michael Vorburger.ch
 */
public class CallSubject extends Subject {

    // as per https://truth.dev/extension

    public static CallSubject assertThat(@Nullable Call<?> actual) {
        return Truth.assertAbout(calls()).that(actual);
    }

    public static Factory<CallSubject, Call<?>> calls() {
        return CallSubject::new;
    }

    private final Call<?> actual;

    protected CallSubject(FailureMetadata metadata, @Nullable Call<?> actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public void hasHttpStatus(int expectedHttpStatus) {
        check("httpStatus").that(Calls.executeU(actual).code()).isEqualTo(expectedHttpStatus);
    }
}
