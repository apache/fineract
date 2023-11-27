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

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.stereotype.Component;

@Provider
@Produces(MediaType.APPLICATION_JSON_VALUE)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Component
@RequiredArgsConstructor
public class JerseyJacksonObjectArgumentHandler<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    private final MappingJackson2HttpMessageConverter converter;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType) {
        return true;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        if (String.class == genericType) {
            // If the request type is String, keep it that way.
            StringWriter writer = new StringWriter();
            IOUtils.copy(entityStream, writer, UTF_8);
            String json = writer.toString();
            return type.cast(json);
        } else {
            // Create the proper type from the JSON
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(httpHeaders);
            return (T) converter.read(genericType, type, new MappingJacksonInputMessage(entityStream, headers));
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (String.class == genericType) {
            // If the response type is String, keep it that way.
            IOUtils.write((String) t, entityStream, UTF_8);
        } else {
            // Create the proper JSON string from the object
            HttpHeaders headers = new HttpHeaders();
            httpHeaders.forEach((header, rawValues) -> {
                List<String> values = rawValues.stream().map(Object::toString).toList();
                headers.put(header, values);
            });
            converter.write(t, genericType, MediaType.APPLICATION_JSON, new SimpleHttpOutputMessage(entityStream, headers));
        }
    }

    @RequiredArgsConstructor
    private static final class SimpleHttpOutputMessage implements HttpOutputMessage {

        private final OutputStream outputStream;
        private final HttpHeaders headers;

        @Override
        public OutputStream getBody() throws IOException {
            return outputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
