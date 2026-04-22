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
package org.apache.fineract.infrastructure.contentstore.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.TestConfiguration;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class MimeContentPolicyTest {

    @Autowired
    private MimeContentPolicy mimeContentPolicy;

    @Test
    void mimeTypeMismatch() {
        var path = "tmp/test-gif.png";
        var mime = "image/png";

        var ctx = ContentPolicyContext.builder().path(path).mimeType(mime)
                .inputStream(MimeContentPolicyTest.class.getClassLoader().getResourceAsStream("test-gif.png")).build();

        var exception = assertThrows(ContentPolicyException.class, () -> {
            mimeContentPolicy.check(ctx);
        });

        assertThat(exception).isNotNull();

        log.info("Error message: {}", exception.getMessage());
    }
}
