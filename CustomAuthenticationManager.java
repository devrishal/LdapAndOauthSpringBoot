package com.rishal.restapi.security;

import static org.springframework.util.StringUtils.isEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sbic.turbine.constant.ApplicationProperty;

/**
 * This class is responsible for authenticating the user using username and
 * password recieved as input from user using LDAP authentication.
 * 
 *
 */

@Component
public class CustomAuthenticationManager implements AuthenticationManager {
	@Autowired
	CustomLdapAuthenticator ldapAuthenticator;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		ServletRequestAttributes authContext = ((ServletRequestAttributes) (RequestContextHolder
				.currentRequestAttributes()));
		String authMode = authContext.getRequest().getParameter(ApplicationProperty.GRANT_TYPE);
		if (!isEmpty(authMode) && authMode.equals(ApplicationProperty.GRANT_TYPE_PASSWORD)) {
			return ldapAuthenticator.authenticate(authentication);
		}
		return authentication;

	}

}
