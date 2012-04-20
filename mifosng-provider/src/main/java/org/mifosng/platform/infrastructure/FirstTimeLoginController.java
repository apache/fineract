package org.mifosng.platform.infrastructure;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.user.domain.PasswordMustBeDifferentException;
import org.mifosng.platform.user.domain.UsernameMustBeDifferentException;
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
import org.springframework.web.servlet.view.RedirectView;

@Controller
@SessionAttributes("firstTimeLoginFormBean")
public class FirstTimeLoginController {

	@Autowired
	private WritePlatformService signupPlatformService;

	@ModelAttribute("firstTimeLoginFormBean")
	@RequestMapping(value = "/firsttimelogin", method = RequestMethod.GET)
	public FirstTimeLoginFormBean loadSignupForm(final Principal principal,
			final HttpSession httpSession) {

		String successView = "/index";
		String savedRequestView = (String) httpSession
				.getAttribute("firsttimeloginview");
		if (StringUtils.isNotBlank(savedRequestView)) {
			successView = savedRequestView;
		}

		FirstTimeLoginFormBean bean = new FirstTimeLoginFormBean();
		bean.setOldUsername(principal.getName());
		bean.setSuccessView(successView);

		return bean;
	}

	@ModelAttribute("firstTimeLoginFormBean")
	@RequestMapping(value = "/firsttimelogin", method = RequestMethod.POST)
	public ModelAndView handleSignup(
			@ModelAttribute("firstTimeLoginFormBean") final FirstTimeLoginFormBean firstTimeLoginFormBean,
			final BindingResult result, final SessionStatus status) {

		if (result.hasErrors()) {
			return new ModelAndView("firsttimelogin", "firstTimeLoginFormBean",
					firstTimeLoginFormBean);
		}

		try {
			UpdateUsernamePasswordCommand command = new UpdateUsernamePasswordCommand(
					firstTimeLoginFormBean.getOldUsername(),
					firstTimeLoginFormBean.getUsername(),
					firstTimeLoginFormBean.getPassword());

			this.signupPlatformService
					.updateUsernamePasswordOnFirstTimeLogin(command);

			status.setComplete();

			RedirectView redirectView = new RedirectView(
					firstTimeLoginFormBean.getSuccessView());
			
			if (!firstTimeLoginFormBean.getSuccessView().startsWith("http")) {
				redirectView.setContextRelative(true);
			}

			return new ModelAndView(redirectView);
		} catch (UsernameAlreadyExistsException e) {
			ObjectError error = new ObjectError("firstTimeLoginFormBean",
					new String[] { "username.already.exists" },
					new Object[] { firstTimeLoginFormBean.getUsername() },
					"An user with username {0} already exists.");
			result.addError(error);
			return new ModelAndView("firsttimelogin", "firstTimeLoginFormBean",
					firstTimeLoginFormBean);
		} catch (UsernameMustBeDifferentException e) {
			ObjectError error = new ObjectError("firstTimeLoginFormBean",
					new String[] { "new.username.must.be.different" },
					new Object[] {},
					"The new username cannot be the same as existing username.");
			result.addError(error);
			return new ModelAndView("firsttimelogin", "firstTimeLoginFormBean",
					firstTimeLoginFormBean);
		} catch (PasswordMustBeDifferentException e) {
			ObjectError error = new ObjectError("firstTimeLoginFormBean",
					new String[] { "new.password.must.be.different" },
					new Object[] {},
					"The new password cannot be the same as existing password.");
			result.addError(error);
			return new ModelAndView("firsttimelogin", "firstTimeLoginFormBean",
					firstTimeLoginFormBean);
		}
	}
}