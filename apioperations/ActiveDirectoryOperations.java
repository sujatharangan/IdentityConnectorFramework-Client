package apioperations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import util.SearchResultsHandler;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import util.ICFClientUtil;

public class ActiveDirectoryOperations {

	public static void main(String[] args) {

		if (args.length != 6) {
			System.out
					.println("Usage: apioperations.ActiveDirectoryOperations <Host> <Port> <Password> <operation> <object class name> <unique string or uid>");
			System.out.println("Sample: apioperations.ActiveDirectoryOperations 130.25.37.141 8759 somepassword createObject __ACCOUNT__ davinci");
		}
		String host = args[0].trim();
		int port = Integer.parseInt(args[1].trim());
		GuardedString password = new GuardedString(args[2].trim().toCharArray());
		String operation = args[3].trim();
		String objectClassName = args[4].trim();
		String nameOrUid = args[5].trim();

		RemoteFrameworkConnectionInfo remoteConnectionInfo = new RemoteFrameworkConnectionInfo(host, port, password);
		ConnectorInfoManagerFactory factory = ConnectorInfoManagerFactory.getInstance();

		ConnectorInfoManager manager = factory.getRemoteManager(remoteConnectionInfo);

		// IMPORTANT : Change if using a different ICF Bundle
		ConnectorKey key = new ConnectorKey("ActiveDirectory.Connector", "1.1.0.6380",
				"Org.IdentityConnectors.ActiveDirectory.ActiveDirectoryConnector");
		ConnectorInfo ci = manager.findConnectorInfo(key);
		ConnectorFacade connector = ICFClientUtil.createConnectorFacade(ci);

		switch (operation) {
		case "createObject":
			createObject(connector, objectClassName, nameOrUid);
			break;
		case "deleteObject":
			deleteObject(connector, objectClassName, nameOrUid);
			break;
		case "updateObject":
			updateObject(connector, objectClassName, nameOrUid);
			break;
		case "disableAccount":
			disableEnableAccount(connector, nameOrUid, true);
			break;
		case "enableAccount":
			disableEnableAccount(connector, nameOrUid, false);
			break;
		case "resetPassword":
			resetPassword(connector, objectClassName, nameOrUid);
			break;
		case "addAttributes":
			addAttributeValues(connector, objectClassName, nameOrUid);
			break;
		case "removeAttributes":
			removeAttributeValues(connector, objectClassName, nameOrUid);
			break;
		case "search":
			search(connector, objectClassName);
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
			hm.put("__NAME__", "cn=" + name + ",ou=abc,DC=adlrg,DC=us,DC=def,DC=com");
			hm.put("sAMAccountName", name);
			hm.put("givenName", name + "_First");
			hm.put("sn", name + "_Last");
			hm.put("__PASSWORD__", new GuardedString("Welcome1234".toCharArray()));
			hm.put("department", "SomeDept");
			hm.put("company", "def");
			hm.put("division", "Sales");
			hm.put("displayName", name + "First " + name + "Last");
			hm.put("initials", "ABC");
			hm.put("title", "Mr");
			hm.put("ReconnectionAction", new Integer(0));
			hm.put("PasswordNeverExpires", Boolean.TRUE);
		} else if (objectClassName.equals("__GROUP__")) {
			hm.put("__NAME__", "cn=" + name + ",ou=org,DC=adlrg,DC=us,DC=def,DC=com");
			hm.put("description", name + " description");
			hm.put("displayName", name + "  display");
			hm.put("sAMAccountName", name);
		} else if (objectClassName.equals("organizationalUnit")) {
			hm.put("__NAME__", "ou=" + name + ",ou=org,DC=adlrg,DC=us,DC=def,DC=com");
			hm.put("displayName", name + " Organization");
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
	 * Disable / Enable account
	 */
	public static void disableEnableAccount(ConnectorFacade connector, String uidValue, boolean disable) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		attrs.add(AttributeBuilder.buildEnabled(disable));

		Uid uid = connector.update(new ObjectClass("__ACCOUNT__"), new Uid(uidValue), attrs, null);
		System.out.println("============Disabled/Enabled account. Updated uid is " + uid.getUidValue());
	}

	/*
	 * Update object
	 */
	public static void updateObject(ConnectorFacade connector, String objectClassName, String uidValue) {
		HashMap<String, Object> hm = new HashMap<String, Object>();

		if (objectClassName.equals("__ACCOUNT__")) {
			hm.put("department", "Updated value");
			hm.put("title", "Ms");
		} else if (objectClassName.equals("__GROUP__")) {
			hm.put("description", "Updated value");
			hm.put("displayName", "Updated value");
		} else if (objectClassName.equals("organizationalUnit")) {
			hm.put("displayName", "Updated value");
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
		if (objectClassName.equals("__ACCOUNT__")) {
			ArrayList<String> mvaPhoneValues = new ArrayList<String>();
			mvaPhoneValues.add("4-444");
			mvaPhoneValues.add("5-5555");
			attrs.add(AttributeBuilder.build("otherHomePhone", mvaPhoneValues));
		} else {
			// If there are multi valued /embedded objects in other
			// objectclasses this block can be implemented.
			System.out.println("Unsupported objectclass. exiting...");
			System.exit(0);
		}
		Uid uid = connector.addAttributeValues(new ObjectClass(objectClassName), new Uid(uidValue), attrs, null);
		System.out.println("============Updated object by adding attributes . Updated uid is " + uid.getUidValue());
	}

	// Applies to incremental changes to be removed for multivalued attributes
	// and EmbeddedObjects.
	public static void removeAttributeValues(ConnectorFacade connector, String objectClassName, String uidValue) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		if (objectClassName.equals("__ACCOUNT__")) {
			ArrayList<String> mvaPhoneValues = new ArrayList<String>();
			mvaPhoneValues.add("4-444");
			mvaPhoneValues.add("5-5555");
			attrs.add(AttributeBuilder.build("otherHomePhone", mvaPhoneValues));
		} else {
			// If there are multi valued /embedded objects in other
			// objectclasses this block can be implemented.
			System.out.println("Unsupported objectclass. exiting...");
			System.exit(0);
		}
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
