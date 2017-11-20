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
package org.apache.fineract.infrastructure.configuration.service;

import java.util.HashSet;
import java.util.Set;

public class ExternalServicesConstants {

    public static final String S3_SERVICE_NAME = "S3";
    public static final String S3_BUCKET_NAME = "s3_bucket_name";
    public static final String S3_ACCESS_KEY = "s3_access_key";
    public static final String S3_SECRET_KEY = "s3_secret_key";

    public static final String SMTP_SERVICE_NAME = "SMTP_Email_Account";
    public static final String SMTP_USERNAME = "username";
    public static final String SMTP_PASSWORD = "password";
    public static final String SMTP_HOST = "host";
    public static final String SMTP_PORT = "port";
    public static final String SMTP_USE_TLS = "useTLS";

    public static final String SMS_SERVICE_NAME = "MESSAGE_GATEWAY";
    public static final String SMS_HOST = "host_name";
    public static final String SMS_PORT = "port_number";
    public static final String SMS_END_POINT = "end_point";
    public static final String SMS_TENANT_APP_KEY = "tenant_app_key";
    
    public static final String NOTIFICATION_SERVICE_NAME = "NOTIFICATION";
    public static final String NOTIFICATION_SERVER_KEY = "server_key";
    public static final String NOTIFICATION_GCM_END_POINT = "gcm_end_point";
    public static final String NOTIFICATION_FCM_END_POINT = "fcm_end_point";
    
    public static enum EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS {
        EXTERNAL_SERVICE_ID("external_service_id"), NAME("name"), VALUE("value");

        private final String value;

        private EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS type : EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum SMTP_JSON_INPUT_PARAMS {
        USERNAME("username"), PASSWORD("password"), HOST("host"), PORT("port"), USETLS("useTLS");

        private final String value;

        private SMTP_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SMTP_JSON_INPUT_PARAMS type : SMTP_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }
    
    public static enum SMS_JSON_INPUT_PARAMS {
        HASTNAME("host_name"), PORT("port_number"), END_POINT("end_point"), TENANT_APP_KEY("tenant_app_key");

        private final String value;

        private SMS_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final SMS_JSON_INPUT_PARAMS type : SMS_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum S3_JSON_INPUT_PARAMS {
        S3_ACCESS_KEY("s3_access_key"), S3_BUCKET_NAME("s3_bucket_name"), S3_SECRET_KEY("s3_secret_key");

        private final String value;

        private S3_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final S3_JSON_INPUT_PARAMS type : S3_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }
    
    public static enum NOTIFICATION_JSON_INPUT_PARAMS {
        SERVER_KEY("server_key"), GCM_END_POINT("gcm_end_point"), FCM_END_POINT("fcm_end_point");

        private final String value;

        private NOTIFICATION_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final NOTIFICATION_JSON_INPUT_PARAMS type : NOTIFICATION_JSON_INPUT_PARAMS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

}
