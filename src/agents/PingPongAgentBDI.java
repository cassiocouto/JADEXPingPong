package agents;

import java.util.HashMap;
import java.util.Map;

import jadex.bdiv3.annotation.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IMessageFeature;
import jadex.bridge.fipa.SFipa;
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
		if (id == 0) {
			IComponentIdentifier id = null;
			do {
				id = df.getAgentAID("1_0");
			} while (id == null);
			Map<String, Object> ping = new HashMap<String, Object>();
			ping.put(SFipa.CONTENT, "PING!");
			ping.put(SFipa.PERFORMATIVE, SFipa.PROPAGATE);
			ping.put(SFipa.RECEIVERS, new IComponentIdentifier[] { id });
			System.out.println(agentName + ": sending ping!");
			agent.getComponentFeature(IMessageFeature.class).sendMessage(ping, SFipa.FIPA_MESSAGE_TYPE);
		}
	}

	@Goal
	public class Communicate {

	}

	@AgentMessageArrived
	public void msgArrived(Map<String, Object> msg, final MessageType mt) {
		String perf = (String) msg.get(SFipa.PERFORMATIVE);
		if ((SFipa.QUERY_IF.equals(perf) || SFipa.QUERY_REF.equals(perf)) && "ping".equals(msg.get(SFipa.CONTENT))) {
			Map<String, Object> reply = mt.createReply(msg);
			reply.put(SFipa.CONTENT, agentName + " is alive");
			reply.put(SFipa.PERFORMATIVE, SFipa.INFORM);
			reply.put(SFipa.SENDER, agent.getComponentIdentifier());
			agent.getComponentFeature(IMessageFeature.class).sendMessage(reply, mt);
		} else {
			agent.getLogger().severe("Could not process message: " + msg);
		}
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
