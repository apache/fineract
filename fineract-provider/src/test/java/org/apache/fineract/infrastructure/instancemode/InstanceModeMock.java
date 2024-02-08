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
package org.apache.fineract.infrastructure.instancemode;

import org.apache.fineract.infrastructure.core.config.FineractProperties;

public final class InstanceModeMock {

    private InstanceModeMock() {

    }

    public static FineractProperties.FineractModeProperties createModeProps(boolean readEnabled, boolean writeEnabled,
            boolean batchWorkerEnabled, boolean batchManagerEnabled) {
        FineractProperties.FineractModeProperties modeProperties = new FineractProperties.FineractModeProperties();
        modeProperties.setReadEnabled(readEnabled);
        modeProperties.setWriteEnabled(writeEnabled);
        modeProperties.setBatchWorkerEnabled(batchWorkerEnabled);
        modeProperties.setBatchManagerEnabled(batchManagerEnabled);
        return modeProperties;
    }
}
