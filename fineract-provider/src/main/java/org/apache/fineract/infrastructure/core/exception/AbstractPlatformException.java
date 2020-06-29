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

import java.util.ArrayList;
import java.util.List;

/**
 * Exception with internationalization support (it's message can be translated).
 */
public abstract class AbstractPlatformException extends RuntimeException {

    private static final Object[] NO_ARGS = new Object[0];

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final Object[] defaultUserMessageArgs;

    protected AbstractPlatformException(String globalisationMessageCode, String defaultUserMessage) {
        super(defaultUserMessage);
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.defaultUserMessageArgs = NO_ARGS;
    }

    protected AbstractPlatformException(String globalisationMessageCode, String defaultUserMessage, Throwable cause) {
        super(defaultUserMessage, cause);
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.defaultUserMessageArgs = NO_ARGS;
    }

    protected AbstractPlatformException(String globalisationMessageCode, String defaultUserMessage, Object[] defaultUserMessageArgs) {
        super(defaultUserMessage, findThrowableCause(defaultUserMessageArgs));
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.defaultUserMessageArgs = AbstractPlatformException.filterThrowableCause(defaultUserMessageArgs);
    }

    private static Throwable findThrowableCause(Object[] defaultUserMessageArgs) {
        for (Object defaultUserMessageArg : defaultUserMessageArgs) {
            if (defaultUserMessageArg instanceof Throwable) {
                return (Throwable) defaultUserMessageArg;
            }
        }
        return null;
    }

    private static Object[] filterThrowableCause(Object[] defaultUserMessageArgs) {
        List<Object> filteredDefaultUserMessageArgs = new ArrayList<>(defaultUserMessageArgs.length);
        for (Object defaultUserMessageArg : defaultUserMessageArgs) {
            if (!(defaultUserMessageArg instanceof Throwable)) {
                filteredDefaultUserMessageArgs.add(defaultUserMessageArg);
            }
        }
        return filteredDefaultUserMessageArgs.toArray();
    }

    public final String getGlobalisationMessageCode() {
        return this.globalisationMessageCode;
    }

    public final String getDefaultUserMessage() {
        return this.defaultUserMessage;
    }

    public final Object[] getDefaultUserMessageArgs() {
        return this.defaultUserMessageArgs;
    }
}
