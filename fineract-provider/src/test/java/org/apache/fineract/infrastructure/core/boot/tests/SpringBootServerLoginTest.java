package org.apache.fineract.infrastructure.core.boot.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.fineract.ServerApplication;
import org.apache.fineract.common.RestAssuredFixture;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is an integration test for the Spring Boot launch stuff.
 * 
 * @see ServerApplication
 */
public class SpringBootServerLoginTest extends AbstractSpringBootWithMariaDB4jIntegrationTest {

    protected RestAssuredFixture util;

    @Test
    @Ignore("Failing on Cloubees")
    public void hasPlatformStarted() {
        util = new RestAssuredFixture(8443);
        List<Map<String, String>> response = util.httpGet("/users");
        assertThat(response.get(0).get("username"), is("mifos"));
    }

}