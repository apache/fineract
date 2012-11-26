package org.mifosng.platform.api.infrastructure;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.accounting.api.commands.RolePermissionCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.infrastructure.api.JsonParserHelper;
import org.mifosng.platform.infrastructure.errorhandling.InvalidJsonException;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implementation of {@link PortfolioCommandDeserializerService} that de-serializes
 * JSON of commands into Java object representation using google-gson.
 */
@Service
public class PortfolioCommandDeerializerServiceGoogleGson implements PortfolioCommandDeserializerService {

    private final JsonParser parser;
    private final Gson gsonConverter;

    public PortfolioCommandDeerializerServiceGoogleGson() {
        parser = new JsonParser();
        gsonConverter = new Gson();
    }
    
    @Override
    public RoleCommand deserializeRoleCommand(final Long roleId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);
       
        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final String name = helper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = helper.extractStringNamed("description", element, parametersPassedInRequest);

        return new RoleCommand(parametersPassedInRequest, makerCheckerApproval, roleId, name, description);
    }

    @Override
    public RolePermissionCommand deserializeRolePermissionCommand(final Long roleId, final String commandAsJson, final boolean makerCheckerApproval) {
        
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }
        
        final RolePermissionCommand command = gsonConverter.fromJson(commandAsJson, RolePermissionCommand.class);

        return new RolePermissionCommand(roleId, command.getPermissions(), makerCheckerApproval);
    }

    @Override
    public UserCommand deserializeUserCommand(final Long userId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final JsonParserHelper helper = new JsonParserHelper();
        final JsonElement element = parser.parse(commandAsJson);
       
        final Set<String> parametersPassedInRequest = new HashSet<String>();
        
        final String username = helper.extractStringNamed("username", element, parametersPassedInRequest);
        final String firstname = helper.extractStringNamed("firstname", element, parametersPassedInRequest);
        
        final String lastname = helper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String password = helper.extractStringNamed("password", element, parametersPassedInRequest);
        final String repeatPassword = helper.extractStringNamed("repeatPassword", element, parametersPassedInRequest);
        final String email = helper.extractStringNamed("email", element, parametersPassedInRequest);
        final Long officeId = helper.extractLongNamed("officeId", element, parametersPassedInRequest);
        
        // check array
        String[] notSelectedRoles = null;
        String[] roles = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("notSelectedRoles")) {
                parametersPassedInRequest.add("notSelectedRoles");
                JsonArray array = object.get("notSelectedRoles").getAsJsonArray();
                notSelectedRoles = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    notSelectedRoles[i] = array.get(i).getAsString();
                }
            }

            if (object.has("roles")) {
                parametersPassedInRequest.add("roles");
                JsonArray array = object.get("roles").getAsJsonArray();
                roles = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    roles[i] = array.get(i).getAsString();
                }
            }
        }

        return new UserCommand(parametersPassedInRequest, makerCheckerApproval, userId, username, firstname, lastname, 
                password, repeatPassword, email, officeId, notSelectedRoles, roles);
    }
}