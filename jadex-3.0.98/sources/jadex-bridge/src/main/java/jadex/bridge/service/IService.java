package jadex.bridge.service;

import java.util.Map;

import jadex.bridge.service.annotation.Reference;
import jadex.commons.future.IFuture;


/**
 *  The interface for platform services.
 */
@Reference
public interface IService //extends INFMixedPropertyProvider //extends IRemotable INFPropertyProvider, INFMethodPropertyProvider, 
{
	//-------- constants --------
	
	/** Empty service array. */
	public static final IService[] EMPTY_SERVICES = new IService[0];

	//-------- methods --------

	/**
	 *  Get the service identifier.
	 *  @return The service identifier.
	 */
	public IServiceIdentifier getServiceIdentifier();
	
	/**
	 *  Test if the service is valid.
	 *  @return True, if service can be used.
	 */
	public IFuture<Boolean> isValid();
		
	/**
	 *  Get the map of properties (considered as constant).
	 *  @return The service property map (if any).
	 */
	public Map<String, Object> getPropertyMap();

//	/**
//	 *  Get an external interface feature.
//	 *  @param type The interface type of the feature.
//	 *  @return The feature.
//	 */
//	public <T> T getExternalComponentFeature(Class<T> type);
	
	// todo: ?! currently BasicService only has 
//	/**
//	 *  Get the hosting component of the service.
//	 *  @return The component.
//	 */
//	public IFuture<IExternalAccess> getComponent();
}
