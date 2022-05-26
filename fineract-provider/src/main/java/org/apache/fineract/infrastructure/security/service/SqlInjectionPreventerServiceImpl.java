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
package org.apache.fineract.infrastructure.security.service;

import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.security.exception.EscapeSqlLiteralException;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.postgresql.core.Utils;
import org.springframework.stereotype.Service;

@Service
public class SqlInjectionPreventerServiceImpl implements SqlInjectionPreventerService {

    private static final Codec<?> MYSQL_CODEC = new MySQLCodec(MySQLCodec.Mode.STANDARD);

    private final DatabaseTypeResolver databaseTypeResolver;

    public SqlInjectionPreventerServiceImpl(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    @Override
    public String encodeSql(String literal) {
        if (databaseTypeResolver.isMySQL()) {
            return ESAPI.encoder().encodeForSQL(MYSQL_CODEC, literal);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            try {
                return Utils.escapeLiteral(null, literal, true).toString();
            } catch (SQLException e) {
                throw new EscapeSqlLiteralException("Failed to escape an SQL literal. literal: " + literal, e);
            }
        } else {
            return literal;
        }
    }
}
