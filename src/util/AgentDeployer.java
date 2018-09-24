package util;

import java.util.Map;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.ITuple2Future;

public class AgentDeployer {

	private Map<String, Object> param;
	private String agentPath = "";
	private IComponentManagementService cms;

	public AgentDeployer(Map<String, Object> param, String agentPath, IComponentManagementService cms) {
		this.param = param;
		this.agentPath = agentPath;
		this.cms = cms;
	}

	public ITuple2Future<IComponentIdentifier, Map<String, Object>> deploy() {
		ITuple2Future<IComponentIdentifier, Map<String, Object>> agent = cms.createComponent(agentPath,
				new CreationInfo(param));
		Exception e = agent.getException();
		if(e != null) {
			e.printStackTrace();
		}
		return agent;
	}
}
