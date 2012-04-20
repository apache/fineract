package org.mifosng.platform.infrastructure;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.command.SignupCommand;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.InvalidSignupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * @deprecated - remove signup functionality for now. 
 */
@Deprecated
@Controller
@SessionAttributes("signupFormBean")
public class SignupController {

	private final WritePlatformService signupPlatformService;

	@Autowired
	public SignupController(WritePlatformService signupPlatformService) {
		this.signupPlatformService = signupPlatformService;
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public ModelAndView loadSignupForm() {

		SignupFormBean bean = new SignupFormBean();
		return new ModelAndView("/tenant/signup", "signupFormBean", bean);
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ModelAndView handleSignup(
			@ModelAttribute("signupFormBean") final SignupFormBean signupFormBean,
			final BindingResult result, final SessionStatus status) {

		final String signupFormView = "/tenant/signup";

		if (result.hasErrors()) {
			return new ModelAndView(signupFormView, "signupFormBean",
					signupFormBean);
		}

		if (StringUtils.isBlank(signupFormBean.getOrganisationName())) {
			ObjectError error = new ObjectError("signupFormBean",
					new String[] { "organisationName.blank" }, new Object[] {},
					"Organisation name cannot be empty.");

			result.addError(error);
		}

		if (signupFormBean.getOpeningDate() == null) {
			ObjectError error = new ObjectError("signupFormBean",
					new String[] { "openingDate.blank" }, new Object[] {},
					"Organisation Founded date cannot be empty.");

			result.addError(error);
		}

		if (StringUtils.isBlank(signupFormBean.getContactEmail())) {

			ObjectError error = new ObjectError("signupFormBean",
					new String[] { "contactEmail.blank" }, new Object[] {},
					"Contact email cannot be empty.");

			result.addError(error);
		}

		if (StringUtils.isBlank(signupFormBean.getContactName())) {
			ObjectError error = new ObjectError("signupFormBean",
					new String[] { "contactName.blank" }, new Object[] {},
					"Contact name cannot be empty.");

			result.addError(error);
		}

		if (result.hasErrors()) {
			return new ModelAndView(signupFormView, "signupFormBean",
					signupFormBean);
		}

		SignupCommand command = new SignupCommand(
				signupFormBean.getOrganisationName(),
				signupFormBean.getContactEmail(),
				signupFormBean.getContactName(),
				signupFormBean.getOpeningDate());

		ModelAndView mav = null;
		try {
			signupPlatformService.signup(command);

			status.setComplete();
			mav = new ModelAndView("/tenant/signupsuccess", "signupFormBean",
					signupFormBean);
		} catch (InvalidSignupException e) {
			// FIXME - InvalidSignupException is really
			// TenantAlreadyExistsException as a tenant already exists with
			// given organisationName
			
			Object[] errorArgs = new Object[] { signupFormBean.getOrganisationName() };
			ObjectError error = new ObjectError("signupFormBean",
					new String[] { "organisation.name.already.exists" }, errorArgs,
					"An organisation with name {0} already exists.");
			result.addError(error);
		} catch (PlatformEmailSendException e) {
			Object[] errorArgs = new Object[] { signupFormBean.getContactEmail() };
			ObjectError error = new ObjectError(
					"signupFormBean",
					new String[] { "email.failed.to.send"},
					errorArgs,
					"The provided email address [{0}] is not valid or our email service is presently not working, if you believe your email to be correct, please try again in a few minutes.");
			result.addError(error);
		}

		if (result.hasErrors()) {
			mav = new ModelAndView(signupFormView, "signupFormBean",
					signupFormBean);
		}

		return mav;
	}
}