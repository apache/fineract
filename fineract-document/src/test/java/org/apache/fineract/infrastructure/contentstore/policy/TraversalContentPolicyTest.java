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

import java.util.List;
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
class TraversalContentPolicyTest {

    @Autowired
    private TraversalContentPolicy traversalContentPolicy;

    @Test
    void pathTraversal() {
        var paths = List.of("/tmp/../17/michael.vorburger-crepes.jpg", "../17/michael.vorburger-crepes.jpg",
                "/tmp/..//17//michael.vorburger-crepes.jpg", "/tmp/..//17/michael.vorburger-crepes.jpg",
                "..//17/michael.vorburger-crepes.jpg", "../../../../../../../../../../tmp/michael.vorburger-crepes.jpg",
                "/tmp/../../../../../../../../../../tmp/image-text-wrong-content.jsp",
                "../../../../../../../../../../tmp/image-text-wrong-content.jsp", "/test/../tmp/image-text-wrong-content.jsp");

        paths.forEach(path -> {
            log.info("Checking path: {}", path);

            var exception = assertThrows(ContentPolicyException.class, () -> {
                var ctx = ContentPolicyContext.builder().path(path).mimeType("image/jpeg").build();

                traversalContentPolicy.check(ctx);
            });

            assertThat(exception).isNotNull();

            assertThat(exception.getMessage()).startsWith("Trying to overwrite a sibling file:");
        });
    }
}
