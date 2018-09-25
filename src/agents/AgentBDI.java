package agents;

import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentCreated;

@Agent
public class AgentBDI {

	@AgentCreated
	public void created() {
		System.out.println("hello");
	}

}
