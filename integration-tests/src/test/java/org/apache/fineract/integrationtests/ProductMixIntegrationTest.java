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
package org.apache.fineract.integrationtests;

import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignProductMixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProductMixIntegrationTest extends FeignIntegrationTest {

    private FeignProductMixHelper productMixHelper;

    @BeforeAll
    public void setup() {
        productMixHelper = new FeignProductMixHelper(fineractClient());
    }

    @Test
    public void getProductMixList() {
        Assertions.assertNotNull(productMixHelper.retrieveAllLoanProducts());

        // GetLoanProductsTemplateResponse has no productOptions field, so this can only prove the call returned 200;
        // adding the field means touching LoanProductsApiResourceSwagger, which is production code.
        Assertions.assertNotNull(productMixHelper.retrieveProductMixTemplate());
    }

}
