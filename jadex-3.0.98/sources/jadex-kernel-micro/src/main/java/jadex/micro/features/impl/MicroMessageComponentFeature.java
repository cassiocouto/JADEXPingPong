package jadex.micro.features.impl;

import jadex.bridge.IComponentStep;
import jadex.bridge.IConnection;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.component.ComponentCreationInfo;
import jadex.bridge.component.IComponentFeatureFactory;
import jadex.bridge.component.IMessageFeature;
import jadex.bridge.component.impl.ComponentFeatureFactory;
import jadex.bridge.component.impl.MessageComponentFeature;
import jadex.micro.annotation.AgentMessageArrived;
import jadex.micro.annotation.AgentStreamArrived;

/**
 *  Extension to allow message injection in agent methods.
 */
public class MicroMessageComponentFeature extends MessageComponentFeature
{
	//-------- constants --------
	
	/** The factory. */
	public static final IComponentFeatureFactory FACTORY = new ComponentFeatureFactory(IMessageFeature.class, MicroMessageComponentFeature.class);
	
	//-------- constructors --------
	
	/**
	 *  Create the feature.
	 */
	public MicroMessageComponentFeature(IInternalAccess component, ComponentCreationInfo cinfo)
	{
		super(component, cinfo);
	}
	
	//-------- IInternalMessageFeature interface --------
	
	/**
	 *  Helper method to override message handling.
	 *  May be called from external threads.
	 */
	protected IComponentStep<Void> createHandleMessageStep(IMessageAdapter message)
	{
		return new HandleMicroMessageStep(message);
	}
	
	/**
	 *  Step to handle a message.
	 */
	public class HandleMicroMessageStep	extends HandleMessageStep
	{
		public HandleMicroMessageStep(IMessageAdapter message)
		{
			super(message);
		}

		/**
		 *  Extracted to allow overriding behaviour.
		 *  @return true, when at least one matching handler was found.
		 */
		protected boolean invokeHandlers(IMessageAdapter message)
		{
			boolean	ret	= super.invokeHandlers(message);
			
			if(!ret)
			{
				MicroLifecycleComponentFeature.invokeMethod(getComponent(), AgentMessageArrived.class, new Object[]{message, message.getParameterMap(), message.getMessageType()});
			}
			
			return ret;
		}
	}

	/**
	 *  Helper method to override stream handling.
	 *  May be called from external threads.
	 */
	protected IComponentStep<Void> createHandleStreamStep(IConnection con)
	{
		return new HandleMicroStreamStep(con);
	}
	
	/**
	 *  Step to handle a message.
	 */
	public class HandleMicroStreamStep	extends HandleStreamStep
	{
		public HandleMicroStreamStep(IConnection con)
		{
			super(con);
		}

		/**
		 *  Extracted to allow overriding behaviour.
		 *  @return true, when at least one matching handler was found.
		 */
		protected boolean invokeHandlers(IConnection con)
		{
			boolean	ret	= super.invokeHandlers(con);
			
			if(!ret)
			{
				MicroLifecycleComponentFeature.invokeMethod(getComponent(), AgentStreamArrived.class, new Object[]{con});
			}
			
			return ret;
		}
	}
}
