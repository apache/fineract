package org.mifosng.platform.api.infrastructure;

import java.util.HashSet;

import org.mifosng.platform.infrastructure.api.GoogleGsonSerializerHelper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioCommandSerializerService} that serializes
 * the commands into JSON using google-gson.
 */
@Service
public class PortfolioCommandSerializerServiceJson implements PortfolioCommandSerializerService {

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public PortfolioCommandSerializerServiceJson(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeRoleCommandToJson(final RoleCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false,
                new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeRolePermissionCommandToJson(final RolePermissionCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }
    
    @Override
    public String serializePermissionsCommandToJson(final PermissionsCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeUserCommandToJson(final UserCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeCodeCommandToJson(final CodeCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeStaffCommandToJson(final StaffCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeFundCommandToJson(final FundCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeOfficeCommandToJson(final OfficeCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeOfficeTransactionCommandToJson(final BranchMoneyTransferCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeCurrencyCommandToJson(final CurrencyCommand command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }

    @Override
    public String serializeCommandToJson(Object command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false, new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }
}