package discovery;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionInfo;
import org.identityconnectors.framework.common.objects.Schema;

/*
 * Discovers ICF Connector Bundle.
 * Prints Configuration properties, Schema (with meta data), Object classes defined, supported operations and OperationOptions.
 */
public class RemoteBundleDiscovery {

	public static void main(String[] args) throws Exception {

		if(args.length !=3){
			System.out.println("Usage: discovery.RemoteBundleDiscovery <Host> <Port> <Password>");
			System.out.println("Sample: discovery.RemoteBundleDiscovery 130.25.37.141 8759 somepassword");
		}
		
		String host=args[0].trim();
		int port = Integer.parseInt(args[1].trim());
		GuardedString password = new GuardedString(args[2].trim().toCharArray());
		
		RemoteFrameworkConnectionInfo remoteConnectionInfo = new RemoteFrameworkConnectionInfo(host, port, password);
		//RemoteFrameworkConnectionInfo(java.lang.String host, int port, GuardedString key, boolean useSSL, java.util.List<javax.net.ssl.TrustManager> trustManagers, int timeout)
		
		ConnectorInfoManagerFactory factory = ConnectorInfoManagerFactory.getInstance();
		ConnectorInfoManager manager = factory.getRemoteManager(remoteConnectionInfo);
		
		List <ConnectorInfo> connInfoList = manager.getConnectorInfos();
		
		System.out.println("Number of connectors defined...."+connInfoList.size());
		
		RemoteBundleDiscovery discoveryClient = new RemoteBundleDiscovery();
		for (ConnectorInfo ci : connInfoList)
		{
			discoveryClient.printGeneralConnectorInfo(ci);
			discoveryClient.printConfigurationProperties(ci);
			ConnectorFacade connector = discoveryClient.createConnectorFacade(ci);
			discoveryClient.printSchema(connector);
		}
		System.out.println("----------------------------------");
	}

	public void printGeneralConnectorInfo(ConnectorInfo ci)
	{
		System.out.println("\n============Printing General Connector Info==================");
		System.out.println("Connector Display Name: " +ci.getConnectorDisplayName());
		
		ConnectorKey cKey = ci.getConnectorKey();
		System.out.println("\nBundle Name: " +cKey.getBundleName());
		System.out.println("Bundle Version: " +cKey.getBundleVersion());
		System.out.println("Connector Name: " +cKey.getConnectorName());
		APIConfiguration apiConfig = ci.createDefaultAPIConfiguration();
		System.out.println("\nIs connection pooling supported ? " +apiConfig.isConnectorPoolingSupported());
	}
	
