package main;

import java.util.HashMap;
import java.util.Map;

import agents.PingPongAgentBDI;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;
import jadex.commons.future.ThreadSuspendable;
import util.AgentDeployer;

public class Main {
	public static IExternalAccess platform;
	public static String platformName;
	private static IComponentManagementService cms;

	public static void main(String[] args) {
		String[] defargs = new String[] { "-gui", "false", "-welcome", "true", "-cli", "false", "-printpass", "false",
				"-awareness", "false" };
		IFuture<IExternalAccess> plataform = jadex.base.Starter.createPlatform(defargs);
		ThreadSuspendable sus = new ThreadSuspendable();
		platform = plataform.get(sus);
		cms = SServiceProvider.getService(platform.getServiceProvider(), IComponentManagementService.class,
				RequiredServiceInfo.SCOPE_PLATFORM).get(sus);

		Map<String, Object> agParam = new HashMap<String, Object>();
		agParam.put("type", PingPongAgentBDI.PONG);
		agParam.put("index", 0);
		new AgentDeployer(agParam, "bin/agents/PingPongAgentBDI.class", cms).deploy();
		
		
		agParam.put("type", PingPongAgentBDI.PING);
		agParam.put("index", 1);
		new AgentDeployer(agParam, "bin/agents/PingPongAgentBDI.class", cms).deploy();

	}

}
