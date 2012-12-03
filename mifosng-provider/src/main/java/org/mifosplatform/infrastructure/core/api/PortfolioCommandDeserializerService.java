package org.mifosplatform.infrastructure.core.api;

import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.mifosplatform.useradministration.command.UserCommand;

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

    StaffCommand deserializeStaffCommand(Long staffId, String commandAsJson, boolean makerCheckerApproval);

    FundCommand deserializeFundCommand(Long fundId, String commandAsJson, boolean makerCheckerApproval);

    OfficeCommand deserializeOfficeCommand(Long officeId, String commandAsJson, boolean makerCheckerApproval);

    BranchMoneyTransferCommand deserializeOfficeTransactionCommand(String commandAsJson, boolean makerCheckerApproval);

    CurrencyCommand deserializeCurrencyCommand(String commandAsJson, boolean makerCheckerApproval);

    ChargeDefinitionCommand deserializeChargeDefinitionCommand(Long chargeDefinitionId, String commandAsJson, boolean makerCheckerApproval);

    ClientCommand deserializeClientCommand(Long clientId, String commandAsJson, boolean makerCheckerApproval);

    LoanProductCommand deserializeLoanProductCommand(Long loanProductId, String commandAsJson, boolean makerCheckerApproval);

    ClientIdentifierCommand deserializeClientIdentifierCommand(Long clientIdentifierId, Long clientId, String commandAsJson,
            boolean makerCheckerApproval);
}