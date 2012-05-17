package org.mifosng.platform;

import static org.mifosng.platform.Specifications.usersThatMatch;

import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.infrastructure.BasicPasswordEncodablePlatformUser;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.infrastructure.UsernameAlreadyExistsException;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WritePlatformServiceJpaRepositoryImpl implements WritePlatformService {

	private final PlatformUserRepository platformUserRepository;
	private final PlatformPasswordEncoder platformPasswordEncoder;

	@Autowired
	public WritePlatformServiceJpaRepositoryImpl(
			final PlatformUserRepository platformUserRepository,
			final PlatformPasswordEncoder platformPasswordEncoder
			) {
		this.platformUserRepository = platformUserRepository;
		this.platformPasswordEncoder = platformPasswordEncoder;
	}
	
	@Transactional
	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final UpdateUsernamePasswordCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		try {
			PlatformUser platformUser = ((AppUserRepository) this.platformUserRepository).findOne(usersThatMatch(currentUser.getOrganisation(), command.getOldUsername()));

			PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
					((AppUser) platformUser).getId(),
					platformUser.getUsername(), command.getPassword());

			String encodePassword = this.platformPasswordEncoder.encode(dummyPlatformUser);

			if (command.isUsernameToBeChanged()) {
				platformUser.updateUsernamePasswordOnFirstTimeLogin(
						command.getUsername(), encodePassword);
			} else {
				platformUser.updatePasswordOnFirstTimeLogin(encodePassword);
			}

			((AppUserRepository) this.platformUserRepository).save((AppUser) platformUser);
		} catch (DataIntegrityViolationException e) {
			throw new UsernameAlreadyExistsException(e);
		}
	}

	private AppUser extractAuthenticatedUser() {
		AppUser currentUser = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication auth = context.getAuthentication();
			if (auth != null) {
				currentUser = (AppUser) auth.getPrincipal();
			}
		}

		if (currentUser == null) {
			throw new UnAuthenticatedUserException();
		}

		return currentUser;
	}
}