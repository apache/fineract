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
package org.apache.fineract.integrationtests.client.feign.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;

/**
 * Detaching a loan product's delinquency bucket needs a body that carries an explicit JSON {@code null}:
 * {@code LoanProductWritePlatformServiceJpaRepositoryImpl} gates the update on
 * {@code command.parameterExists("delinquencyBucketId")}, so the key has to be <em>present</em> with a null value.
 *
 * <p>
 * The generated {@code PutLoanProductsProductIdRequest} cannot express that: the mapper's global {@code NON_NULL} drops
 * a null property, so {@code .delinquencyBucketId(null)} serialises to <code>{}</code> and the bucket is never cleared.
 * Declaring the property {@code nullable} in the spec would model it as a {@code JsonNullable<Long>} and fix it for SDK
 * consumers, but it cannot fix it here: {@code fineract-client} (Retrofit/Gson) and {@code fineract-client-feign}
 * (Jackson) both generate into {@code org.apache.fineract.client.models}, and {@code integration-tests} puts the
 * Retrofit artifact on the test classpath first ({@code integration-tests/dependencies.gradle}), so the Retrofit model
 * - a plain {@code Long} - wins the name clash and the Jackson annotations never reach the wire. Measured, not assumed.
 *
 * <p>
 * So the spec is left alone and this keeps the call typed end to end, binding the response to the generated model.
 * Resolving the classpath shadowing is a separate concern: 46 test files import Retrofit-only packages and 430 import
 * the shared model package.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface LoanProductCommandsApi {

    @RequestLine("PUT /v1/loanproducts/{productId}")
    PutLoanProductsProductIdResponse clearDelinquencyBucket(@Param("productId") Long productId, ClearDelinquencyBucketRequest request);

    /**
     * Body for {@link #clearDelinquencyBucket}: serialises to {@code {"delinquencyBucketId":null}}. The property-level
     * {@code @JsonInclude(ALWAYS)} is what overrides the mapper's global {@code NON_NULL}.
     */
    class ClearDelinquencyBucketRequest {

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private final Long delinquencyBucketId = null;

        public Long getDelinquencyBucketId() {
            return delinquencyBucketId;
        }
    }
}
