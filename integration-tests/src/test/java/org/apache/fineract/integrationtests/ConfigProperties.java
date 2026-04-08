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
package org.apache.fineract.integrationtests;

public class ConfigProperties {

    public static class Backend {

        public static final String PROTOCOL = getValueFromEnvOrDefault("BACKEND_PROTOCOL", "https");
        public static final String HOST = getValueFromEnvOrDefault("BACKEND_HOST", "localhost");
        public static final Integer PORT = Integer.parseInt(getValueFromEnvOrDefault("BACKEND_PORT", "8443"));
        public static final String USERNAME = getValueFromEnvOrDefault("BACKEND_USERNAME", "mifos");
        public static final String PASSWORD = getValueFromEnvOrDefault("BACKEND_PASSWORD", "password");
        public static final String TENANT = getValueFromEnvOrDefault("BACKEND_TENANT", "default");
    }

    private static String getValueFromEnvOrDefault(final String key, final String defaultValue) {
        return System.getenv(key) == null ? defaultValue : System.getenv(key);
    }
}
