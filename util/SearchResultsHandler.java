package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

public class SearchResultsHandler implements ResultsHandler {

	private List<ConnectorObject> coList = new ArrayList<ConnectorObject>();

	@Override
	public boolean handle(ConnectorObject co) {
		System.out.println("-----Handling: " + co.getUid() + " " + co.getName() + "--------");
		Set<Attribute> attrs = co.getAttributes();

		for (Attribute a : attrs) {
			System.out.println("\t" + a.getName() + " ==> " + a.getValue());
		}

		System.out.println("--------------------------------------------");
		coList.add(co);
		return true;
	}

}
