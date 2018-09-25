package jadex.micro.testcases.subscriptionlistener;

import jadex.bridge.service.annotation.Service;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.micro.annotation.Agent;

@Agent(autoprovide=true)
@Service
public class ProviderAgent implements ITestService
{
	/**
	 * 
	 */
	public ISubscriptionIntermediateFuture<String> test()
	{
		SubscriptionIntermediateFuture<String> ret = new SubscriptionIntermediateFuture<String>();

		ret.addIntermediateResult("a");
		ret.addIntermediateResult("b");
		ret.addIntermediateResult("c");
		ret.setFinished();
		
		return ret;
	}
}
