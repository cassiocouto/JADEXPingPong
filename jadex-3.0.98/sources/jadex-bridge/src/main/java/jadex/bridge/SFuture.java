package jadex.bridge;

import jadex.base.Starter;
import jadex.bridge.component.IExecutionFeature;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.future.Future;
import jadex.commons.future.IForwardCommandFuture;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IPullIntermediateFuture;
import jadex.commons.future.IPullSubscriptionIntermediateFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.ITerminableFuture;
import jadex.commons.future.ITerminableIntermediateFuture;
import jadex.commons.future.ITuple2Future;
import jadex.commons.future.IntermediateFuture;
import jadex.commons.future.PullIntermediateDelegationFuture;
import jadex.commons.future.PullSubscriptionIntermediateDelegationFuture;
import jadex.commons.future.SubscriptionIntermediateDelegationFuture;
import jadex.commons.future.TerminableDelegationFuture;
import jadex.commons.future.TerminableIntermediateDelegationFuture;
import jadex.commons.future.Tuple2Future;

/**
 *  Helper class for future aspects.
 */
public class SFuture
{
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that time span.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ia The component handling the service call (on that component the periodic updates are scheduled).
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IInternalAccess ia)
	{
		ServiceCall sc = ServiceCall.getCurrentInvocation();
		long to = sc!=null? sc.getTimeout(): Starter.getLocalDefaultTimeout(ia.getComponentIdentifier()); // Hack!!! find out in which cases service call can null
		boolean	realtime	= sc!=null ? sc.getRealtime()!=null ? sc.getRealtime().booleanValue() : false : false;
	//	boolean local = sc.getCaller().getPlatformName().equals(agent.getComponentIdentifier().getPlatformName());
	//	long to = sc.getTimeout()>0? sc.getTimeout(): (local? BasicService.DEFAULT_LOCAL: BasicService.DEFAULT_REMOTE);
	//	to = 5000;
		avoidCallTimeouts(ret, ia, to, realtime);
	}
	
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that timespan.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ea The component handling the service call (on that component the periodic updates are scheduled).
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IExternalAccess ea)
	{
		ServiceCall sc = ServiceCall.getCurrentInvocation();
		long to = sc!=null? sc.getTimeout(): Starter.getLocalDefaultTimeout(ea.getComponentIdentifier()); // Hack!!! find out in which cases service call can null
		boolean	realtime	= sc!=null ? sc.getRealtime()!=null ? sc.getRealtime().booleanValue() : false : false;
	//	boolean local = sc.getCaller().getPlatformName().equals(agent.getComponentIdentifier().getPlatformName());
	//	long to = sc.getTimeout()>0? sc.getTimeout(): (local? BasicService.DEFAULT_LOCAL: BasicService.DEFAULT_REMOTE);
	//	to = 5000;
		avoidCallTimeouts(ret, ea, to, realtime);
	}
		
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that timespan.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ia The component handling the service call (on that component the periodic updates are scheduled).
	 *  @param to The timeout.
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IInternalAccess ia, long to, boolean realtime)
	{
		avoidCallTimeouts(ret, ia, to, 0.8, realtime);
	}
	
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that timespan.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ea The component handling the service call (on that component the periodic updates are scheduled).
	 *  @param to The timeout.
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IExternalAccess ea, long to, boolean realtime)
	{
		avoidCallTimeouts(ret, ea, to, 0.8, realtime);
	}
	
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that timespan.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ia The component handling the service call (on that component the periodic updates are scheduled).
	 *  @param to The timeout.
	 *  @param factor (default 0.8) Used to update the timer when factor*to has elapsed.
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IInternalAccess ia, long to, double factor, final boolean realtime)
	{
		if(to>0)
		{
			final long w = (long)(to*factor);
			IComponentStep<Void> step = new ImmediateComponentStep<Void>()
			{
				public IFuture<Void> execute(IInternalAccess ia)
				{
					if(!ret.isDone())
					{
						ret.sendForwardCommand(IForwardCommandFuture.Type.UPDATETIMER);
						ia.getComponentFeature(IExecutionFeature.class).waitForDelay(w, this, realtime);
					}
					return IFuture.DONE;
				}
			};
			ia.getComponentFeature(IExecutionFeature.class).waitForDelay(w, step, realtime);
		}
	}
	
	/**
	 *  Automatically update the timer of a long running service call future.
	 *  Ensures that the caller does not timeout even if no result
	 *  value is set in that timespan.
	 *  The call periodically sends alive calls to the caller. 
	 *  @param ret The future that is returned by the service call.
	 *  @param ea The component handling the service call (on that component the periodic updates are scheduled).
	 *  @param to The timeout.
	 *  @param factor (default 0.8) Used to update the timer when factor*to has elapsed.
	 */
	public static void avoidCallTimeouts(final Future<?> ret, IExternalAccess ea, final long to, final double factor, final boolean realtime)
	{
		if(to>0)
		{
			ea.scheduleStep(new ImmediateComponentStep<Void>()
			{
				public IFuture<Void> execute(IInternalAccess ia)
				{
					avoidCallTimeouts(ret, ia, to, factor, realtime);
					return IFuture.DONE;
				}
			});
		}
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ea The external access.
	 */
	public static <T> Future<?> getNoTimeoutFuture(IInternalAccess ia)
	{
		return getFuture((Class<?>)Future.class, false, ia);
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ia The internal access.
	 */
	public static <T> Future<?> getNoTimeoutFuture(Class<T> type, IInternalAccess ia)
	{
		return getFuture(type, false, ia);
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ea The external access.
	 */
	public static <T> Future<?> getNoTimeoutFuture(Class<T> type, IExternalAccess ea)
	{
		return getFuture(type, false, ea);
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ia The external access.
	 */
	public static <T> Future<?> getFuture(boolean timeouts, IInternalAccess ia)
	{
		return getFuture((Class<T>)Future.class, timeouts, ia);
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param type The future implementation type.
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ia The external access.
	 */
	public static <T> Future<?> getFuture(Class<T> type, boolean timeouts, IInternalAccess ia)
	{
		Future<?> ret = getFuture(type);
			
		if(!timeouts)
			avoidCallTimeouts(ret, ia);
		
		return ret;
	}
	
	/**
	 *  Convenience method for creating a future (possibly with timeout avoidance).
	 *  @param type The future implementation type.
	 *  @param timeouts (default is true) False, if no timeouts should be generated when service call timeout has elapsed.
	 *  @param ea The external access.
	 */
	public static <T> Future<?> getFuture(Class<T> type, boolean timeouts, IExternalAccess ea)
	{
		Future<?> ret = getFuture(type);
		
		if(!timeouts)
			avoidCallTimeouts(ret, ea);
		
		return ret;
	}
	
	/**
	 *  Get the matching future object to a future (interface) type.
	 */
	public static Future<?> getFuture(Class<?> clazz)
	{
		Future<?> ret = null;
		
		// todo: refactor to not catch exception but check type?
		
		try
		{
			ret = (Future<?>)clazz.newInstance();
		}
		catch(Exception e)
		{
			if(SReflect.isSupertype(ITuple2Future.class, clazz))
			{
				ret = new Tuple2Future();
			}
			else if(SReflect.isSupertype(IPullSubscriptionIntermediateFuture.class, clazz))
			{
				ret = new PullSubscriptionIntermediateDelegationFuture();
			}
			else if(SReflect.isSupertype(IPullIntermediateFuture.class, clazz))
			{
				ret = new PullIntermediateDelegationFuture();
			}
			else if(SReflect.isSupertype(ISubscriptionIntermediateFuture.class, clazz))
			{
				ret = new SubscriptionIntermediateDelegationFuture();
			}
			else if(SReflect.isSupertype(ITerminableIntermediateFuture.class, clazz))
			{
				ret = new TerminableIntermediateDelegationFuture();
			}
			else if(SReflect.isSupertype(ITerminableFuture.class, clazz))
			{
				ret = new TerminableDelegationFuture();
			}
			else if(SReflect.isSupertype(IIntermediateFuture.class, clazz))
			{
				ret	= new IntermediateFuture();
			}
			else if(SReflect.isSupertype(IFuture.class, clazz))
			{
				ret	= new Future();
			}
			else
			{
				throw SUtil.throwUnchecked(e);
			}
		}
		
		return ret;
	}
	

}
