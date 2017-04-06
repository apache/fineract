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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLInjectionValidator {

	private final static String[] DDL_COMMANDS = {"create", "drop", "alter", "truncate", "comment"} ;
	
	private final static String[] DML_COMMANDS = {"select", "insert", "update", "delete", "merge", "upsert", "call"} ;
	
	private final static String SQL_PATTERN = "[a-zA-Z_, ()0-9]*" ;
	
	public final static void validateSQLInput(final String sqlSearch) {
		String lowerCaseSQL = sqlSearch.toLowerCase() ;
		for(String ddl: DDL_COMMANDS){
			if(lowerCaseSQL.contains(ddl)) {
				throw new SQLInjectionException() ;
			}
		}
		
		for(String dml: DML_COMMANDS){
			if(lowerCaseSQL.contains(dml)) {
				throw new SQLInjectionException() ;
			}
		}
		
		Pattern pattern = Pattern.compile(SQL_PATTERN);
		Matcher matcher = pattern.matcher(sqlSearch);
		if (!matcher.matches()) {
			throw new SQLInjectionException() ;
		}
	}
}
