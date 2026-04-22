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
package org.apache.fineract.infrastructure.contentstore.processor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@FunctionalInterface
public interface ContentProcessor {

    String BEAN_NAME_EXECUTOR = "contentProcessorExecutor";

    ContentProcessorContext process(ContentProcessorContext context);

    default void apply() {
        // do nothing by default
    }

    default ContentProcessorContext process(InputStream is) {
        return process(new ContentProcessorContext(is));
    }

    default ContentProcessorContext process(InputStream is, Map<String, Object> parameters) {
        return process(new ContentProcessorContext(is, parameters));
    }

    default ContentProcessor then(ContentProcessor next) {
        return (context) -> next.process(this.process(context));
    }

    @FunctionalInterface
    interface InputOutputStreamConsumer {

        void accept(InputStream input, OutputStream output) throws Exception;
    }

    @FunctionalInterface
    interface OutputStreamConsumer {

        void accept(OutputStream output) throws Exception;
    }
}
