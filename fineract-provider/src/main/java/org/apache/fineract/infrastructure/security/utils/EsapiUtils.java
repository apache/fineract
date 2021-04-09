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

package org.apache.fineract.infrastructure.security.utils;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.MySQLCodec.Mode;

/** This is used for general ESAPI (The OWASP Enterprise Security API) utils. */
public final class EsapiUtils {

    private EsapiUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Codec<?> MYSQL_CODEC = new MySQLCodec(Mode.STANDARD);

    /**
     * Returns Encoded input for use in a MySQL query according to SQL {@code Mode.STANDARD} codec.
     *
     * <p>
     * This method is not recommended. The use of the {@code PreparedStatement} interface is the preferred approach.
     * However, if for some reason this is impossible, then this method is provided as a weaker alternative.
     *
     * @param aInput
     *            the text to encode for SQL
     * @return aInput encoded for use in SQL
     */
    public static String encodeForMySQL(String aInput) {
        return ESAPI.encoder().encodeForSQL(MYSQL_CODEC, aInput);
    }
}
