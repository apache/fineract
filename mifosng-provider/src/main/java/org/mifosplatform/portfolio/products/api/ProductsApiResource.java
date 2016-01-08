package org.mifosplatform.portfolio.products.api;

import javax.ws.rs.Path;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/products/{type}/{productId}")
@Component
@Scope("singleton")
public class ProductsApiResource {

}