	public static ConnectorFacade createConnectorFacade(ConnectorInfo ci)
	{
		System.out.println("\n============Creating ConnectorFacade ==================");
		
		APIConfiguration apiConfig = ci.createDefaultAPIConfiguration();

		//Set all required properties needed by the connector
		ConfigurationProperties configProps = apiConfig.getConfigurationProperties();		
		
		// IMPORTANT: This section should be updated with the values for the actual target.
		switch(ci.getConnectorDisplayName()) {
		case "Generic Unix Connector":
			configProps.setPropertyValue("host","");
			configProps.setPropertyValue("port", 22);
			configProps.setPropertyValue("loginUser", "");
			configProps.setPropertyValue("loginUserpassword", new GuardedString("".toCharArray()));				
			configProps.setPropertyValue("sudoAuthorization", false);
			//configProps.setPropertyValue("loginShellPrompt", "#");
			configProps.setPropertyValue("commandTimeout", 3600);
			break;
		case "Flat File Connector" :
			configProps.setPropertyValue("schemaFile", "/tmp/flatfiles");
			break;
		case "Database User Management Connector":
			String jdbcUrl = "jdbc:oracle:thin:@"+ ":" + "5521"+ ":" + "sampledb";
			configProps.setPropertyValue("jdbcUrl",jdbcUrl);
			configProps.setPropertyValue("loginPassword",new GuardedString("".toCharArray()));
			configProps.setPropertyValue("loginUser","sys as sysdba");
			configProps.setPropertyValue("dbType","Oracle");
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

		ConnectorFacade connector =   ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
		//connector.validate();
		connector.test();
		System.out.println("Done testing successfully ....");
		return connector;
		
	}
	public void printConfigurationProperties(ConnectorInfo ci)
	{
		System.out.println("\n==============Configuration Properties defined============= \n");
		APIConfiguration apiConfig = ci.createDefaultAPIConfiguration();
		ConfigurationProperties configProps = apiConfig.getConfigurationProperties();
		
		for (String propName : configProps.getPropertyNames())
		{
			ConfigurationProperty cp = configProps.getProperty(propName);
			System.out.println("Property Name:" + cp.getName());
			System.out.println("Is Confidential:" + cp.isConfidential());
			System.out.println("Is Required:" + cp.isRequired());
			System.out.println("Type Name :" + cp.getType().getName());
			String className = cp.getType().getName();
			System.out.println("Type name "+className);
			System.out.println("Value:" + cp.getValue());
			System.out.println("Display Name:" + cp.getDisplayName(cp.getName()));
			System.out.println("Help Message:" + cp.getHelpMessage(cp.getName()));
			System.out.println("Operations for which this property must be specified:" + cp.getOperations());
			System.out.println("ObjectClasses for which this property must be specified:" + cp.getObjectClasses());
			System.out.println("Attributes for which this property must be specified:" + cp.getAttributes());
			System.out.println("----------------------------------------------------------------------------");
		}
	}
	
	public void printSchema(ConnectorFacade connector)
	{
		System.out.println("\n============Printing Schema==================");
		Schema schema = connector.schema();

		System.out.println("==================Printing out the objectclasses and their attributes:=====================");		 
		Set<ObjectClassInfo> objectClassInfoSet = schema.getObjectClassInfo();
		
		 //For each object class 
		 for (ObjectClassInfo oc: objectClassInfoSet) {
			 //Get Basic object class details
			 System.out.println("object class type : "+oc.getType());
			 System.out.println("object class : is containiner? : "+oc.isContainer());
			 System.out.println("object class : is embedded? : "+oc.isEmbedded());
			 
			 //Attributes of the object class
			 Set<AttributeInfo> aiSet = oc.getAttributeInfo();
			 System.out.println("Total number of attributes are "+aiSet.size());
			 for (AttributeInfo ai: aiSet) {
				 System.out.println("\t" + ai.getName() + " ==> " + ai.getType()+ "\t isCreateable "+ai.isCreateable()+"\t isMultiValued "+ai.isMultiValued()+"\t isReadable "+ai.isReadable()+"\t isRequired "+ai.isRequired()+"\t isReturnedByDefault "+ai.isReturnedByDefault()+"\t isUpdateble "+ai.isUpdateable()+"\t objectClassName "+ai.getObjectClassName());
			 }
		 }	
		 
		 //Supported operations for each of  the object class in the schema
		 System.out.println("==================Printing Supported operations for the objectclasses:=====================");		 
		 Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> opObClassSetMap = schema.getSupportedObjectClassesByOperation();
		 
		 Iterator<Class<? extends APIOperation>> it = opObClassSetMap.keySet().iterator();
		 while (it.hasNext()) {
			 Object op = it.next();
			 Set<ObjectClassInfo> ocinfoSet = (Set<ObjectClassInfo>)opObClassSetMap.get(op);
			 //operation name
			 System.out.print(op + " ==> " );
			 
			 //object class set
			 for (ObjectClassInfo oc: ocinfoSet) {
				 System.out.print(oc.getType() + " " );
			 }
			 System.out.println();
		 }
		 
		 System.out.println("==================Printing Supported Options for the objectclasses:=====================");		 
		 
		 Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> opOptionSetMap = schema.getSupportedOptionsByOperation();
			System.out.println("Number of OperationOptionInfo listed in the connector "+opOptionSetMap.size());		 
		 Iterator<Class<? extends APIOperation>> it1 = opOptionSetMap.keySet().iterator();
		 while (it1.hasNext()) {
			 Object op1 = it1.next();
			 Set<OperationOptionInfo> opOptionInfoSet = (Set<OperationOptionInfo>)opOptionSetMap.get(op1);
			 //operation name
			 System.out.print(op1 + " ==> " );
			 
			 //OperationOptionInfo
			 for (OperationOptionInfo oc: opOptionInfoSet) {
				 System.out.print("type=> "+oc.getType() + " name => " +oc.getName());
			 }
			 System.out.println();
		 }
	}
}
