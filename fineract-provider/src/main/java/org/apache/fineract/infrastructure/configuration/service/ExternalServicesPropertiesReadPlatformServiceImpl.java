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
package org.apache.fineract.infrastructure.configuration.service;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.sms.data.MessageGatewayConfigurationData;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.apache.fineract.infrastructure.configuration.data.S3CredentialsData;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.apache.fineract.infrastructure.core.service.StringUtil;
import org.apache.fineract.infrastructure.gcm.domain.NotificationConfigurationData;
import org.apache.fineract.query.QCExternalService;
import org.apache.fineract.query.QCExternalServiceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalServicesPropertiesReadPlatformServiceImpl implements ExternalServicesPropertiesReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;
    private final QCExternalServiceProperties esp = new QCExternalServiceProperties("esp");
    private final QCExternalService es = new QCExternalService("es");

    private static final class ExternalServiceMapper implements RowMapper<ExternalServicesPropertiesData> {

        List<String> secretAttributes;

        ExternalServiceMapper() {
            secretAttributes = new ArrayList<>();
            secretAttributes.add("password");
            secretAttributes.add("server_key");
        }

        @Override
        public ExternalServicesPropertiesData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final String name = rs.getString("name");
            String value = rs.getString("value");
            // Masking the password as we should not send the password back
            if (name != null && secretAttributes.contains(name)) {
                value = StringUtil.maskValue(value);
            }
            return new ExternalServicesPropertiesData().setName(name).setValue(value);
        }

    }

    private static final class MessageGatewayDataExtractor implements ResultSetExtractor<MessageGatewayConfigurationData> {

        @Override
        public MessageGatewayConfigurationData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            String host = null;
            int port = 9191;
            String endPoint = null;
            String tenantAppKey = null;

            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMS_HOST)) {
                    host = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMS_PORT)) {
                    port = rs.getInt("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMS_END_POINT)) {
                    endPoint = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMS_TENANT_APP_KEY)) {
                    tenantAppKey = rs.getString("value");
                }
            }
            return new MessageGatewayConfigurationData(null, null, host, port, endPoint, null, null, false, tenantAppKey);
        }
    }

    @Override
    public S3CredentialsData getS3Credentials() {
        List<Tuple> rows = queryFactory.select(esp.name, esp.value).from(esp).innerJoin(es).on(esp.externalServiceId.eq(es.id))
                .where(es.name.eq(ExternalServicesConstants.S3_SERVICE_NAME)).fetch();

        S3CredentialsData s3CredentialsData = new S3CredentialsData();
        for (Tuple t : rows) {
            String name = t.get(esp.name);
            String value = t.get(esp.value);
            if (ExternalServicesConstants.S3_ACCESS_KEY.equalsIgnoreCase(name)) {
                s3CredentialsData.setAccessKey(value);
            } else if (ExternalServicesConstants.S3_BUCKET_NAME.equalsIgnoreCase(name)) {
                s3CredentialsData.setBucketName(value);
            } else if (ExternalServicesConstants.S3_SECRET_KEY.equalsIgnoreCase(name)) {
                s3CredentialsData.setSecretKey(value);
            }
        }
        return s3CredentialsData;
    }

    @Override
    public SMTPCredentialsData getSMTPCredentials() {

        List<Tuple> rows = queryFactory.select(esp.name, esp.value).from(esp).innerJoin(es).on(esp.externalServiceId.eq(es.id))
                .where(es.name.eq(ExternalServicesConstants.SMTP_SERVICE_NAME)).fetch();

        SMTPCredentialsData smtpCredentialsData = new SMTPCredentialsData().setPort("587");
        for (Tuple t : rows) {
            String name = t.get(esp.name);
            String value = t.get(esp.value);
            if (ExternalServicesConstants.SMTP_USERNAME.equalsIgnoreCase(name)) {
                smtpCredentialsData.setUsername(value);
            } else if (ExternalServicesConstants.SMTP_PASSWORD.equalsIgnoreCase(name)) {
                smtpCredentialsData.setPassword(value);
            } else if (ExternalServicesConstants.SMTP_HOST.equalsIgnoreCase(name)) {
                smtpCredentialsData.setHost(value);
            } else if (ExternalServicesConstants.SMTP_USE_TLS.equalsIgnoreCase(name)) {
                smtpCredentialsData.setUseTLS(Boolean.parseBoolean(value));
            } else if (ExternalServicesConstants.SMTP_FROM_EMAIL.equalsIgnoreCase(name)) {
                smtpCredentialsData.setFromEmail(value);
            } else if (ExternalServicesConstants.SMTP_FROM_NAME.equalsIgnoreCase(name)) {
                smtpCredentialsData.setFromName(value);
            } else if (ExternalServicesConstants.SMTP_PORT.equalsIgnoreCase(name)) {
                smtpCredentialsData.setPort(value);
            }
        }

        return smtpCredentialsData;
    }

    @Override
    public MessageGatewayConfigurationData getSMSGateway() {
        final ResultSetExtractor<MessageGatewayConfigurationData> resultSetExtractor = new MessageGatewayDataExtractor();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + ExternalServicesConstants.SMS_SERVICE_NAME + "'";
        final MessageGatewayConfigurationData messageGatewayConfigurationData = this.jdbcTemplate.query(sql, resultSetExtractor,
                new Object[] {});
        return messageGatewayConfigurationData;
    }

    @Override
    public Collection<ExternalServicesPropertiesData> retrieveOne(String serviceName) {
        String serviceNameToUse = null;
        switch (serviceName) {
            case "S3":
                serviceNameToUse = ExternalServicesConstants.S3_SERVICE_NAME;
            break;

            case "SMTP":
                serviceNameToUse = ExternalServicesConstants.SMTP_SERVICE_NAME;
            break;

            case "SMS":
                serviceNameToUse = ExternalServicesConstants.SMS_SERVICE_NAME;
            break;

            case "NOTIFICATION":
                serviceNameToUse = ExternalServicesConstants.NOTIFICATION_SERVICE_NAME;
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(serviceName);
        }
        final ExternalServiceMapper mapper = new ExternalServiceMapper();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + serviceNameToUse + "'";
        return this.jdbcTemplate.query(sql, mapper); // NOSONAR

    }

    private static final class NotificationDataExtractor implements ResultSetExtractor<NotificationConfigurationData> {

        @Override
        public NotificationConfigurationData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            String serverKey = null;
            String gcmEndPoint = null;
            String fcmEndPoint = null;
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.NOTIFICATION_SERVER_KEY)) {
                    serverKey = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.NOTIFICATION_GCM_END_POINT)) {
                    gcmEndPoint = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.NOTIFICATION_FCM_END_POINT)) {
                    fcmEndPoint = rs.getString("value");
                }
            }
            return new NotificationConfigurationData().setServerKey(serverKey).setGcmEndPoint(gcmEndPoint).setFcmEndPoint(fcmEndPoint);
        }
    }

    @Override
    public NotificationConfigurationData getNotificationConfiguration() {
        final ResultSetExtractor<NotificationConfigurationData> resultSetExtractor = new NotificationDataExtractor();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + ExternalServicesConstants.NOTIFICATION_SERVICE_NAME + "'";
        final NotificationConfigurationData notificationConfigurationData = this.jdbcTemplate.query(sql, resultSetExtractor,
                new Object[] {});
        return notificationConfigurationData;
    }

}
