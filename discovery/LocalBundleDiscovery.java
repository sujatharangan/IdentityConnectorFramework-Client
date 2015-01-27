package discovery;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.IOUtil;
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
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionInfo;
import org.identityconnectors.framework.common.objects.Schema;

import util.ICFClientUtil;

/*
 * Discovers ICF Connector Bundle.
 * Prints Configuration properties, Schema (with meta data), Object classes defined, supported operations and OperationOptions.
 */
public class LocalBundleDiscovery {

	public static void main(String[] args) throws Exception {

		if(args.length !=2){
			System.out.println("Usage: discovery.LocalBundleDiscovery <ICF Bundle Directory Location> <ICF Bundle Name>");
			System.out.println("Sample: discovery.LocalBundleDiscovery /scratch/icfbundles/dbum/bundle org.identityconnectors.dbum-1.0.1116.jar");
		}
		String bundleDirectoryLocation = args[0].trim();
		String bundleName= args[1].trim();
		
		File bundleDirectory = new File(bundleDirectoryLocation);
		URL connBundleUrl = IOUtil.makeURL(bundleDirectory,bundleName);

		ConnectorInfoManagerFactory factory = ConnectorInfoManagerFactory.getInstance();
		ConnectorInfoManager manager = factory.getLocalManager(connBundleUrl);
		List <ConnectorInfo> connInfoList = manager.getConnectorInfos();
		System.out.println("Number of Connectors defined in the bundle...."+connInfoList.size());
		LocalBundleDiscovery discoveryClient = new LocalBundleDiscovery();

		for (ConnectorInfo ci : connInfoList)
		{
			discoveryClient.printGeneralConnectorInfo(ci);
			discoveryClient.printConfigurationProperties(ci);
			ConnectorFacade connector = ICFClientUtil.createConnectorFacade(ci);
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
