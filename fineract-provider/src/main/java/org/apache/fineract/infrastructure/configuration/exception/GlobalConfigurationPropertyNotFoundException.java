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
package org.apache.fineract.infrastructure.configuration.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when global configuration properties are
 * not found.
 */
public class GlobalConfigurationPropertyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GlobalConfigurationPropertyNotFoundException(final String propertyName) {
        super("error.msg.configuration.property.invalid", "Configuration property `" + propertyName + "` does not exist", propertyName);
    }

    public GlobalConfigurationPropertyNotFoundException(final Long configId) {
        super("error.msg.configuration.id.invalid", "Configuration identifier `" + configId + "` does not exist", configId);
    }
}