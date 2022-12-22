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

package org.apache.fineract.infrastructure.core.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fineract")
public class FineractProperties {

    private String nodeId;

    private String idempotencyKeyHeaderName;

    private FineractTenantProperties tenant;

    private FineractModeProperties mode;

    private FineractCorrelationProperties correlation;

    private FineractPartitionedJob partitionedJob;

    private FineractRemoteJobMessageHandlerProperties remoteJobMessageHandler;

    private FineractEventsProperties events;

    private FineractContentProperties content;

    @Getter
    @Setter
    public static class FineractTenantProperties {

        private String host;
        private Integer port;
        private String username;
        private String password;
        private String parameters;
        private String timezone;
        private String identifier;
        private String name;
        private String description;
    }

    @Getter
    @Setter
    public static class FineractModeProperties {

        private boolean readEnabled;
        private boolean writeEnabled;
        private boolean batchWorkerEnabled;
        private boolean batchManagerEnabled;

        public boolean isReadOnlyMode() {
            return readEnabled && !writeEnabled && !batchWorkerEnabled && !batchManagerEnabled;
        }
    }

    @Getter
    @Setter
    public static class FineractCorrelationProperties {

        private boolean enabled;
        private String headerName;
    }

    @Getter
    @Setter
    public static class FineractPartitionedJob {

        // TODO should be used without wrapper class
        private List<PartitionedJobProperty> partitionedJobProperties;
    }

    @Getter
    @Setter
    public static class PartitionedJobProperty {

        private String jobName;
        private Integer chunkSize;
        private Integer partitionSize;
        private Integer threadCount;
    }

    @Getter
    @Setter
    public static class FineractRemoteJobMessageHandlerProperties {

        private FineractRemoteJobMessageHandlerSpringEventsProperties springEvents;
        private FineractRemoteJobMessageHandlerJmsProperties jms;
    }

    @Getter
    @Setter
    public static class FineractRemoteJobMessageHandlerSpringEventsProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractRemoteJobMessageHandlerJmsProperties {

        private boolean enabled;
        private String requestQueueName;
        private String brokerUrl;
    }

    @Getter
    @Setter
    public static class FineractEventsProperties {

        private FineractExternalEventsProperties external;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProperties {

        private boolean enabled;
        private FineractExternalEventsProducerProperties producer;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProducerProperties {

        private int readBatchSize;
        private FineractExternalEventsProducerJmsProperties jms;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProducerJmsProperties {

        private boolean enabled;
        private String eventQueueName;
        private String brokerUrl;
    }

    @Getter
    @Setter
    public static class FineractContentProperties {

        private boolean regexWhitelistEnabled;
        private List<String> regexWhitelist;
        private boolean mimeWhitelistEnabled;
        private List<String> mimeWhitelist;
        private FineractContentFilesystemProperties filesystem;
        private FineractContentS3Properties s3;
    }

    @Getter
    @Setter
    public static class FineractContentFilesystemProperties {

        private Boolean enabled;
        private String rootFolder;
    }

    @Getter
    @Setter
    public static class FineractContentS3Properties {

        private Boolean enabled;
        private String bucketName;
        private String accessKey;
        private String secretKey;
    }
}
