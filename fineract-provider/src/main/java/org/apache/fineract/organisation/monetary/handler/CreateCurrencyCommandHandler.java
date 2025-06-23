package org.apache.fineract.organisation.monetary.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.organisation.monetary.service.CurrencyWritePlatformService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@CommandType(entity = "CURRENCY", action = "CREATE")
@RequiredArgsConstructor
public class CreateCurrencyCommandHandler implements NewCommandSourceHandler{

	private final CurrencyWritePlatformService writePlatformService;

	@Transactional
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		 return this.writePlatformService.createAllowedCurrencies(command);
	}

}
