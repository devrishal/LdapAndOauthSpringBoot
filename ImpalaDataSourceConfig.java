import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.cloudera.impala.jdbc41.DataSource;
import com.zaxxer.hikari.HikariDataSource;

@org.springframework.context.annotation.Configuration
public class ImpalaDataSourceConfig {

	@Value("${impala.trust-store.key}")
	private String trustStore;
	@Value("${impala.krb.file}")
	private String krbFile;
	@Value("${impala.jaas.file}")
	private String jassFile;
	@Value("${impala.connection.url}")
	private String jdbcURL;
	@Value("${impala.jdbc.driver}")
	private String jdbcDriver;

	@Value("${impala.maxPoolSize}")
	private String connectionPoolSize;
	@Value("${impala.minimumIdleTime}")
	private String minimumIdleTime;
	@Value("${impala.connectionTimeOut}")
	private String connectionTimeOut;
	@Value("${impala.idleTimeOut}")
	private String idleTimeOut;
	@Value("${impala.spring.fqdn}")
	private String krb_kdc;
	@Value("${impala.domain}")
	private String impalaDomain;

	@Bean
	public HikariDataSource impalaDS() throws SQLException, PropertyVetoException {

		// System.setProperty("sun.security.krb5.debug", "true");
		// System.setProperty("sun.security.jgss.debug", "true");
		System.setProperty("java.security.krb5.realm", impalaDomain);
		System.setProperty("java.security.krb5.kdc", krb_kdc);
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
		System.setProperty("java.security.krb5.conf", krbFile);
		System.setProperty("java.security.auth.login.config", jassFile);
		System.setProperty("sun.security.provider.certpath", trustStore);

		Configuration conf = new Configuration();
		conf.set("hadoop.security.authentication", "kerberos");
		UserGroupInformation.setConfiguration(conf);

		DataSource impalaDS = new DataSource();
		impalaDS.setURL(jdbcURL);

		HikariDataSource hikariDS = new HikariDataSource();
		hikariDS.setDataSource(impalaDS);

		hikariDS.setMaximumPoolSize(Integer.valueOf(connectionPoolSize));
		hikariDS.setMinimumIdle(Integer.valueOf(minimumIdleTime));
		hikariDS.setAutoCommit(true);
		hikariDS.setConnectionTimeout(Integer.valueOf(connectionTimeOut));
		hikariDS.setIdleTimeout(Integer.valueOf(idleTimeOut));

		return hikariDS;
	}
}
