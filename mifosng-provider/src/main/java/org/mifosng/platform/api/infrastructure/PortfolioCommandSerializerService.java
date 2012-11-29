package org.mifosng.platform.api.infrastructure;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.configuration.command.CurrencyCommand;
import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.command.OfficeCommand;
import org.mifosplatform.infrastructure.staff.command.StaffCommand;
import org.mifosplatform.infrastructure.user.command.PermissionsCommand;
import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.command.UserCommand;
import org.mifosplatform.portfolio.fund.command.FundCommand;

/**
 * Service for serializing commands into another format.
 * 
 * <p>Known implementations:</p>
 * @see PortfolioCommandSerializerServiceJson 
 */
public interface PortfolioCommandSerializerService {

    String serializeRoleCommandToJson(RoleCommand command);

    String serializeRolePermissionCommandToJson(RolePermissionCommand command);

    String serializePermissionsCommandToJson(PermissionsCommand command);
    
    String serializeUserCommandToJson(UserCommand command);

    String serializeCodeCommandToJson(CodeCommand command);

    String serializeStaffCommandToJson(StaffCommand command);

    String serializeFundCommandToJson(FundCommand command);

    String serializeOfficeCommandToJson(OfficeCommand command);

    String serializeOfficeTransactionCommandToJson(BranchMoneyTransferCommand command);

    String serializeCurrencyCommandToJson(CurrencyCommand command);
}