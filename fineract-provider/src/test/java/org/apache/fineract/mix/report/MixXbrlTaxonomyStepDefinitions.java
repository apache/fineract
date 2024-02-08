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
package org.apache.fineract.mix.report;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;

import io.cucumber.java8.En;
import java.util.List;
import org.apache.fineract.mix.service.XBRLResultServiceImpl;
import org.assertj.core.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;

public class MixXbrlTaxonomyStepDefinitions implements En {

    private XBRLResultServiceImpl readService;

    private String template;

    private List<String> result;

    public MixXbrlTaxonomyStepDefinitions() {
        Given("/^A XBRL template (.*)$/", (String template) -> {
            this.readService = new XBRLResultServiceImpl(mock(JdbcTemplate.class), null, null);
            this.template = template;
        });

        When("The user resolves GL codes", () -> {
            this.result = this.readService.getGLCodes(template);
        });

        Then("/^The result should contain (.*)$/", (String line) -> {
            assertArrayEquals(result.toArray(new String[0]), Arrays.array(line.split(",")));
        });
    }
}
