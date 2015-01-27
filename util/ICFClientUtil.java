package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;

public class ICFClientUtil {

	public static Set<Attribute> getAttributeSet(HashMap<String, Object> attrMap) {
		Set<Attribute> attrs = new HashSet<Attribute>();

		if (attrMap == null || attrMap.isEmpty())
			return attrs;

		for (String key : attrMap.keySet()) {
			if (key == null)
				continue;

			if (attrMap.get(key).equals("__PASSWORD__"))
				attrs.add(AttributeBuilder.buildPassword((GuardedString) attrMap.get(key)));
			else
				attrs.add(AttributeBuilder.build(key, attrMap.get(key)));
		}
		return attrs;
	}

	public static ConnectorFacade createConnectorFacade(ConnectorInfo ci) {
		System.out.println("\n============Creating ConnectorFacade ==================");

		APIConfiguration apiConfig = ci.createDefaultAPIConfiguration();

		// Set all required properties needed by the connector
		ConfigurationProperties configProps = apiConfig.getConfigurationProperties();

		// IMPORTANT: This section should be updated with the values for the
		// actual target.
		switch (ci.getConnectorDisplayName()) {
		case "Generic Unix Connector":
			configProps.setPropertyValue("host", "");
			configProps.setPropertyValue("port", 22);
			configProps.setPropertyValue("loginUser", "");
			configProps.setPropertyValue("loginUserpassword", new GuardedString("".toCharArray()));
			configProps.setPropertyValue("sudoAuthorization", false);
			// configProps.setPropertyValue("loginShellPrompt", "#");
			configProps.setPropertyValue("commandTimeout", 3600);
			break;
		case "Flat File Connector":
			configProps.setPropertyValue("schemaFile", "/tmp/flatfiles");
			break;
		case "Database User Management Connector":
			String jdbcUrl = "jdbc:oracle:thin:@" + "abc.com:" + "5521" + ":" + "db";
			configProps.setPropertyValue("jdbcUrl", jdbcUrl);
			configProps.setPropertyValue("loginPassword", new GuardedString("".toCharArray()));
			configProps.setPropertyValue("loginUser", "sys as sysdba");
			configProps.setPropertyValue("dbType", "Oracle");
			break;
		case "Domino Connector":
			configProps.setPropertyValue("registrationServer", "something");
			configProps.setPropertyValue("adminPassword", new GuardedString("changeit".toCharArray()));
			configProps.setPropertyValue("administrationServer", "something");
			configProps.setPropertyValue("userDatabaseName", "something");
			configProps.setPropertyValue("adminName", "something");
			configProps.setPropertyValue("adminIdFile", "something");
			configProps.setPropertyValue("mailFileAction", new Integer(0));
			break;
		case "GoogleApps Connector":
			configProps.setPropertyValue("connectionUrl", "");
			configProps.setPropertyValue("domain", "");
			configProps.setPropertyValue("login", "");
			configProps.setPropertyValue("password", new GuardedString("".toCharArray()));
			configProps.setPropertyValue("proxyHost", "");
			configProps.setPropertyValue("proxyPort", 80);
			break;
		case "Database Table Connector":
			configProps.setPropertyValue("host", "");
			configProps.setPropertyValue("port", "5521");
			configProps.setPropertyValue("user", "");
			configProps.setPropertyValue("password", new GuardedString("".toCharArray()));
			configProps.setPropertyValue("database", "");
			configProps.setPropertyValue("table", "");
			configProps.setPropertyValue("keyColumn", "");
			break;
		case "Windows Active Directory Connector":
			configProps.setPropertyValue("DirectoryAdminName", "");
			configProps.setPropertyValue("DirectoryAdminPassword", new GuardedString("".toCharArray()));
			configProps.setPropertyValue("Container", "");
			configProps.setPropertyValue("DomainName", "");
			break;
		}

		ConnectorFacade connector = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
		// connector.validate();
		connector.test();
		System.out.println("Done testing successfully ....");
		return connector;
	}

}
