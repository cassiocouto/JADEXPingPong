package jadex.micro.testcases.subscriptionlistener;

import java.util.Collection;
import java.util.Map;

import jadex.base.test.TestReport;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.testcases.TestAgent;

/**
 *  Test getting values of a subscription future.
 */
@Agent
public class GetterAgent	extends TestAgent
{
	/** The agent. */
	@Agent
	protected IInternalAccess	agent;
	
	/**
	 *  Perform  the test.
	 *  @param cms	The cms of the platform to test (local or remote).
	 * 	@param local	True when tests runs on local platform. 
	 *  @return	The test result.
	 */
	protected IFuture<TestReport>	test(final IComponentManagementService cms, final boolean local)
	{
		final Future<TestReport>	ret	= new Future<TestReport>();
		cms.getRootIdentifier()
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, TestReport>(ret)
		{
			public void customResultAvailable(IComponentIdentifier root)
			{
				createComponent(ProviderAgent.class.getName()+".class", root, null)
					.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, TestReport>(ret)
				{
					public void customResultAvailable(final IComponentIdentifier provider)
					{
						agent.getComponentFeature(IRequiredServicesFeature.class).searchService(ITestService.class, provider)
							.addResultListener(new ExceptionDelegationResultListener<ITestService, TestReport>(ret)
						{
							public void customResultAvailable(ITestService ts)
							{
								ISubscriptionIntermediateFuture<String>	fut	= ts.test();
								Collection<String>	vals1	= fut.get();
								Collection<String>	vals2	= fut.getIntermediateResults();
								
								final TestReport	tr	= new TestReport(local ? "#1" : "#2", "Test getting values of a "+(local ? "local" : "remote")+" subscription future.");
								if(vals1.toString().equals("[a, b, c]") && vals1.equals(vals2))
								{
									tr.setSucceeded(true);
								}
								else
								{
									tr.setFailed("Wrong vals: "+vals1+", "+vals2);
								}
								
								cms.destroyComponent(provider)
									.addResultListener(new ExceptionDelegationResultListener<Map<String, Object>, TestReport>(ret)
								{
									public void customResultAvailable(Map<String, Object> map)
									{
										ret.setResult(tr);
									}
								});
							}
						});
					}
				});
			}
		});
		
		return ret;
	}
}
