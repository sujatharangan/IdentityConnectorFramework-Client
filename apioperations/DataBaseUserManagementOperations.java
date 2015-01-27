package apioperations;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import util.SearchResultsHandler;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.EmbeddedObject;
import org.identityconnectors.framework.common.objects.EmbeddedObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import util.ICFClientUtil;

public class DataBaseUserManagementOperations {

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: apioperations.DataBaseUserManagementOperations "
					+ "<ICF Bundle Directory Location> <operation> <unique string or uid>");
			System.out.println("Sample: apioperations.DataBaseUserManagementOperations " + "/scratch/icfbundles/dbum/bundle createObject davinci");
		}
		String bundleDirectoryLocation = args[0].trim();

		File bundleDirectory = new File(bundleDirectoryLocation);
		URL connBundleUrl = IOUtil.makeURL(bundleDirectory, "org.identityconnectors.dbum-1.0.1116.jar");
		ConnectorInfoManagerFactory factory = ConnectorInfoManagerFactory.getInstance();
		ConnectorInfoManager manager = factory.getLocalManager(connBundleUrl);

		// Change if using different bundle
		ConnectorKey key = new ConnectorKey("org.identityconnectors.dbum", "1.0.1116", "org.identityconnectors.dbum.DBUMConnector");
		ConnectorInfo ci = manager.findConnectorInfo(key);
		ConnectorFacade connector = ICFClientUtil.createConnectorFacade(ci);

		String operation = args[1].trim();
		String nameOrUid = args[2].trim();

		switch (operation) {
		case "createObject":
			createObject(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "deleteObject":
			deleteObject(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "updateObject":
			updateObject(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "resetPassword":
			resetPassword(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "addAttributes":
			addAttributeValues(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "removeAttributes":
			removeAttributeValues(connector, "__ACCOUNT__", nameOrUid);
			break;
		case "search":
			search(connector, "__ACCOUNT__");
			break;
		default:
			System.out.println("Unsupported operation!");
			System.exit(0);
		}
	}

	/*
	 * Create object
	 */
	public static String createObject(ConnectorFacade connector, String objectClassName, String name) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		if (objectClassName.equals("__ACCOUNT__")) {
			hm.put("__NAME__", name);
			hm.put("__UID__", name);
			hm.put("tempTableSpace", "SYSTEM");
			hm.put("__PASSWORD__", new GuardedString("welcome1".toCharArray()));
		} else {
			System.out.println("Unsupported Objectclass. Exiting.");
			System.exit(0);
		}
		// Get ICF Attribute set for data.
		Set<Attribute> attrs = ICFClientUtil.getAttributeSet(hm);

		Uid uid = connector.create(new ObjectClass(objectClassName), attrs, null);
		System.out.println("============Created object with uid " + uid.getUidValue());

		return uid.getUidValue();
	}

	/*
	 * Delete object
	 */
	public static void deleteObject(ConnectorFacade connector, String objectClassName, String uidValue) {
		connector.delete(new ObjectClass(objectClassName), new Uid(uidValue), null);
		System.out.println("============Deleted object with uid " + uidValue);
	}

	/*
	 * Reset account password
	 */
	public static Uid resetPassword(ConnectorFacade connector, String objectClassName, String uidValue) {
		// Note this password should match the password policies defined on
		// active directory (if any)
		String passwordToUpdate = "We1come!!";
		Set<Attribute> attrs = new HashSet<Attribute>();
		attrs.add(AttributeBuilder.buildPassword(passwordToUpdate.toCharArray()));

		Uid uid = connector.update(new ObjectClass(objectClassName), new Uid(uidValue), attrs, null);
		System.out.println("============Reset Password. Updated uid is " + uid.getUidValue());
		return uid;
	}

	/*
	 * Update object
	 */
	public static void updateObject(ConnectorFacade connector, String objectClassName, String uidValue) {
		HashMap<String, Object> hm = new HashMap<String, Object>();

		if (objectClassName.equals("__ACCOUNT__")) {
			hm.put("tempTableSpace", "SYSTEM");

		} else {
			System.out.println("Unsupported Objectclass. Exiting.");
			System.exit(0);
		}
		// Get ICF Attribute set for data.
		Set<Attribute> attrs = ICFClientUtil.getAttributeSet(hm);
		Uid uid = connector.update(new ObjectClass(objectClassName), new Uid(uidValue), attrs, null);
		System.out.println("=========== Updated object. Updated uid is " + uid.getUidValue());
	}

	// Applies to incremental changes to be added for multivalued attributes and
	// EmbeddedObjects.
	public static void addAttributeValues(ConnectorFacade connector, String objectClassName, String uidValue) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		ObjectClass embeddedObjectClass = new ObjectClass("DBRole");

		HashMap hm1 = new HashMap();
		hm1.put("__NAME__", "CONNECT");
		hm1.put("adminOption", "WITH ADMIN OPTION");
		Set<Attribute> attrs1 = ICFClientUtil.getAttributeSet(hm1);

		EmbeddedObjectBuilder eoBuilder1 = new EmbeddedObjectBuilder();
		eoBuilder1.setObjectClass(embeddedObjectClass);
		eoBuilder1.addAttributes(attrs1);
		EmbeddedObject embeddedObject1 = eoBuilder1.build();

		HashMap hm2 = new HashMap();
		hm2.put("__NAME__", "RESOURCE");
		hm2.put("adminOption", "WITH ADMIN OPTION");
		Set<Attribute> attrs2 = ICFClientUtil.getAttributeSet(hm2);

		EmbeddedObjectBuilder eoBuilder2 = new EmbeddedObjectBuilder();
		eoBuilder2.setObjectClass(embeddedObjectClass);
		eoBuilder2.addAttributes(attrs2);
		EmbeddedObject embeddedObject2 = eoBuilder2.build();

		Set<Attribute> aSet = new HashSet<Attribute>();
		Attribute a1 = AttributeBuilder.build("roles", embeddedObject1);
		Attribute a2 = AttributeBuilder.build("roles", embeddedObject2);
		aSet.add(a1);
		aSet.add(a2);

		Uid uid = connector.addAttributeValues(new ObjectClass(objectClassName), new Uid(uidValue), aSet, null);
		System.out.println("============Updated object by adding attributes . Updated uid is " + uid.getUidValue());
	}

	// Applies to incremental changes to be removed for multivalued attributes
	// and EmbeddedObjects.
	public static void removeAttributeValues(ConnectorFacade connector, String objectClassName, String uidValue) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		ObjectClass embeddedObjectClass = new ObjectClass("DBRole");
		HashMap hm1 = new HashMap();
		hm1.put("__NAME__", "CONNECT");
		hm1.put("adminOption", "WITH ADMIN OPTION");
		Set<Attribute> attrs1 = ICFClientUtil.getAttributeSet(hm1);

		EmbeddedObjectBuilder eoBuilder1 = new EmbeddedObjectBuilder();
		eoBuilder1.setObjectClass(embeddedObjectClass);
		eoBuilder1.addAttributes(attrs1);
		EmbeddedObject embeddedObject1 = eoBuilder1.build();

		HashMap hm2 = new HashMap();
		hm2.put("__NAME__", "RESOURCE");
		hm2.put("adminOption", "WITH ADMIN OPTION");
		Set<Attribute> attrs2 = ICFClientUtil.getAttributeSet(hm2);

		EmbeddedObjectBuilder eoBuilder2 = new EmbeddedObjectBuilder();
		eoBuilder2.setObjectClass(embeddedObjectClass);
		eoBuilder2.addAttributes(attrs2);
		EmbeddedObject embeddedObject2 = eoBuilder2.build();

		Attribute a1 = AttributeBuilder.build("roles", embeddedObject1);
		Attribute a2 = AttributeBuilder.build("roles", embeddedObject2);
		attrs.add(a1);
		attrs.add(a2);

		Uid uid = connector.removeAttributeValues(new ObjectClass(objectClassName), new Uid(uidValue), attrs, null);
		System.out.println("============Updated object by removing attributes . Updated uid is " + uid.getUidValue());
	}

	// Search
	public static void search(ConnectorFacade connector, String objectClassName) {
		OperationOptionsBuilder options = new OperationOptionsBuilder();
		Collection<String> attrNames = new ArrayList<String>();

		Schema schema = connector.schema();
		ObjectClassInfo oc = schema.findObjectClassInfo(objectClassName);
		Set<AttributeInfo> aiSet = oc.getAttributeInfo();

		for (AttributeInfo ai : aiSet) {
			if (!ai.getName().equals(OperationalAttributes.PASSWORD_NAME) && !(ai.getName().equals(OperationalAttributes.ENABLE_NAME))
					&& !(ai.getName().equals(OperationalAttributes.CURRENT_PASSWORD_NAME)))
				attrNames.add(ai.getName());
		}
		options.setAttributesToGet(attrNames);

		connector.search(new ObjectClass(objectClassName), null, new SearchResultsHandler(), options.build());
	}
}
