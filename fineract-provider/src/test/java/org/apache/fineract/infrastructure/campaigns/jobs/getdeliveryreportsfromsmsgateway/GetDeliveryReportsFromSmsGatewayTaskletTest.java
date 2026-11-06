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
package org.apache.fineract.infrastructure.campaigns.jobs.getdeliveryreportsfromsmsgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.service.SmsReadPlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;

@ExtendWith(MockitoExtension.class)
class GetDeliveryReportsFromSmsGatewayTaskletTest {

    @Mock
    private SmsReadPlatformService smsReadPlatformService;

    @Mock
    private SmsConfigUtils smsConfigUtils;

    @Mock
    private SmsMessageRepository smsMessageRepository;

    @Test
    void taskletDoesNotHangWhenGatewayAcceptsConnectionButNeverReplies() throws Exception {
        List<Socket> accepted = Collections.synchronizedList(new ArrayList<>());
        try (ServerSocket server = new ServerSocket(0)) {
            // Accept connections and never write a response.
            Thread acceptor = new Thread(() -> {
                try {
                    while (!server.isClosed()) {
                        accepted.add(server.accept());
                    }
                } catch (IOException e) {
                    // server closed at end of test
                }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            URI uri = URI.create("http://localhost:" + server.getLocalPort() + "/sms/report");
            when(smsReadPlatformService.retrieveAllWaitingForDeliveryReport(any())).thenReturn(new Page<>(List.of(1L), 1));
            when(smsConfigUtils.getMessageGateWayRequestURI(anyString(), anyString()))
                    .thenReturn(Map.of("uri", uri, "entity", new HttpEntity<>("[]")));

            GetDeliveryReportsFromSmsGatewayTasklet tasklet = new GetDeliveryReportsFromSmsGatewayTasklet(smsReadPlatformService,
                    smsConfigUtils, smsMessageRepository);

            try {
                assertTimeoutPreemptively(Duration.ofSeconds(60), () -> tasklet.execute(null, null));
            } finally {
                for (Socket s : accepted) {
                    s.close();
                }
                // Checked even after a timeout, so "no connection reached the gateway" is reported as such.
                assertEquals(1, accepted.size(), "gateway socket must receive exactly one connection");
            }
        }
    }
}
