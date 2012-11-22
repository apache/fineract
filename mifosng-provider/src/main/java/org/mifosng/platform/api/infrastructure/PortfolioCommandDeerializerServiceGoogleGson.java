package org.mifosng.platform.api.infrastructure;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.infrastructure.api.JsonParserHelper;
import org.mifosng.platform.infrastructure.errorhandling.InvalidJsonException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Implementation of {@link PortfolioCommandDeserializerService} that de-serializes
 * JSON of commands into Java object representation using google-gson.
 */
@Service
public class PortfolioCommandDeerializerServiceGoogleGson implements PortfolioCommandDeserializerService {

    private final JsonParser parser;

    public PortfolioCommandDeerializerServiceGoogleGson() {
        parser = new JsonParser();
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
}