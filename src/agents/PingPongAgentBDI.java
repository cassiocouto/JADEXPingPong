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
	protected static int counter = 0;

	@AgentArgument
	private int type;
	@AgentArgument
	private int index;
	@AgentFeature
	private IBDIAgentFeature bdiagent;
	@Agent
	private IInternalAccess agent;

	@AgentCreated
	public void created() {
		agentName = createName(this.getClass().getName(), index);
		registerSelf(agentName, agent.getComponentIdentifier());
		if (index == 0) {
			IComponentIdentifier id = null;
			do {
				id = df.getAgentAID(createName(this.getClass().getName(), 1));
			} while (id == null);
			Map<String, Object> ping = new HashMap<String, Object>();
			ping.put(SFipa.CONTENT, "PING!");
			ping.put(SFipa.PERFORMATIVE, SFipa.PROPAGATE);
			ping.put(SFipa.RECEIVERS, new IComponentIdentifier[] { id });
			ping.put(SFipa.SENDER, id);
			System.out.println(agentName + ": sending ping!");
			agent.getComponentFeature(IMessageFeature.class).sendMessage(ping, SFipa.FIPA_MESSAGE_TYPE);
		}
	}

	@Goal
	public class Communicate {

	}

	@AgentMessageArrived
	private void messageArrived(Map<String, Object> msg, MessageType mt) {

		if(counter > 30) {
			System.out.println("Stopping "+counter);
			return;
		}
		
		System.out.println(agentName + ": I received \"" + msg.get(SFipa.CONTENT) + "\" from " + msg.get(SFipa.SENDER));
		Map<String, Object> reply = mt.createReply(msg);
		reply.put(SFipa.CONTENT, agentName + " is alive "+ (counter++));
		reply.put(SFipa.PERFORMATIVE, SFipa.INFORM);
		reply.put(SFipa.SENDER, agent.getComponentIdentifier());
		agent.getComponentFeature(IMessageFeature.class).sendMessage(reply, mt);
		return;
	}

	@Plan(trigger = @Trigger(goals = (Communicate.class)))
	public void sendMsg() {

	}

	protected String createName(String classname, int id) {
		return classname + "_" + id;
	}

	protected void registerSelf(String name, IComponentIdentifier identifier) {
		df = DirectoryFacilitator.getInstance();
		df.registerAgent(name, identifier);
	}
}
