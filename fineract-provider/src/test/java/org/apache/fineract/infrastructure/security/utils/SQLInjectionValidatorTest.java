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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class SQLInjectionValidatorTest {

    private static final String[] DDL_COMMANDS = { "create", "drop", "alter", "truncate", "comment", "sleep" };
    private static final String[] DML_COMMANDS = { "select", "insert", "update", "delete", "merge", "upsert", "call" };
    private static final String[] COMMENTS = { "--", "({", "/*", "#" };

    @Test
    public void testValidateSqlInputQuote() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateSQLInput("' or 1=1");
        });
    }

    @Test
    public void testValidateSqlInputSemicolon() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateSQLInput("; drop table foo;");
        });
    }

    @Test
    public void testValidateAdhocQueryQuote() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateAdhocQuery("' or 1=1");
        });
    }

    @Test
    public void testValidateAdhocQuerySemicolon() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateAdhocQuery("; drop table foo;");
        });
    }

    @Test
    public void testValidateDynamicQueryQuote() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateDynamicQuery("' or 1=1");
        });
    }

    @Test
    public void testValidateDynamicQuerySemicolon() {
        Assertions.assertThrows(SQLInjectionException.class, () -> {
            SQLInjectionValidator.validateDynamicQuery("; drop table foo;");
        });
    }

    @Test
    public void testValidateSqlLInputReservedWords() {
        Arrays.asList(DDL_COMMANDS).forEach(ddl -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateSQLInput(ddl);
            });
        });

        Arrays.asList(DML_COMMANDS).forEach(dml -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateSQLInput(dml);
            });
        });

        Arrays.asList(COMMENTS).forEach(comment -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateSQLInput(comment);
            });
        });
    }

    @Test
    public void testValidateAdhocQueryReservedWords() {
        Arrays.asList(DDL_COMMANDS).forEach(ddl -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateAdhocQuery(ddl);
            });
        });

        // left out intentionally from adhocquery validation?
        // Arrays.asList(DML_COMMANDS).forEach(dml -> {
        // Assertions.assertThrows(SQLInjectionException.class, () -> {
        // SQLInjectionValidator.validateAdhocQuery(dml);
        // });
        // });

        Arrays.asList(COMMENTS).forEach(comment -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateAdhocQuery(comment);
            });
        });
    }

    @Test
    public void testValidateDynamicQueryReservedWords() {
        Arrays.asList(DDL_COMMANDS).forEach(ddl -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateDynamicQuery(ddl);
            });
        });

        Arrays.asList(DML_COMMANDS).forEach(dml -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateDynamicQuery(dml);
            });
        });

        Arrays.asList(COMMENTS).forEach(comment -> {
            Assertions.assertThrows(SQLInjectionException.class, () -> {
                SQLInjectionValidator.validateDynamicQuery(comment);
            });
        });
    }

    @Test
    public void testValidateDynamicQueryColon() {
        SQLInjectionValidator.validateDynamicQuery("2022-10-13 18:40:21");
    }

    @Test
    public void testValidateDynamicQueryReservedWordsInsideValue() {
        Arrays.asList(DDL_COMMANDS).forEach(ddl -> SQLInjectionValidator.validateDynamicQuery("foo" + ddl));
        Arrays.asList(DML_COMMANDS).forEach(dml -> SQLInjectionValidator.validateDynamicQuery("foo" + dml));
    }
}
