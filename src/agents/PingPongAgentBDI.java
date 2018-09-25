package agents;

import java.util.Map;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.component.IMessageFeature;
import jadex.bridge.service.types.message.MessageType;
import jadex.micro.annotation.*;
import util.DirectoryFacilitator;

@Agent
@Arguments({ @Argument(name = "type", clazz = Integer.class), @Argument(name = "index", clazz = Integer.class) })
public class PingPongAgentBDI extends AgentBDI {
	public static final int PING = 0;
	public static final int PONG = 1;
	protected DirectoryFacilitator df;
	protected String agentName;

	@AgentArgument
	private int type;
	@AgentArgument
	private int id;
	@AgentFeature
	private IBDIAgentFeature bdiagent;
	@Agent
	private IInternalAccess agent;

	@AgentCreated
	public void created() {
		String agentName = type + "_" + id;
		registerSelf(agentName, agent.getComponentIdentifier());
		if(id == 0) {
			IComponentIdentifier id = df.getAgentAID("1_0");
			agent.
		}
	}

	@Goal
	public class Communicate {

	}

	@AgentMessageArrived
	public void msgArrived(Map<String, Object> msg, final MessageType mt) {

	}

	@Plan(trigger = @Trigger(goals = (Communicate.class)))
	public void sendMsg() {
	}

	protected String setName(String classname, int id) {
		return classname + "_" + id;
	}

	protected void registerSelf(String name, IComponentIdentifier identifier) {
		df = DirectoryFacilitator.getInstance();
		df.registerAgent(name, identifier);
	}
}
