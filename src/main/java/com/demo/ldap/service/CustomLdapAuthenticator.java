package com.demo.ldap.service;

import java.util.ArrayList;

import com.demo.ldap.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * Class to provide customised Ldap Authentication, by using LDAP authentication
 * properties.
 *
 *
 *
 */
@Component
public class CustomLdapAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(CustomLdapAuthenticator.class);

    @Autowired
    private LdapUserServiceImpl ldapManager;

    public Authentication authenticate(Authentication authentication) throws Exception {
        log.info("Authetication of user details started.");
        ServletRequestAttributes authContext = ((ServletRequestAttributes) (RequestContextHolder
                .currentRequestAttributes()));

        String username = (String) authContext.getAttribute("username", SCOPE_REQUEST);// authentication.getName();
        String password = (String) authContext.getAttribute("password", SCOPE_REQUEST);// (String) authentication.getCredentials();
        boolean userAttribFetched = ldapManager.authenticateUser(username, password);
        log.info("Authetication Done for the user: " + username);
        if (!userAttribFetched)
            throw new CustomException("Not Authorised", HttpStatus.UNAUTHORIZED);
        return new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
    }

    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
