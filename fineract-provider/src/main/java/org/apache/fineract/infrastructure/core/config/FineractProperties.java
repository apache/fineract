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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "fineract")
public class FineractProperties {

    private String nodeId;

    private FineractTenantProperties tenant;

    private FineractModeProperties mode;

    private FineractCorrelationProperties correlation;



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
        private String protocol;
        private String subprotocol;
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

    public static class FineractContentProperties {
        private boolean regexWhitelistEnabled;
        private List<String> regexWhitelist;
        private boolean mimeWhitelistEnabled;
        private List<String> mimeWhitelist;
        private FineractContentFilesystemProperties filesystem;
        private FineractContentS3Properties s3;

        public boolean isRegexWhitelistEnabled() {
            return regexWhitelistEnabled;
        }

        public void setRegexWhitelistEnabled(boolean regexWhitelistEnabled) {
            this.regexWhitelistEnabled = regexWhitelistEnabled;
        }

        public List<String> getRegexWhitelist() {
            return regexWhitelist;
        }

        public void setRegexWhitelist(List<String> regexWhitelist) {
            this.regexWhitelist = regexWhitelist;
        }

        public boolean isMimeWhitelistEnabled() {
            return mimeWhitelistEnabled;
        }

        public void setMimeWhitelistEnabled(boolean mimeWhitelistEnabled) {
            this.mimeWhitelistEnabled = mimeWhitelistEnabled;
        }

        public List<String> getMimeWhitelist() {
            return mimeWhitelist;
        }

        public void setMimeWhitelist(List<String> mimeWhitelist) {
            this.mimeWhitelist = mimeWhitelist;
        }

        public FineractContentFilesystemProperties getFilesystem() {
            return filesystem;
        }

        public void setFilesystem(FineractContentFilesystemProperties filesystem) {
            this.filesystem = filesystem;
        }

        public FineractContentS3Properties getS3() {
            return s3;
        }

        public void setS3(FineractContentS3Properties s3) {
            this.s3 = s3;
        }
    }



    public static class FineractContentFilesystemProperties {

        private String rootFolder;

        public String getRootFolder() {
            return rootFolder;
        }

        public void setRootFolder(String rootFolder) {
            this.rootFolder = rootFolder;
        }
    }

    public static class FineractContentS3Properties {

        private String bucketName;
        private String accessKey;
        private String secretKey;

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
