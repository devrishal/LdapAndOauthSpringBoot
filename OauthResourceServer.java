import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
/**
* Resource Server responsible for authorising a request. 
* It will requires an access token to allow, or at least consider, access to its resources
*/
@Configuration
@EnableResourceServer
public class OauthResourceServer extends ResourceServerConfigurerAdapter {
  private static final String RESOURCE_ID ="oauth-resource-id";
  
  @Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.resourceId(RESOURCE_ID);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/oauth/token", "/oauth/authorize **", "/publishes").permitAll()
				.anyRequest().authenticated();
	}
}
