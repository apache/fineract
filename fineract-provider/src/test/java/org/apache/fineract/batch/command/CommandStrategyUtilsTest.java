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
package org.apache.fineract.batch.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.batch.domain.BatchRequest;
import org.junit.jupiter.api.Test;

class CommandStrategyUtilsTest {

    @Test
    public void testRelativeUrlWithoutVersionRemovesV1() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("v1/clients/123");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123");
    }

    @Test
    public void testRelativeUrlWithoutVersionRemovesV2() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("v2/clients/123");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123");
    }

    @Test
    public void testRelativeUrlWithoutVersionRemovesV12() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("v12/clients/123");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123");
    }

    @Test
    public void testRelativeUrlWithoutVersionRemovesV1WhenQueryParamsPresent() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("v1/clients/123?command=action&something=else");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123?command=action&something=else");
    }

    @Test
    public void testRelativeUrlWithoutVersionRemovesNothing() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("clients/123");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123");
    }

    @Test
    public void testRelativeUrlWithoutVersionRemovesNothingWhenQueryParamsPresent() {
        // given
        BatchRequest request = new BatchRequest();
        request.setRelativeUrl("clients/123?command=action&something=else");
        // when
        String result = CommandStrategyUtils.relativeUrlWithoutVersion(request);
        // then
        assertThat(result).isEqualTo("clients/123?command=action&something=else");
    }
}
