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
package org.apache.fineract.infrastructure.contentstore.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MimeContentPolicy implements ContentPolicy {

    private final ContentDetectorManager contentDetectorManager;

    @Override
    public void check(ContentPolicyContext ctx) {
        if (ctx.getInputStream() != null) {
            var result = contentDetectorManager
                    .detect(ContentDetectorContext.builder().inputStream(ctx.getInputStream()).inputStreamEnabled(true).build());

            if (!Strings.CI.equals(result.getMimeType(), ctx.getMimeType())) {
                throw new ContentPolicyException(String.format("Detected file type (%s), but mime type (%s) was provided. Mismatch!",
                        result.getMimeType(), ctx.getMimeType()));
            }
        }
    }
}
