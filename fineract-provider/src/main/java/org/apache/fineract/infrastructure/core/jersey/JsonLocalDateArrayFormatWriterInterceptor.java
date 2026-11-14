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

package org.apache.fineract.infrastructure.core.jersey;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.Arrays;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonLocalDateBeanSerializerModifier;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.springframework.stereotype.Component;

@Provider
@Component
public class JsonLocalDateArrayFormatWriterInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        if (isLocalDateArrayFormat(context)) {
            JacksonLocalDateBeanSerializerModifier.writeWithLocalDateArrayFormat(context::proceed);
        } else {
            context.proceed();
        }
    }

    private boolean isLocalDateArrayFormat(WriterInterceptorContext context) {
        return context.getAnnotations() != null
                && Arrays.stream(context.getAnnotations()).anyMatch(JsonLocalDateArrayFormat.class::isInstance);
    }
}
