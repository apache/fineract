package org.mifosng.platform.exceptions;

public class ProductNotFoundException extends AbstractPlatformResourceNotFoundException{

	public ProductNotFoundException(Long id) {
		super("error.msg.product.id.invalid", "Product with identifier " + id + " does not exist", id);
	}

}
