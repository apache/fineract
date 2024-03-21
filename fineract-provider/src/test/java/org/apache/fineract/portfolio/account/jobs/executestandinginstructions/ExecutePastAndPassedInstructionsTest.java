package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExecutePastAndPassedInstructionsTest {

    private final LocalDate currentDate = LocalDate.now(Clock.systemUTC());
    private final LocalDate previousDate = currentDate.minusDays(2);

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Africa/Kampala", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testAcceptPreviousDateAsDue() {
        ExecuteStandingInstructionsTasklet tasklet = new ExecuteStandingInstructionsTasklet(null, null, null, null);
        boolean isDueForTransfer = tasklet.isDueForTransfer(new StandingInstructionDuesData(previousDate, BigDecimal.ONE));
        assertThat(isDueForTransfer).isTrue().describedAs("Earlier instructions are accepted as due");
    }

    @Test
    public void testAcceptCurrentDateAsDue() {
        ExecuteStandingInstructionsTasklet tasklet = new ExecuteStandingInstructionsTasklet(null, null, null, null);
        boolean isDueForTransfer = tasklet.isDueForTransfer(new StandingInstructionDuesData(currentDate, BigDecimal.ONE));
        assertThat(isDueForTransfer).isTrue().describedAs("Current day instructions are accepted as due");
    }
}
