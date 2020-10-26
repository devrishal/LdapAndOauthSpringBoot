
import java.util.Properties;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class LdapUserServiceImpl {
	private static final Logger log = LoggerFactory.getLogger(LdapUserServiceImpl.class);
	@Value("${ldap.userSearchBase}")
	private String userSearchBase;

	@Value("${ldap.initialContextFactory}")
	private String initialContextFactory;

	@Value("${ldap.providerURL}")
	private String providerURL;

	@Value("${ldap.authMode}")
	private String authMode;

	@Value("${ldap.adminUserName}")
	private String adminUserName;

	@Value("${ldap.adminPassword}")
	private String adminPassword;

	@Value("${ldap.securityProtocol}")
	private String securityProtocol;

	@Value("${ldap.userDn}")
	private String userDn;

	/**
	 * Initialises LDAP context with LDAP configuration from properties.
	 * 
	 * @return
	 * @throws NamingException
	 */
	private DirContext initContext() throws NamingException {
		Properties env = new Properties();
		populateEnviromentProperties(env);
		DirContext ctx = new InitialDirContext(env);
		return ctx;
	}

	/**
	 * populate Enviroment variables to be used by initAdminContext method for
	 * population of ldap Context population.
	 * 
	 * @param env
	 * @throws NamingException
	 */
	private void populateEnviromentProperties(Properties env) throws NamingException {
		env.put(ApplicationProperty.INIT_CTX_FACTORY, initialContextFactory);
		env.put(ApplicationProperty.NAMING_PROVIDER_URL, providerURL);
		// env.put(SbicCommon.SECURITY_AUTHENTICATION, securityProtocol);
		env.put(ApplicationProperty.SECURITY_PRINCIPAL, authMode);
		env.put(ApplicationProperty.SECURITY_PRINCIPAL, adminUserName);
		env.put(ApplicationProperty.SECURITY_CREDENTIALS, adminPassword);
	}

	public boolean authenticateUser(String userid, String sPassword) {
		boolean isAuthenticated = false;
		log.info("Inside authenticateUser Method, for authentication of the LDAP user/password: ");
		String dnString = ApplicationProperty.CN + ApplicationProperty.EQUALS + userid + ApplicationProperty.COMMA
				+ userDn + ApplicationProperty.COMMA + userSearchBase;
		log.info("Populated userDN String: " + dnString);
		DirContext ctx = null;
		try {
			log.info("Before population of initialContext using ldap configuration.");
			ctx = initContext();
			log.info("After population of initialContext using ldap configuration.");
			// loggedinAttrib = ctx.getAttributes(dnString, null);
			isAuthenticated = authenticateUser(userid, sPassword, dnString, ctx);
			log.info("After fetching log in attributes of user. ");

		} catch (NameNotFoundException ee) {
			log.error(ee.getMessage(), ee);
			throw new TurbineApiException("Invalid Credentials.", HttpStatus.UNAUTHORIZED);
		} catch (NamingException e) {
			log.error(e.getMessage(), e);
			throw new TurbineApiException("Invalid Credentials.", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new TurbineApiException(e.getMessage(), HttpStatus.UNAUTHORIZED);
		} finally {
			if (ctx != null)
				try {
					ctx.close();
				} catch (NamingException e) {
					log.error(e.getMessage(), e);
					throw new TurbineApiException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
		}
		return isAuthenticated;

	}

	/**
	 * Authenticate user with given username and password.
	 * 
	 * @param userid
	 * @param sPassword
	 * @param dnString
	 * @param ctx
	 * @return
	 * @throws NamingException
	 */
	private boolean authenticateUser(String userid, String sPassword, String dnString, DirContext ctx)
			throws NamingException, NameNotFoundException, Exception {
		log.info("Before fetching log in attributes of user.");
		String[] attributeFilter = { ApplicationProperty.CN };
		SearchControls sc = new SearchControls();
		sc.setReturningAttributes(attributeFilter);
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String searchFilter = "(" + ApplicationProperty.CN + "=" + userid + ")";
		NamingEnumeration<SearchResult> results = ctx.search(dnString, searchFilter, sc);

		if (results.hasMore()) {
			SearchResult result = results.next();
			String distinguishedName = result.getNameInNamespace();
			Properties authEnv = new Properties();
			populateEnviromentProperties(authEnv);
			authEnv.put(ApplicationProperty.SECURITY_PRINCIPAL, distinguishedName);
			authEnv.put(ApplicationProperty.SECURITY_CREDENTIALS, sPassword);
			log.info("After fetching log in attributes of user. ");
			new InitialDirContext(authEnv);
			return true;
		}
		return false;
	}

}
