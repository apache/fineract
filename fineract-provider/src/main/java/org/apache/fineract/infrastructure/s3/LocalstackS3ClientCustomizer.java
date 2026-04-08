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
package org.apache.fineract.infrastructure.s3;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.poi.util.StringUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Component
@RequiredArgsConstructor
@Profile(FineractProfiles.TEST)
public class LocalstackS3ClientCustomizer implements S3ClientCustomizer {

    private final Environment environment;

    @Override
    public void customize(S3ClientBuilder builder) {
        String env = environment.getProperty("AWS_ENDPOINT_URL", "");
        if (StringUtil.isNotBlank(env)) {
            builder.endpointOverride(URI.create(env)).forcePathStyle(true);
        }
    }
}
