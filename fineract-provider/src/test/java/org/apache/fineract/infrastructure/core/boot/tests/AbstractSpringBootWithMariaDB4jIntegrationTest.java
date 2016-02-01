package org.apache.fineract.infrastructure.core.boot.tests;

import org.apache.fineract.ServerWithMariaDB4jApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServerWithMariaDB4jApplication.Configuration.class)
@WebAppConfiguration
@ActiveProfiles("basicauth")
@IntegrationTest({ "server.port=0", "management.port=0", "mariaDB4j.port=0", "mariaDB4j.dataDir=null" })
public abstract class AbstractSpringBootWithMariaDB4jIntegrationTest {

	// do NOT put any helper methods here!
	// it's much better to use composition instead of inheritance
	// so write a test util ("fixture") and use it as a field in your test

}