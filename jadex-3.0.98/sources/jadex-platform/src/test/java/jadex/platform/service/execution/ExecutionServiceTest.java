package jadex.platform.service.execution;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.execution.IExecutionService;
import jadex.commons.concurrent.IExecutable;

public class ExecutionServiceTest
{
	@Test
	public void	testSimpleExecution()
	{
		IExternalAccess	platform	= Starter.createPlatform(PlatformConfiguration.getMinimal()).get();
		IExecutionService	exe	= SServiceProvider.getService(platform, IExecutionService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		
		final List<String>	list	= new ArrayList<String>();		
		exe.execute(new IExecutable()
		{
			@Override
			public boolean execute()
			{
				list.add("executed");
				return false;
			}
		});

		exe.getNextIdleFuture().get();

		Assert.assertEquals(list.toString(), 1, list.size());
		
		platform.killComponent().get();
	}
}
