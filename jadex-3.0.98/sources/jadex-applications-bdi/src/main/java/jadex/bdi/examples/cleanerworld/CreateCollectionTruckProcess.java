package jadex.bdi.examples.cleanerworld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IMonitoringComponentFeature;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.monitoring.IMonitoringEvent;
import jadex.bridge.service.types.monitoring.IMonitoringService.PublishEventLevel;
import jadex.commons.SimplePropertyObject;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.commons.gui.future.SwingIntermediateResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.ISpaceProcess;

/**
 *  Process responsible for creating truck agents in response to full wastebins.
 */
public class CreateCollectionTruckProcess extends SimplePropertyObject implements ISpaceProcess
{
	protected Set<ISpaceObject> ongoing = new HashSet<ISpaceObject>();
	
	//-------- ISpaceProcess interface --------
	
	/**
	 *  This method will be executed by the object before the process gets added
	 *  to the execution queue.
	 *  @param clock	The clock.
	 *  @param space	The space this process is running in.
	 */
	public void start(IClockService clock, IEnvironmentSpace space)
	{
//		System.out.println("create waste process started.");
	}

	/**
	 *  This method will be executed by the object before the process is removed
	 *  from the execution queue.
	 *  @param clock	The clock.
	 *  @param space	The space this process is running in.
	 */
	public void shutdown(IEnvironmentSpace space)
	{
//		System.out.println("create waste process shutdowned.");
	}

	/**
	 *  Executes the environment process
	 *  @param clock	The clock.
	 *  @param space	The space this process is running in.
	 */
	public void execute(IClockService clock, final IEnvironmentSpace space)
	{
		ISpaceObject[] wastebins = space.getSpaceObjectsByType("wastebin");
		
		if(wastebins.length>0)
		{
			final Set<ISpaceObject> todo = new HashSet<ISpaceObject>();
			
			for(int i=0; i<wastebins.length; i++)
			{
				if(((Boolean)wastebins[i].getProperty("full")).booleanValue()
					&& !ongoing.contains(wastebins[i]))
				{
					todo.add(wastebins[i]);
				}
			}
		
			if(todo.size()>0)
			{
//				System.out.println("Creating garbage collection truck.");
//				final IApplication app = space.getContext();
				final Map<String, Object> params = new HashMap<String, Object>();
				params.put("wastebins", todo.toArray());
				ongoing.addAll(todo);
				SServiceProvider.getService(space.getExternalAccess(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
					.addResultListener(new DefaultResultListener<IComponentManagementService>()
				{
					public void resultAvailable(final IComponentManagementService cms)
					{
						IFuture<IComponentIdentifier> ret = cms.createComponent(null, "Truck",
							new CreationInfo(null, params, space.getExternalAccess().getComponentIdentifier(), null, null, null, null, null, null, null, space.getExternalAccess().getModel().getAllImports(), null, null), null);
						
						IResultListener<IComponentIdentifier> lis = new IResultListener<IComponentIdentifier>()
						{
							public void exceptionOccurred(Exception exception)
							{
							}
							public void resultAvailable(IComponentIdentifier truck)
							{
								cms.getExternalAccess(truck).addResultListener(new IResultListener<IExternalAccess>()
								{
									public void exceptionOccurred(Exception exception)
									{
									}
									public void resultAvailable(IExternalAccess ex)
									{
										ex.scheduleStep(new IComponentStep<Void>()
										{
											@Classname("rem")
											public IFuture<Void> execute(IInternalAccess ia)
											{
//												IBDIInternalAccess bia = (IBDIInternalAccess)ia;
//												bia.addComponentListener(new TerminationAdapter()
//												{
//													public void componentTerminated()
//													{
//														ongoing.removeAll(todo);
//													}
//												});
												
												ia.getComponentFeature(IMonitoringComponentFeature.class).subscribeToEvents(IMonitoringEvent.TERMINATION_FILTER, false, PublishEventLevel.COARSE)
													.addResultListener(new SwingIntermediateResultListener<IMonitoringEvent>(new IntermediateDefaultResultListener<IMonitoringEvent>()
												{
													public void intermediateResultAvailable(IMonitoringEvent result)
													{
														ongoing.removeAll(todo);
													}
												}));
												return IFuture.DONE;
											}
										});
//										ex.addAgentListener(new IAgentListener()
//										{
//											public void agentTerminated(AgentEvent ae)
//											{
//												ongoing.removeAll(todo);
//											}
//											public void agentTerminating(AgentEvent ae)
//											{
//											}
//										});
									}
								});
							}
						};
						ret.addResultListener(lis);
					}
				});
			}
		}
	}
	
	/**
	 *  Executes the environment process
	 *  @param clock	The clock.
	 *  @param space	The space this process is running in.
	 * /
	public void execute(IClockService clock, IEnvironmentSpace space)
	{
		ISpaceObject[] trucks = space.getSpaceObjectsByType("truck");
		if(trucks.length==0)
		{
			ISpaceObject[] wastebins = space.getSpaceObjectsByType("wastebin");
			
			if(wastebins.length>0)
			{
				boolean full = true;
				
				for(int i=0; full && i<wastebins.length; i++)
				{
					if(!((Boolean)wastebins[i].getProperty("full")).booleanValue())
						full = false;
				}
			
				if(full)
				{
					System.out.println("Creating garbage collection truck.");
					IApplicationContext app = (IApplicationContext)space.getContext();
					Map params = new HashMap();
					params.put("wastebins", wastebins);
					app.createAgent(null, "Truck", null, params, true, false, null, null);
				}
			}
		}
	}*/
}
