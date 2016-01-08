package org.mifosplatform.portfolio.accounts.api;

import javax.ws.rs.Path;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/accounts/{type}/{accountId}")
@Component
@Scope("singleton")
public class AccountsApiResource {

}
