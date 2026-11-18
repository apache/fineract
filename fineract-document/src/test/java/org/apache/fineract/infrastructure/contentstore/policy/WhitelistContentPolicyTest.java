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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WhitelistContentPolicyTest {

    private WhitelistContentPolicy policy;

    @BeforeEach
    void setUp() {
        final FineractProperties.FineractContentProperties content = new FineractProperties.FineractContentProperties();
        content.setRegexWhitelistEnabled(true);
        content.setRegexWhitelist(List.of(".*\\.pdf$", ".*\\.xls", ".*\\.xlsx"));
        content.setMimeWhitelistEnabled(false);
        final FineractProperties properties = new FineractProperties();
        properties.setContent(content);
        policy = new WhitelistContentPolicy(properties);
        policy.onStartup();
    }

    @ParameterizedTest
    @ValueSource(strings = { "documents/IMPORT/1/offices.xls", "documents/IMPORT/1/OFFICES.XLS", "documents/IMPORT/1/Offices.Xlsx",
            "documents/clients/1/scan.PDF" })
    void acceptsWhitelistedExtensionsInAnyCase(final String path) {
        assertThatCode(() -> policy.check(ContentPolicyContext.builder().path(path).build())).doesNotThrowAnyException();
    }

    @Test
    void rejectsFileNameOutsideWhitelist() {
        assertThatThrownBy(() -> policy.check(ContentPolicyContext.builder().path("documents/IMPORT/1/offices.CSV").build()))
                .isInstanceOf(ContentPolicyException.class);
    }
}
