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
package org.apache.fineract.infrastructure.core.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MultiExceptionTest {

    private static Logger LOG = LoggerFactory.getLogger(MultiExceptionTest.class);

    @Test()
    void testEmpty() throws MultiException {
        assertThrows(IllegalArgumentException.class, () -> {
            throw new MultiException(Collections.emptyList());
        });
    }

    @Test()
    void test() throws MultiException {
        List<Throwable> causes = List.of(new IllegalArgumentException(), new IllegalStateException());
        MultiException e = new MultiException(causes);
        LOG.warn("Biep, bieb", e);
        // Uncomment to see JUnit UI:
        // throw e;
    }
}
