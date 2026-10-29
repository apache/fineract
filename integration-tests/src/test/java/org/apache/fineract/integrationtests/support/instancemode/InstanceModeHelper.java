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
package org.apache.fineract.integrationtests.support.instancemode;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.ChangeInstanceModeRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

@SuppressWarnings({ "HideUtilityClassConstructor" })
public class InstanceModeHelper {

    public static void changeMode(boolean readEnabled, boolean writeEnabled, boolean batchWorkerEnabled, boolean batchManagerEnabled) {
        ChangeInstanceModeRequest request = new ChangeInstanceModeRequest().readEnabled(readEnabled).writeEnabled(writeEnabled)
                .batchWorkerEnabled(batchWorkerEnabled).batchManagerEnabled(batchManagerEnabled);
        ApiResponse<Void> response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().instanceMode().updateInstanceModeWithHttpInfo(request, Map.of()));
        assertEquals(200, response.getStatusCode());
    }
}
