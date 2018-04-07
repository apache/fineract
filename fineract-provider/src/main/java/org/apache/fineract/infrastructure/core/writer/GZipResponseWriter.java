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
package org.apache.fineract.infrastructure.core.writer;

import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GZipResponseWriter implements ContainerResponseWriter {
    private final ContainerResponseWriter crw;

    private GZIPOutputStream gos;

    public GZipResponseWriter(ContainerResponseWriter crw) {
        this.crw = crw;
    }

    public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
        gos = new GZIPOutputStream(crw.writeStatusAndHeaders(-1, response));
        return gos;
    }

    public void finish() throws IOException {
        gos.finish();
        crw.finish();
    }
}
