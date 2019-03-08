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
package org.apache.fineract.infrastructure.core.boot.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JdbcDriverConfig {

    @Value("${fineract.datasource.port}")
    private int port;
    
    @Value("${fineract.datasource.driver}")
    private String driver;

    @Value("${fineract.datasource.host}")
    private String hostname;

    @Value("${fineract.datasource.db}")
    private String dbName;

    @Value("${fineract.datasource.username}")
    private String username;

    @Value("${fineract.datasource.password}")
    private String password;

    @Value("${fineract.datasource.protocol}")
    private String jdbcProtocol;

    @Value("${fineract.datasource.subprotocol}")
    private String jdbcSubprotocol;
	
    public Integer getPort() {
    	return this.port ;
    }
    
    public String getDriverClassName() {
    	return this.driver ;
    }
 
    public String getHost() {
		return hostname;
	}

	public String getDbName() {
		return dbName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getProtocol() {
    	return this.jdbcProtocol ;
    }

    public String getSubProtocol() {
    	return this.jdbcSubprotocol ;
    }

    public String constructProtocol(String schemaServer, String schemaServerPort, String schemaName) {
    	return new StringBuilder(jdbcProtocol).append(":").append(jdbcSubprotocol).append("://").append(schemaServer).append(':').append(schemaServerPort)
                .append('/').append(schemaName).toString();
    }
}