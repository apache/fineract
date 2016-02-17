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
package org.apache.fineract.infrastructure.core.boot;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JDBCDriverConfig {

	private final static String DRIVER_CLASS_PROPERTYNAME = "DRIVERCLASS_NAME" ;
	private final static String PROTOCOL_PROPERTYNAME = "PROTOCOL" ;
	private final static String SUBPROTOCOL_PROPERTYNAME = "SUB_PROTOCOL" ;
	private final static String PORT_PROPERTYNAME = "PORT" ;
	
	private String driverClassName ;
	private String protocol ;
	private String subProtocol ;
	private Integer port ;
	
	@Autowired ApplicationContext context ;
	
	@PostConstruct
	protected void init() {
		Environment environment = context.getEnvironment() ;
		driverClassName = (String)environment.getProperty(DRIVER_CLASS_PROPERTYNAME) ;
		protocol = (String) environment.getProperty(PROTOCOL_PROPERTYNAME) ;
		subProtocol = (String) environment.getProperty(SUBPROTOCOL_PROPERTYNAME) ;
		port = Integer.parseInt((String) environment.getProperty(PORT_PROPERTYNAME)) ;
	}
	
    public String getDriverClassName() {
    	return this.driverClassName ;
    }
    
    public String getProtocol() {
    	return this.protocol ;
    }

    public String getSubProtocol() {
    	return this.subProtocol ;
    }
    
    public Integer getPort() {
    	return this.port ;
    }
    
    public String constructProtocol(String schemaServer, String schemaServerPort, String schemaName) {
    	final String url = new StringBuilder(protocol).append(":").append(subProtocol).append("://").append(schemaServer).append(':').append(schemaServerPort)
                .append('/').append(schemaName).toString();
        return url;
    }

}
