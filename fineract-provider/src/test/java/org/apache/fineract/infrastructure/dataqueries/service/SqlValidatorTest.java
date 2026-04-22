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
package org.apache.fineract.infrastructure.dataqueries.service;

import org.apache.fineract.TestConfiguration;
import org.apache.fineract.infrastructure.security.exception.SqlValidationException;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class SqlValidatorTest {

    @Autowired
    private SqlValidator sqlValidator;

    @Test
    public void testSingleDashInParameterName() {
        final String paramToValidate = "Loan Report - Active";
        Assertions.assertDoesNotThrow(() -> sqlValidator.validate(paramToValidate));
    }

    @Test
    public void testCommentInjectionAttempt() {
        final String paramToValidate = "Loan Report -- Active";
        Assertions.assertThrows(SqlValidationException.class, () -> sqlValidator.validate(paramToValidate));
    }
}
