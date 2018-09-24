package util;

import java.util.HashMap;
import jadex.bridge.IComponentIdentifier;

public class DirectoryFacilitator {
	private HashMap<String, IComponentIdentifier> yellow_pages;
	private static DirectoryFacilitator this_instance;

	private DirectoryFacilitator() {
		yellow_pages = new HashMap<String, IComponentIdentifier>();
	}

	public static DirectoryFacilitator getInstance() {
		if (this_instance == null) {
			this_instance = new DirectoryFacilitator();
		}
		return this_instance;
	}

	public synchronized void registerAgent(String agentName, IComponentIdentifier AID) {
		this_instance.yellow_pages.put(agentName, AID);
		System.out.println(String.format("%s just registered itself! Now the directory has %d entries", agentName,
				this_instance.yellow_pages.keySet().size()));
	}

	public IComponentIdentifier getAgentAID(String agentName) {
		return this_instance.yellow_pages.get(agentName);
	}
}
