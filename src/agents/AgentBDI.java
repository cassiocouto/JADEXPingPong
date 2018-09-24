package agents;

import jadex.bridge.IComponentIdentifier;
import util.DirectoryFacilitator;

public class AgentBDI {

	protected DirectoryFacilitator df;
	protected String agentName;
	
	protected String setName(String classname, int id) {
		return classname+"_"+id;
	}

	protected void registerSelf(String name, IComponentIdentifier identifier) {
		df = DirectoryFacilitator.getInstance();
		df.registerAgent(name, identifier);
	}

}
