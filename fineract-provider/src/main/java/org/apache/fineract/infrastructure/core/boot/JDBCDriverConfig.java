/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JDBCDriverConfig {

    @Value("${drizzle.driver-classname}")
    private String driverClassName;
    @Value("${drizzle.protocol}")
    private String protocol;
    @Value("${drizzle.subprotocol}")
    private String subProtocol;
    @Value("${drizzle.port}")
    private Integer port;

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getSubProtocol() {
        return this.subProtocol;
    }

    public Integer getPort() {
        return this.port;
    }

    public String constructProtocol(String schemaServer, String schemaServerPort, String schemaName) {
        final String url = new StringBuilder(protocol).append(":").append(subProtocol).append("://").append(schemaServer).append(':').append(schemaServerPort)
                .append('/').append(schemaName).toString();
        return url;
    }

}
