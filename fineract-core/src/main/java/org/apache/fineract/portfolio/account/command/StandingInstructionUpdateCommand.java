package org.apache.fineract.portfolio.account.command;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateRequest;

@Getter
@Setter
public class StandingInstructionUpdateCommand extends Command<StandingInstructionUpdateRequest> {

    @Serial
    private static final long serialVersionUID = 1L;
}
