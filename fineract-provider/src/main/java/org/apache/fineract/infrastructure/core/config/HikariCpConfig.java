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

package org.apache.fineract.infrastructure.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class HikariCpConfig {

    @Autowired
    ApplicationContext context;

    @Bean(destroyMethod = "close")
    public HikariDataSource hikariTenantDataSource(HikariConfig hc) {
        return new HikariDataSource(hc);
    }

    @Bean
    public HikariConfig hikariConfig() {
        Environment environment = context.getEnvironment();
        HikariConfig hc = new HikariConfig();

        hc.setDriverClassName(environment.getProperty("fineract_tenants_driver"));
        hc.setJdbcUrl(environment.getProperty("fineract_tenants_url"));
        hc.setUsername(environment.getProperty("fineract_tenants_uid"));
        hc.setPassword(environment.getProperty("fineract_tenants_pwd"));
        hc.setMinimumIdle(3);
        hc.setMaximumPoolSize(10);
        hc.setIdleTimeout(60000);
        hc.setConnectionTestQuery("SELECT 1");
        hc.setDataSourceProperties(dataSourceProperties());

        return hc;
    }

    // These are the properties for the all Tenants DB; the same configuration is also (hard-coded) in the
    // TomcatJdbcDataSourcePerTenantService class -->
    private Properties dataSourceProperties() {
        Properties props = new Properties();

        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("useLocalSessionState", "true");
        props.setProperty("rewriteBatchedStatements", "true");
        props.setProperty("cacheResultSetMetadata", "true");
        props.setProperty("cacheServerConfiguration", "true");
        props.setProperty("elideSetAutoCommits", "true");
        props.setProperty("maintainTimeStats", "false");

        // https://github.com/brettwooldridge/HikariCP/wiki/JDBC-Logging#mysql-connectorj
        // TODO FINERACT-890: <prop key="logger">com.mysql.cj.log.Slf4JLogger</prop>
        props.setProperty("logSlowQueries", "true");
        props.setProperty("dumpQueriesOnException", "true");

        return props;
    }
}
