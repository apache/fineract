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

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLInjectionValidator {

	private final static String[] DDL_COMMANDS = { "create", "drop", "alter", "truncate", "comment", "sleep" };

	private final static String[] DML_COMMANDS = { "select", "insert", "update", "delete", "merge", "upsert", "call" };

	private final static String[] COMMENTS = { "--", "({", "/*", "#" };

	private final static String SQL_PATTERN = "[a-zA-Z_=,\\-'!><.?\"`% ()0-9*\n\r]*";

	public final static void validateSQLInput(final String sqlSearch) {
		String lowerCaseSQL = sqlSearch.toLowerCase();
		for (String ddl : DDL_COMMANDS) {
			if (lowerCaseSQL.contains(ddl)) {
				throw new SQLInjectionException();
			}
		}

		for (String dml : DML_COMMANDS) {
			if (lowerCaseSQL.contains(dml)) {
				throw new SQLInjectionException();
			}
		}

		for (String comments : COMMENTS) {
			if (lowerCaseSQL.contains(comments)) {
				throw new SQLInjectionException();
			}
		}

		//Removing the space before and after '=' operator 
		//String s = "          \"              OR 1    =    1"; For the cases like this
		boolean injectionFound = false;
		String inputSqlString = lowerCaseSQL;
		while (inputSqlString.indexOf(" =") > 0) { //Don't remove space before = operator
			inputSqlString = inputSqlString.replaceAll(" =", "=");
		}

		while (inputSqlString.indexOf("= ") > 0) { //Don't remove space after = operator
			inputSqlString = inputSqlString.replaceAll("= ", "=");
		}

		StringTokenizer tokenizer = new StringTokenizer(inputSqlString, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (token.equals("'")) {
				if (tokenizer.hasMoreElements()) {
					String nextToken = tokenizer.nextToken().trim();
					if (!nextToken.equals("'")) {
						injectionFound = true;
						break;
					}
				} else {
					injectionFound = true;
					break ;
				}
			}
			if (token.equals("\"")) {
				if (tokenizer.hasMoreElements()) {
					String nextToken = tokenizer.nextToken().trim();
					if (!nextToken.equals("\"")) {
						injectionFound = true;
						break;
					}
				} else {
					injectionFound = true;
					break ;
				}
			} else if (token.indexOf('=') > 0) {
				StringTokenizer operatorToken = new StringTokenizer(token, "=");
				String operand = operatorToken.nextToken().trim();
				if (!operatorToken.hasMoreTokens()) {
					injectionFound = true;
					break;
				}
				String value = operatorToken.nextToken().trim();
				if (operand.equals(value)) {
					injectionFound = true;
					break;
				}
			}
		}
		if (injectionFound) {
			throw new SQLInjectionException();
		}
		
		Pattern pattern = Pattern.compile(SQL_PATTERN);
		Matcher matcher = pattern.matcher(sqlSearch);
		if (!matcher.matches()) {
			throw new SQLInjectionException();
		}
	}
	public final static void validateAdhocQuery(final String sqlSearch) {
		String lowerCaseSQL = sqlSearch.toLowerCase().trim();
		for (String ddl : DDL_COMMANDS) {
			if (lowerCaseSQL.startsWith(ddl)) {
				throw new SQLInjectionException();
			}
		}

		
		for (String comments : COMMENTS) {
			if (lowerCaseSQL.contains(comments)) {
				throw new SQLInjectionException();
			}
		}

		//Removing the space before and after '=' operator 
		//String s = "          \"              OR 1    =    1"; For the cases like this
		boolean injectionFound = false;
		String inputSqlString = lowerCaseSQL;
		while (inputSqlString.indexOf(" =") > 0) { //Don't remove space before = operator
			inputSqlString = inputSqlString.replaceAll(" =", "=");
		}

		while (inputSqlString.indexOf("= ") > 0) { //Don't remove space after = operator
			inputSqlString = inputSqlString.replaceAll("= ", "=");
		}

		StringTokenizer tokenizer = new StringTokenizer(inputSqlString, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (token.equals("'")) {
				if (tokenizer.hasMoreElements()) {
					String nextToken = tokenizer.nextToken().trim();
					if (!nextToken.equals("'")) {
						injectionFound = true;
						break;
					}
				} else {
					injectionFound = true;
					break ;
				}
			}
			if (token.equals("\"")) {
				if (tokenizer.hasMoreElements()) {
					String nextToken = tokenizer.nextToken().trim();
					if (!nextToken.equals("\"")) {
						injectionFound = true;
						break;
					}
				} else {
					injectionFound = true;
					break ;
				}
			} else if (token.indexOf('=') > 0) {
				StringTokenizer operatorToken = new StringTokenizer(token, "=");
				String operand = operatorToken.nextToken().trim();
				if (!operatorToken.hasMoreTokens()) {
					injectionFound = true;
					break;
				}
				String value = operatorToken.nextToken().trim();
				if (operand.equals(value)) {
					injectionFound = true;
					break;
				}
			}
		}
		if (injectionFound) {
			throw new SQLInjectionException();
		}
		
		Pattern pattern = Pattern.compile(SQL_PATTERN);
		Matcher matcher = pattern.matcher(sqlSearch);
		if (!matcher.matches()) {
			throw new SQLInjectionException();
		}
	}
}
