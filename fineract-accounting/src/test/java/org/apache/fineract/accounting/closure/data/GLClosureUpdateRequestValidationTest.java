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
package org.apache.fineract.accounting.closure.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GLClosureUpdateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidCommentsPasses() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments("Valid comments").build();
        final Set<ConstraintViolation<GLClosureUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullCommentsFailsNotBlank() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments(null).build();
        final Set<ConstraintViolation<GLClosureUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("{org.apache.fineract.accounting.closure.comments.not-blank}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void testEmptyCommentsFailsNotBlank() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments("").build();
        final Set<ConstraintViolation<GLClosureUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("{org.apache.fineract.accounting.closure.comments.not-blank}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void testWhitespaceCommentsFailsNotBlank() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments("   ").build();
        final Set<ConstraintViolation<GLClosureUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("{org.apache.fineract.accounting.closure.comments.not-blank}", violations.iterator().next().getMessageTemplate());
    }

    @Test
    void testCommentsExceedingMaxSize() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments("a".repeat(501)).build();
        final Set<ConstraintViolation<GLClosureUpdateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("{org.apache.fineract.accounting.closure.comments.size}", violations.iterator().next().getMessageTemplate());
    }
}
