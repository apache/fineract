package org.mifosng.platform.api.infrastructure;

import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.configuration.command.CurrencyCommand;
import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.command.OfficeCommand;
import org.mifosplatform.infrastructure.staff.command.StaffCommand;
import org.mifosplatform.infrastructure.user.command.PermissionsCommand;
import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.command.UserCommand;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.fund.command.FundCommand;

/**
 * Service for de-serializing JSON for a command into the platforms internal
 * Java object representation of the command.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see PortfolioCommandDeerializerServiceGoogleGson
 */
public interface PortfolioCommandDeserializerService {

    RoleCommand deserializeRoleCommand(Long roleId, String commandAsJson, boolean makerCheckerApproval);

    RolePermissionCommand deserializeRolePermissionCommand(Long roleId, String commandAsJson, boolean makerCheckerApproval);

    PermissionsCommand deserializePermissionsCommand(String commandAsJson, boolean makerCheckerApproval);
    
    UserCommand deserializeUserCommand(Long userId, String commandAsJson, boolean makerCheckerApproval);

    CodeCommand deserializeCodeCommand(Long codeId, String commandAsJson, boolean makerCheckerApproval);

    StaffCommand deserializeStaffCommand(Long staffId, String commandAsJson, boolean makerCheckerApproval);

    FundCommand deserializeFundCommand(Long fundId, String commandAsJson, boolean makerCheckerApproval);

    OfficeCommand deserializeOfficeCommand(Long officeId, String commandAsJson, boolean makerCheckerApproval);

    BranchMoneyTransferCommand deserializeOfficeTransactionCommand(String commandAsJson, boolean makerCheckerApproval);

    CurrencyCommand deserializeCurrencyCommand(String commandAsJson, boolean makerCheckerApproval);

    ChargeDefinitionCommand deserializeChargeDefinitionCommand(Long chargeDefinitionId, String commandAsJson, boolean makerCheckerApproval);

    ClientCommand deserializeClientCommand(Long clientId, String commandAsJson, boolean makerCheckerApproval);
}