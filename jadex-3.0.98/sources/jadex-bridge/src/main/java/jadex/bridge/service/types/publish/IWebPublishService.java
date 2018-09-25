package jadex.bridge.service.types.publish;

import java.net.URI;

import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.PublishInfo;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;

/**
 * 
 */
@Service
public interface IWebPublishService extends IPublishService
{
//	/**
//	 *  Publish a servlet (without ressources).
//	 */
//	public IFuture<Void> publishServet(URI uri, Object servlet);
//	
	/**
	 *  Publish permanent redirect.
	 */
	public IFuture<Void> publishRedirect(URI uri, final String html);
	
	/**
	 *  Publish a static page (without ressources).
	 */
//	public IFuture<Void> publishHMTLPage(URI uri, String vhost, String html);
	public IFuture<Void> publishHMTLPage(String uri, String vhost, String html);
	
	/**
	 *  Publish file resources from the classpath.
	 */
	public IFuture<Void> publishResources(URI uri, String rootpath);
	
	/**
	 *  Publish file resources from the file system.
	 */
	public IFuture<Void> publishExternal(URI uri, String rootpath);
	
	/**
	 *  Unpublish a service.
	 *  @param sid The service identifier.
	 */
	public IFuture<Void> unpublishService(IServiceIdentifier sid);
	
	/**
	 *  Unpublish an already-published handler.
	 *  @param vhost The virtual host, if any, null for general.
	 *  @param uti The uri being unpublished.
	 */
	public IFuture<Void> unpublish(String vhost, URI uri);
	
	/**
	 *  Mirror an existing http server.
	 *  @param sourceserveruri The URI of the server being mirrored.
	 *  @param targetserveruri The URI of the mirror server.
	 *  @param info Publish infos for the mirror server.
	 */
	public IFuture<Void> mirrorHttpServer(URI sourceserveruri, URI targetserveruri, PublishInfo info);
	
	/**
	 *  Explicitely terminated an existing http server.
	 *  @param uri URI of the server.
	 */
	public IFuture<Void> shutdownHttpServer(URI uri);
}
