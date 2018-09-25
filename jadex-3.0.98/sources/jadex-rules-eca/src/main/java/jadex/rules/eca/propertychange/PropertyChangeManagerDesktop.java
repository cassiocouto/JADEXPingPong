package jadex.rules.eca.propertychange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import jadex.commons.IResultCommand;
import jadex.commons.SReflect;
import jadex.commons.beans.PropertyChangeEvent;
import jadex.commons.beans.PropertyChangeListener;
import jadex.commons.future.IFuture;

/**
 * Supports Usage of java.beans and jadex.commons.beans types in watched objects.  
 */
public class PropertyChangeManagerDesktop extends PropertyChangeManager
{
	/** The argument types for alternative property change listener adding/removal (cached for speed). */
	protected static Class<?>[]	JAVABEANS_PCL	= new Class[]{java.beans.PropertyChangeListener.class};
	
	/**
	 *  Create a new listener.
	 */
	protected PropertyChangeManagerDesktop()
	{
	}
	
	/**
	 * 
	 * @param pcl
	 * @return
	 */
	private java.beans.PropertyChangeListener wrapJadexPcl(final jadex.commons.beans.PropertyChangeListener pcl)
	{
		return new java.beans.PropertyChangeListener()
		{
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				PropertyChangeEvent jadexPCE = new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				jadexPCE.setPropagationId(evt.getPropagationId());
				pcl.propertyChange(jadexPCE);
			}
		};
	}

	/**
	 *  Deregister a value for observation.
	 *  if its a bean then remove the property listener.
	 */
	public void	removePropertyChangeListener(Object object, IResultCommand<IFuture<Void>, PropertyChangeEvent> eventadder)
	{
		if(object!=null)
		{
//			System.out.println("deregister ("+cnt[0]+"): "+value);
			// Stop listening for bean events.
			if(pcls!=null)
			{
				Map<IResultCommand<IFuture<Void>, PropertyChangeEvent>, Object> mypcls = pcls.get(object);
				if(mypcls!=null)
				{
					if(eventadder!=null)
					{
						Object pcl = mypcls.remove(eventadder);
						removePCL(object, pcl);
					}
					else
					{
						for(Object pcl: mypcls.values())
						{
							removePCL(object, pcl);
						}
						mypcls.clear();
					}
					if(mypcls.size()==0)
						pcls.remove(object);
				}
			}
		}
	}
		
	/**
	 * 
	 */
	protected void removePCL(Object object, Object pcl)
	{
		if(pcl!=null)
		{
			try
			{
//				System.out.println(getTypeModel().getName()+": Deregister: "+value+", "+type);						
				// Do not use Class.getMethod (slow).
				Method	meth = null;
				if(pcl instanceof jadex.commons.beans.PropertyChangeListener) 
				{
					meth = SReflect.getMethod(object.getClass(), "removePropertyChangeListener", PCL);
				} 
				else 
				{
					meth = SReflect.getMethod(object.getClass(), "removePropertyChangeListener", JAVABEANS_PCL);
				}
				
				if(meth!=null)
					meth.invoke(object, new Object[]{pcl});
			}
			catch(IllegalAccessException e){e.printStackTrace();}
			catch(InvocationTargetException e){e.printStackTrace();}
		}
	}

	/**
	 *  Get listener add method
	 */
	@Override
	protected Method	getAddMethod(Object object)
	{
		Method	meth = super.getAddMethod(object);
		// check if java.beans or jadex.commons are used:
		if(meth == null) 
		{
			// Do not use Class.getMethod (slow).
			meth = SReflect.getMethod(object.getClass(), "addPropertyChangeListener", JAVABEANS_PCL);
		}
		
		return meth;
	}
	
	/**
	 *  Create a listener.
	 */
	@Override
	protected Object createPCL(Method meth, IResultCommand<IFuture<Void>, PropertyChangeEvent> eventadder)
	{
		Object	ret	= super.createPCL(meth, eventadder);
		if(meth.getParameterTypes()[0].equals(JAVABEANS_PCL[0]))
		{
			ret	= wrapJadexPcl((PropertyChangeListener)ret);
		}
		return ret;
	}
}
