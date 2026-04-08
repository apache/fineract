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

package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ActuatorIntegrationTest {

    private static final String INFO_URL = "/fineract-provider/actuator/info";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    public void testActuatorGitBuildInfo() {
        log.info(INFO_URL);
        Response response = RestAssured.given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).when().get(INFO_URL)
                .then().contentType(ContentType.JSON).extract().response();

        Map<String, String> gitBuildInfo = response.jsonPath().getMap("git");

        assertTrue(gitBuildInfo.containsKey("branch"));
        assertTrue(gitBuildInfo.containsKey("remote"));

    }

}
