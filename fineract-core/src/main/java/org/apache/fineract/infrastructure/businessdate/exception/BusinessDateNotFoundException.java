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
package org.apache.fineract.infrastructure.businessdate.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when business date is not found.
 */
public class BusinessDateNotFoundException extends AbstractPlatformResourceNotFoundException {

    public BusinessDateNotFoundException(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

    public static BusinessDateNotFoundException notExist(final String type, Throwable... e) {
        return new BusinessDateNotFoundException("error.msg.businessdate.type.not.exist",
                "Business date with type `" + type + "` does not exist.", type, e);
    }

    public static BusinessDateNotFoundException notFound(final String type, Throwable... e) {
        return new BusinessDateNotFoundException("error.msg.businessdate.not.found", "Business date with type `" + type + "` is not found.",
                type, e);
    }
}
