package jadex.bdiv3.testcases.beliefs;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IArgumentsResultsFeature;
import jadex.bridge.component.IExecutionFeature;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentKilled;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

/**
 *  Test beliefs with update rate.
 */
@Agent
@Results(@Result(name="testresults", clazz=Testcase.class))
public class UpdaterateGetterSetterBDI
{
	//-------- attributes --------
	
	/** The test report. */
	protected TestReport	tr	= new TestReport("#1", "Test if getter/setter belief with updaterate is updated.");
	
	/** The counter. */
	protected int	cnt;
	
	/** The agent. */
	@Agent
	protected IInternalAccess	agent;
	
	//-------- constructors --------
	
	/**
	 *  The agent body.
	 */
	@AgentBody
	public void	body(IInternalAccess agent)
	{
		agent.getComponentFeature(IExecutionFeature.class).waitForDelay(1000).get();
		tr.setFailed("Plan was not triggered.");
		agent.killComponent();
	}

	@Belief(updaterate=100)
	public void setCntBel(int cnt) {

	}

	@Belief(updaterate=100)
	public int getCntBel() {
		cnt++;
		return cnt;
	}
	
	/**
	 *  Called when agent is killed.
	 */
	@AgentKilled
	public void	destroy(IInternalAccess agent)
	{
		agent.getComponentFeature(IArgumentsResultsFeature.class).getResults().put("testresults", new Testcase(1, new TestReport[]{tr}));
	}
	
	//-------- plans --------
	
	/**
	 *  Plan that is triggered when fact changes.
	 */
	@Plan(trigger=@Trigger(factchangeds={"cntBel"}))
	public void	beliefChanged(int cntevt)
	{
		if(cnt==5 && cntevt==5)
		{
			tr.setSucceeded(true);
		}
		else if(cnt!=cntevt)
		{
			tr.setFailed("Inconsistent values: "+cnt+", "+cntevt);
		}
		
		if(tr.isFinished())
		{
			agent.killComponent();
		}
	}
}
