package agents;

import java.util.Map;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.*;
import jadex.bridge.service.types.message.MessageType;
import jadex.micro.annotation.*;

@Agent
// @Arguments({ @Argument(name = "type", clazz = Integer.class), @Argument(name = "index", clazz = Integer.class) })
public class PingPongAgentBDI extends AgentBDI {
	public static final int PING = 0;
	public static final int PONG = 1;

	@Agent
	BDIAgent agent;
	@Belief
	private int type;
	@Belief
	private int id;

	/*@AgentCreated
	public void created() {
		id = (Integer) agent.getArgument("index");
		type = (Integer) agent.getArgument("type");
		agentName = setName(this.getClass().getName(), id);
		this.registerSelf(agentName, agent.getComponentIdentifier());
	}*/

	@Goal
	public class Communicate {

	}

	@AgentMessageArrived
	public void msgArrived(Map<String, Object> msg, final MessageType mt) {

	}

	@Plan(trigger = @Trigger(goals = (Communicate.class)))
	public void sendMsg() {
	}
}
