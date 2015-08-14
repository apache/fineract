/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.infrastructure.codes.CodeConstants.CODEVALUE_JSON_INPUT_PARAMS;

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

}
