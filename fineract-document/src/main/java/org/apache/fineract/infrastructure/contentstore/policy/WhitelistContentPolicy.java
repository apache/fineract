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

import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WhitelistContentPolicy implements ContentPolicy {

    private final FineractProperties properties;

    private List<Pattern> regexWhitelist;

    @EventListener(ApplicationStartedEvent.class)
    void onStartup() {
        regexWhitelist = properties.getContent().getRegexWhitelist().stream().map(Pattern::compile).toList();
    }

    @Override
    public void check(ContentPolicyContext ctx) {
        if (properties.getContent().isRegexWhitelistEnabled()) {
            final var fileName = FilenameUtils.getName(ctx.getPath());

            boolean matches = regexWhitelist.stream().anyMatch(p -> p.matcher(fileName).matches());

            if (!matches) {
                throw new ContentPolicyException(String.format("File name not allowed: %s", fileName));
            }
        }

        if (properties.getContent().isMimeWhitelistEnabled()) {
            final var fileName = FilenameUtils.getName(ctx.getPath());

            if (StringUtils.isEmpty(ctx.getMimeType())) {
                throw new ContentPolicyException(String.format("Could not detect mime type for filename %s!", fileName));
            }

            if (!properties.getContent().getMimeWhitelist().contains(ctx.getMimeType())) {
                throw new ContentPolicyException(
                        String.format("Detected mime type %s for filename %s not allowed!", ctx.getMimeType(), fileName));
            }
        }
    }
}
