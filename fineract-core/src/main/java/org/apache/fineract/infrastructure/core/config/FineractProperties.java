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

import io.github.resilience4j.common.retry.configuration.CommonRetryConfigurationProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fineract")
public class FineractProperties {

    private String nodeId;

    private String idempotencyKeyHeaderName;

    private Boolean insecureHttpClient;

    private FineractTenantProperties tenant;

    private FineractModeProperties mode;

    private FineractCorrelationProperties correlation;

    private FineractPartitionedJob partitionedJob;

    private FineractRemoteJobMessageHandlerProperties remoteJobMessageHandler;

    private FineractEventsProperties events;

    private FineractTaskExecutor taskExecutor;

    private FineractContentProperties content;

    private FineractReportProperties report;

    private FineractJobProperties job;

    private FineractTemplateProperties template;
    private FineractJpaProperties jpa;

    private FineractDatabaseProperties database;
    private FineractQueryProperties query;
    private FineractApiProperties api;
    private FineractSecurityProperties security;

    private FineractNotificationProperties notification;

    private FineractLoanProperties loan;

    private FineractSamplingProperties sampling;

    private FineractModulesProperties module;

    private FineractResilience4jProperties resilience4j;

    @Getter
    @Setter
    public static class FineractResilience4jProperties {

        private CommonRetryConfigurationProperties.InstanceProperties executeCommand;
    }

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
        private String masterPassword;
        private String encryption;

        private String readOnlyHost;
        private Integer readOnlyPort;
        private String readOnlyUsername;
        private String readOnlyPassword;
        private String readOnlyParameters;
        private String readOnlyName;

