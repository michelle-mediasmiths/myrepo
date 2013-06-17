package com.mediasmiths;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SetupJNDI
{
	private final static Logger logger = Logger.getLogger(SetupJNDI.class);


	public static void setUpJNDI(String jdbcUrl, String user, String password) throws Exception
	{
		//shamelessly copied from https://blogs.oracle.com/randystuph/entry/injecting_jndi_datasources_for_junit

		// rcarver - setup the jndi context and the datasource
		try
		{
			// Create initial context
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
			InitialContext ic = new InitialContext();

			ic.createSubcontext("java:");
			ic.createSubcontext("java:comp");
			ic.createSubcontext("java:comp/env");
			ic.createSubcontext("java:comp/env/jdbc");

			// Construct DataSource
			OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
			ds.setURL(jdbcUrl);
			//ds.setURL("jdbc:oracle:thin:oracle/oracle@192.168.2.62:1521/orcl");
			ds.setUser(user);
			ds.setPassword(password);
			ic.bind("java:comp/env/jdbc/EventingSystem", ds);
		}
		catch (NamingException ex)
		{
			logger.error(ex);
		}
	}
}
