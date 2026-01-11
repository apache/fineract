package ng.com.createsoftware.fn_account_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springramework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.assertJ.core.api.Assertions;
import org.junit.junipter.api.Test;

@SpringBootTest
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDtabaseConnection.H2)
class FnAccountServiceApplicationTests {
	@Autowired
	private AccountOfficerRepository accountOfficerRepository;

	@Test
	void contextLoads() {
	}

	@Test
	public void AccountOfficer_save_returnAccountOffficer(){
      AccountOfficer officer = AccountOfficer.builder()
          .staffId("staff1")
		  .code("Add Staff")
		  .firstName("John")
		  .lastName("Doe")
		  .email("mail@example.com")
		  .branchCode("Abuja")
		  .phone("08012345678")
		  .status(Status.ACTIVE)
		  .build();


		AccountOFficer officerSaved = accountOfficerRepository.save(officer);

		Assertions.assetThat(officerSaved).isNotNull();
	}

}