        private FineractConfigProperties config;
    }

    /**
     * Configuration properties to override configurations stored in the tenants database
     */
    @Getter
    @Setter
    public static class FineractConfigProperties {

        private int minPoolSize;
        private int maxPoolSize;

        public boolean isMinPoolSizeSet() {
            return minPoolSize != -1;
        }

        public boolean isMaxPoolSizeSet() {
            return maxPoolSize != -1;
        }
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
        private Integer threadPoolCorePoolSize;
        private Integer threadPoolMaxPoolSize;
        private Integer threadPoolQueueCapacity;
        private Integer retryLimit;
        private Integer pollInterval;

    }

    @Getter
    @Setter
    public static class FineractRemoteJobMessageHandlerProperties {

        private FineractRemoteJobMessageHandlerSpringEventsProperties springEvents;
        private FineractRemoteJobMessageHandlerJmsProperties jms;
        private FineractRemoteJobMessageHandlerKafkaProperties kafka;
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
        private String brokerUsername;
        private String brokerPassword;

        public boolean isBrokerPasswordProtected() {
            return StringUtils.isNotBlank(brokerUsername) || StringUtils.isNotBlank(brokerPassword);
        }
    }

    @Getter
    @Setter
    public static class FineractRemoteJobMessageHandlerKafkaProperties {

        private boolean enabled;
        private String bootstrapServers;
        private KafkaTopicProperties topic;
        private KafkaConsumerProperties consumer;
        private KafkaProperties producer;
        private KafkaProperties admin;
    }

    @Getter
    @Setter
    public static class KafkaTopicProperties {

        private boolean autoCreate;
        private String name;
        private int replicas;
        private int partitions;
    }

    @Getter
    @Setter
    public static class KafkaConsumerProperties extends KafkaProperties {

        private String groupId;
    }

    @Getter
    @Setter
    @Slf4j
    public static class KafkaProperties {

        private String extraPropertiesKeyValueSeparator;
        private String extraPropertiesSeparator;
        private String extraProperties;

        public Map<String, String> getExtraPropertiesMap() {
            Map<String, String> map = new HashMap<>();
            if (StringUtils.isNotEmpty(getExtraProperties()) && validateSeparators()) {
                String[] lines = StringUtils.split(getExtraProperties(), extraPropertiesSeparator);
                Arrays.stream(lines).forEach(line -> {
                    String[] keyAndValue = StringUtils.split(line, extraPropertiesKeyValueSeparator);
                    if (keyAndValue.length == 2) {
                        map.put(keyAndValue[0], keyAndValue[1]);
                    } else {
                        log.warn("Invalid property: {}", line);
                    }

                });
            }
            return map;
        }

        private boolean validateSeparators() {
            boolean valid = (StringUtils.isNotEmpty(extraPropertiesSeparator) && extraPropertiesSeparator.length() == 1
                    && StringUtils.isNotEmpty(extraPropertiesKeyValueSeparator) && extraPropertiesKeyValueSeparator.length() == 1
                    && !extraPropertiesSeparator.equals(extraPropertiesKeyValueSeparator));
            if (!valid) {
                log.warn("Invalid KafkaProperties configuration, lineSeparator '{}' and keyValueSeparator '{}'", extraPropertiesSeparator,
                        extraPropertiesKeyValueSeparator);
            }
            return valid;
        }
    }

    @Getter
    @Setter
    public static class FineractEventsProperties {

        private FineractExternalEventsProperties external;
    }

    @Getter
    @Setter
    public static class FineractTaskExecutor {

        private int defaultTaskExecutorCorePoolSize;
        private int defaultTaskExecutorMaxPoolSize;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProperties {

        private boolean enabled;
        private FineractExternalEventsProducerProperties producer;
        private int partitionSize;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProducerProperties {

        private FineractExternalEventsProducerJmsProperties jms;
        private FineractExternalEventsProducerKafkaProperties kafka;
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProducerJmsProperties {

        private boolean enabled;
        private String eventQueueName;
        private String eventTopicName;
        private String brokerUrl;
        private String brokerUsername;
        private String brokerPassword;
        private int producerCount;
        private boolean asyncSendEnabled;
        private int threadPoolTaskExecutorCorePoolSize;
        private int threadPoolTaskExecutorMaxPoolSize;

        public boolean isBrokerPasswordProtected() {
            return StringUtils.isNotBlank(brokerUsername) || StringUtils.isNotBlank(brokerPassword);
        }
    }

    @Getter
    @Setter
    public static class FineractExternalEventsProducerKafkaProperties {

        private boolean enabled;
        private String bootstrapServers;
        private KafkaTopicProperties topic;
        private KafkaProperties producer;
        private KafkaProperties admin;
        private int timeoutInSeconds;
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

    @Getter
    @Setter
    public static class FineractReportProperties {

        private FineractExportProperties export;
    }

    @Getter
    @Setter
    public static class FineractExportProperties {

        private FineractExportS3Properties s3;
    }

    @Getter
    @Setter
    public static class FineractExportS3Properties {

        private String bucketName;
        private Boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractJobProperties {

        private int stuckRetryThreshold;
        private boolean loanCobEnabled;
    }

    @Getter
    @Setter
    public static class FineractTemplateProperties {

        private boolean regexWhitelistEnabled;
        private List<String> regexWhitelist;
    }

    @Getter
    @Setter
    public static class FineractJpaProperties {

        private boolean statementLoggingEnabled;
    }

    @Getter
    @Setter
    public static class FineractDatabaseProperties {

        private String defaultMasterPassword;
    }

    @Getter
    @Setter
    public static class FineractQueryProperties {

        private int inClauseParameterSizeLimit;
    }

    @Getter
    @Setter
    public static class FineractApiProperties {

        private FineractBodyItemSizeLimitProperties bodyItemSizeLimit;
    }

    @Getter
    @Setter
    public static class FineractBodyItemSizeLimitProperties {

        private int inlineLoanCob;
    }

    @Getter
    @Setter
    public static class FineractNotificationProperties {

        private UserNotificationSystemProperties userNotificationSystem;
    }

    @Getter
    @Setter
    public static class UserNotificationSystemProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractLoanProperties {

        private FineractTransactionProcessorProperties transactionProcessor;
        private String statusChangeHistoryStatuses;
    }

    @Getter
    @Setter
    public static class FineractTransactionProcessorProperties {

        private FineractTransactionProcessorItemProperties creocore;
        private FineractTransactionProcessorItemProperties earlyRepayment;
        private FineractTransactionProcessorItemProperties mifosStandard;
        private FineractTransactionProcessorItemProperties heavensFamily;
        private FineractTransactionProcessorItemProperties interestPrincipalPenaltiesFees;
        private FineractTransactionProcessorItemProperties principalInterestPenaltiesFees;
        private FineractTransactionProcessorItemProperties rbiIndia;
        private FineractTransactionProcessorItemProperties duePenaltyFeeInterestPrincipalInAdvancePrincipalPenaltyFeeInterest;
        private FineractTransactionProcessorItemProperties duePenaltyInterestPrincipalFeeInAdvancePenaltyInterestPrincipalFee;
        private FineractTransactionProcessorItemProperties advancedPaymentStrategy;
        private boolean errorNotFoundFail;
    }

    @Getter
    @Setter
    public static class FineractSecurityProperties {

        private FineractSecurityBasicAuth basicauth;
        private FineractSecurityTwoFactorAuth twoFactor;
        private FineractSecurityOAuth oauth;

        public void set2fa(FineractSecurityTwoFactorAuth twoFactor) {
            this.twoFactor = twoFactor;
        }
    }

    @Getter
    @Setter
    public static class FineractSecurityBasicAuth {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractSecurityTwoFactorAuth {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractSecurityOAuth {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractTransactionProcessorItemProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class FineractSamplingProperties {

        private boolean enabled;
        private int samplingRate;
        private String sampledClasses;
        private int resetPeriodSec;
    }

    @Getter
    @Setter
    public static class FineractModulesProperties {

        private FineractInvestorModuleProperties investor;
    }

    @Getter
    @Setter
    public static class FineractInvestorModuleProperties extends AbstractFineractModuleProperties {

    }
}
