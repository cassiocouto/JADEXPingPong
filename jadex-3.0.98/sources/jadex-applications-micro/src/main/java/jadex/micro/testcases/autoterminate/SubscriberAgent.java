package jadex.micro.testcases.autoterminate;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.IResultCommand;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Configuration;
import jadex.micro.annotation.Configurations;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  Agent that subscribes to the service and kills itself or its platform.
 */
@Agent
@Configurations({
	@Configuration(name="self"),
	@Configuration(name="platform")})
@RequiredServices({
	@RequiredService(name="sub", type=IAutoTerminateService.class,
		binding=@Binding(scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="cms", type=IComponentManagementService.class,
		binding=@Binding(scope=Binding.SCOPE_PLATFORM))})
public class SubscriberAgent
{
	//-------- attributes --------
	
	/** The agent. */
	@Agent
	protected IInternalAccess agent;
	
//	/** The service. */
//	@AgentService
//	protected IAutoTerminateService	sub;

	/** The cms. */
	@AgentService
	protected IComponentManagementService	cms;
	
	//-------- methods --------
	
	/**
	 *  The agent body.
	 */
	@AgentBody
	public void	body(final IInternalAccess agent)
	{
//		System.out.println("subscribe "+agent.getComponentIdentifier()+", "+agent.getConfiguration());
		
		SServiceProvider.waitForService(agent, new IResultCommand<IFuture<IAutoTerminateService>, Void>()
		{
			public IFuture<IAutoTerminateService> execute(Void args)
			{
				return SServiceProvider.getService(agent, IAutoTerminateService.class, RequiredServiceInfo.SCOPE_GLOBAL);
			}
		}, 3, 2000).addResultListener(new IResultListener<IAutoTerminateService>()
		{
			public void exceptionOccurred(Exception exception)
			{
				throw new RuntimeException(exception);
			}
			
			public void resultAvailable(IAutoTerminateService sub)
			{
				sub.subscribe().addResultListener(new IntermediateDefaultResultListener<String>()
				{
					public void intermediateResultAvailable(String result)
					{
//						System.out.println("subscribed "+agent.getComponentIdentifier());
						
						if("platform".equals(agent.getConfiguration()))
						{
//							System.out.println("destroy platform: "+agent.getComponentIdentifier().getRoot());
							cms.destroyComponent(agent.getComponentIdentifier().getRoot());
						}
						else
						{
//							System.out.println("destroy comp: "+agent.getComponentIdentifier());
							agent.killComponent();
						}
					}
				});
			}
		});
		
		
	}
}
