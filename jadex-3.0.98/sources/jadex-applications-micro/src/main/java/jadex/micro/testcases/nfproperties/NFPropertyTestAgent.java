package jadex.micro.testcases.nfproperties;

import jadex.bridge.IInternalAccess;
import jadex.bridge.nonfunctional.SNFPropertyProvider;
import jadex.bridge.nonfunctional.annotation.NFProperties;
import jadex.bridge.nonfunctional.annotation.NFProperty;
import jadex.bridge.service.IService;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

@Agent
@ProvidedServices(@ProvidedService(type=ICoreDependentService.class, implementation=@Implementation(NFPropertyTestService.class)))
@NFProperties(@NFProperty(name="componentcores", value=CoreNumberProperty2.class))
public class NFPropertyTestAgent
{
	@Agent
	protected IInternalAccess agent;
	
	@AgentBody
	public IFuture<Void> body()
	{
		ICoreDependentService cds = SServiceProvider.getService(agent, ICoreDependentService.class).get();
		IService iscds = (IService)cds;
//		INFPropertyProvider prov = (INFPropertyProvider)iscds.getExternalComponentFeature(INFPropertyComponentFeature.class);
		String[] names = SNFPropertyProvider.getNFPropertyNames(agent.getExternalAccess(), iscds.getServiceIdentifier()).get();
		
		System.out.println("Begin list of non-functional properties:");
		for(String name : names)
		{
			System.out.println(name);
		}
		System.out.println("Finished list of non-functional properties.");
		
		System.out.println("Service Value: " + SNFPropertyProvider.getNFPropertyValue(agent.getExternalAccess(), iscds.getServiceIdentifier(), "cores").get());
		
		System.out.println("Component Value, requested from Service: " + SNFPropertyProvider.getNFPropertyValue(agent.getExternalAccess(), iscds.getServiceIdentifier(), "componentcores").get());
//		try
//		{
//			System.out.println("Speed Value for method: " +iscds.getNFPropertyValue(ICoreDependentService.class.getMethod("testMethod", new Class<?>[0]), "methodspeed").get());
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
		
		return IFuture.DONE;
	}
}
