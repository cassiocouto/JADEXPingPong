package jadex.base.gui.componenttree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;

import jadex.base.SRemoteGui;
import jadex.base.gui.CMSUpdateHandler;
import jadex.base.gui.asynctree.AbstractSwingTreeNode;
import jadex.base.gui.asynctree.AsyncSwingTreeModel;
import jadex.base.gui.asynctree.ISwingTreeNode;
import jadex.base.gui.asynctree.ITreeNode;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.nonfunctional.INFPropertyMetaInfo;
import jadex.bridge.nonfunctional.SNFPropertyProvider;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.ProvidedServiceInfo;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.cms.ICMSComponentListener;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.SReflect;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.gui.CombiIcon;
import jadex.commons.gui.SGUI;
import jadex.commons.gui.future.SwingResultListener;

/**
 *  Node object representing a service container.
 */
public class ComponentTreeNode	extends AbstractSwingTreeNode implements IActiveComponentTreeNode
{
	//-------- constants --------

	/**
	 * The image icons.
	 */
	public static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"overlay_check", SGUI.makeIcon(ComponentTreeNode.class, "/jadex/base/gui/images/overlay_check.png"),
//		"overlay_busy", SGUI.makeIcon(ComponentTreeNode.class, "/jadex/base/gui/images/overlay_busy.png")
		"overlay_busy", SGUI.makeIcon(ComponentTreeNode.class, "/jadex/base/gui/images/overlay_clock.png"),
		"overlay_system", SGUI.makeIcon(ComponentTreeNode.class, "/jadex/base/gui/images/overlay_system.png")
	});
	
	//-------- attributes --------
	
	/** The component description. */
	protected IComponentDescription	desc;
		
	/** The component management service. */
	protected final IComponentManagementService	cms;
		
	/** The platform access. */
	protected IExternalAccess	access;
		
	/** The icon cache. */
	protected final ComponentIconCache	iconcache;
		
	/** The properties component (if any). */
	protected ComponentProperties	propcomp;
	
	/** The cms listener (if any). */
	protected ICMSComponentListener	cmslistener;
	
	/** The component id for listening (if any). */
	protected IComponentIdentifier	listenercid;
	
	/** Flag indicating a broken node (e.g. children could not be searched due to network problems). */
	protected boolean	broken;
	
	/** Flag indicating a busy node (e.g. update in progress). */
	protected boolean	busy;
	
	//-------- constructors --------
	
	/**
	 *  Create a new service container node.
	 */
	public ComponentTreeNode(ISwingTreeNode parent, AsyncSwingTreeModel model, JTree tree, IComponentDescription desc,
		IComponentManagementService cms, ComponentIconCache iconcache, IExternalAccess access)
	{
		super(parent, model, tree);
		
		assert desc!=null;
		
//		System.out.println("node: "+getClass()+" "+desc.getName()+" "+desc.getType());
		
		this.desc	= desc;
		this.cms	= cms;
		this.iconcache	= iconcache;
		this.access	= access;
		
		model.registerNode(this);
		
		// Add CMS listener for platform node.
		if(desc.getName().getParent()==null)
		{
			addCMSListener(desc.getName());
		}
	}
	
	//-------- AbstractComponentTreeNode methods --------
	
	/**
	 *  Get the id used for lookup.
	 */
	public Object	getId()
	{
		return desc.getName();
	}
	
	/**
	 *  Get the icon as byte[] for a node.
	 */
	public byte[] getIcon()
	{
		return null;
	}

	/**
	 *  Get the icon for a node.
	 */
	public Icon	getSwingIcon()
	{
		Icon	icon	= iconcache.getIcon(desc.getType(), this, getModel());
		if(busy)
		{
			icon	= icon!=null ? new CombiIcon(new Icon[]{icon, icons.getIcon("overlay_busy")}) : icons.getIcon("overlay_busy");
		}
		else if(broken)
		{
			icon	= icon!=null ? new CombiIcon(new Icon[]{icon, icons.getIcon("overlay_check")}) : icons.getIcon("overlay_check");
		}
		
		if(desc.isSystemComponent())
		{
			icon	= icon!=null ? new CombiIcon(new Icon[]{icon, icons.getIcon("overlay_system")}) : icons.getIcon("overlay_system");
		}
		return icon;
	}
	
	/**
	 *  Get tooltip text.
	 */
	public String getTooltipText()
	{
		return desc.toString();
	}
	
	/**
	 *  Refresh the node.
	 *  @param recurse	Recursively refresh subnodes, if true.
	 */
	public void refresh(final boolean recurse)
	{
//		System.out.println("CTN refresh: "+getId());
		busy	= true;
		getModel().fireNodeChanged(ComponentTreeNode.this);
		
		cms.getComponentDescription(desc.getName())
			.addResultListener(new SwingResultListener<IComponentDescription>(new IResultListener<IComponentDescription>()
		{
			public void resultAvailable(IComponentDescription result)
			{
				ComponentTreeNode.this.desc	= (IComponentDescription)result;
				broken	= false;
				busy	= false;
				getModel().fireNodeChanged(ComponentTreeNode.this);
				
				ComponentTreeNode.super.refresh(recurse);
			}
			public void exceptionOccurred(Exception exception)
			{
				broken	= true;
				busy	= false;
				getModel().fireNodeChanged(ComponentTreeNode.this);
			}
		}));
	}
	
	/**
	 *  Asynchronously search for children.
	 *  Should call setChildren() once children are found.
	 */
	protected void	searchChildren()
	{
		busy	= true;
		getModel().fireNodeChanged(ComponentTreeNode.this);
//		if(getComponentIdentifier().getName().indexOf("Garbage")!=-1)
//			System.out.println("searchChildren: "+getId());
		searchChildren(cms, getComponentIdentifier())
			.addResultListener(new IResultListener<List<ITreeNode>>()
		{
			public void resultAvailable(List<ITreeNode> result)
			{
				broken	= false;
				busy	= false;
				getModel().fireNodeChanged(ComponentTreeNode.this);
				setChildren(result);
			}
			public void exceptionOccurred(Exception exception)
			{
				broken	= true;
				busy	= false;
				getModel().fireNodeChanged(ComponentTreeNode.this);
				List<ITreeNode> res = Collections.emptyList();
				setChildren(res);
			}
		});
	}
	
	//-------- methods --------
	
	/**
	 *  Create a new component node.
	 */
	public ISwingTreeNode	createComponentNode(final IComponentDescription desc)
	{
		ISwingTreeNode	node	= getModel().getNode(desc.getName());
		if(node==null)
		{
			boolean proxy = "jadex.platform.service.remote.Proxy".equals(desc.getModelName());
			if(proxy)
			{
				// Only create proxy nodes for local proxy components to avoid infinite nesting.
				if(((IActiveComponentTreeNode)getModel().getRoot()).getComponentIdentifier().getName().equals(desc.getName().getPlatformName()))
				{
//					System.out.println("proxy for: "+desc.getName()+" from: "+getDescription().getName());
					node = new ProxyComponentTreeNode(this, getModel(), getTree(), desc, cms, iconcache, access);
				}
				else
				{
//					System.out.println("creating pseudo: "+desc.getName()+" from: "+getDescription().getName());
					node = new PseudoProxyComponentTreeNode(this, getModel(), getTree(), desc, cms, iconcache, access);
				}
			}
			else
			{
				node = new ComponentTreeNode(this, getModel(), getTree(), desc, cms, iconcache, access);
			}
		}
		return node;
	}
	
	/**
	 *  Create a string representation.
	 */
	public String toString()
	{
		return desc.getName().getLocalName();
	}
	
	/**
	 *  Get the component description.
	 */
	public IComponentDescription	getDescription()
	{
		return desc;
	}
	
	/**
	 *  Get the component id.
	 */
	public IComponentIdentifier getComponentIdentifier()
	{
		return desc!=null? desc.getName(): null;
	}

	/**
	 *  Set the component description.
	 */
	public void setDescription(IComponentDescription desc)
	{
		this.desc	= desc;
		if(propcomp!=null)
		{
			propcomp.setDescription(desc);
			propcomp.repaint();
		}
	}

	/**
	 *  True, if the node has properties that can be displayed.
	 */
	public boolean	hasProperties()
	{
		return true;
	}

	
	/**
	 *  Get or create a component displaying the node properties.
	 *  Only to be called if hasProperties() is true;
	 */
	public JComponent	getPropertiesComponent()
	{
		// Refresh to update cid addresses later.
		refresh(false);
		
		if(propcomp==null)
		{
			propcomp	= new ComponentProperties();
		}
		propcomp.setDescription(desc);
//		System.out.println(desc.getName()+" "+getClass().getName());
		return propcomp;
	}

	/**
	 *  Asynchronously search for children.
	 */
	protected IFuture<List<ITreeNode>> searchChildren(final IComponentManagementService cms, final IComponentIdentifier cid)
	{
		final Future<List<ITreeNode>>	ret	= new Future<List<ITreeNode>>();
		final List<ITreeNode>	children	= new ArrayList<ITreeNode>();
		final boolean	ready[]	= new boolean[2];	// 0: children, 1: services;

//		if(ComponentTreeNode.this.toString().indexOf("Hunter")!=-1)
//			System.out.println("searchChildren 1: "+this);
		cms.getChildrenDescriptions(cid).addResultListener(new SwingResultListener<IComponentDescription[]>(new IResultListener<IComponentDescription[]>()
		{
			public void resultAvailable(final IComponentDescription[] achildren)
			{
//				if(ComponentTreeNode.this.toString().indexOf("Hunter")!=-1)
//					System.out.println("searchChildren 2: "+ComponentTreeNode.this+" "+achildren.length);
//				final IComponentDescription[] achildren = (IComponentDescription[])result;
				
				Arrays.sort(achildren, new java.util.Comparator<IComponentDescription>()
				{
					public int compare(IComponentDescription o1, IComponentDescription o2)
					{
						return o1.getName().getName().toLowerCase().compareTo(o2.getName().getName().toLowerCase());
					}
				});
				
				for(int i=0; i<achildren.length; i++)
				{
					ISwingTreeNode	node	= createComponentNode(achildren[i]);
					children.add(node);
				}
				ready[0]	= true;
				if(ready[0] &&  ready[1])
				{
					ret.setResult(children);
				}
			}
			
			public void exceptionOccurred(Exception exception)
			{
//				if(ComponentTreeNode.this.toString().indexOf("Hunter")!=-1)
//					System.out.println("searchChildren ex: "+ComponentTreeNode.this);
				ready[0]	= true;
				if(ready[0] &&  ready[1])
				{
					ret.setExceptionIfUndone(exception);
				}
			}
		}));
		
		// Search services and only add container node when services are found.
//		System.out.println("name: "+desc.getName());
		
		cms.getRootIdentifier().addResultListener(new SwingResultListener<IComponentIdentifier>(new IResultListener<IComponentIdentifier>()
		{
			public void resultAvailable(IComponentIdentifier root) 
			{
				cms.getExternalAccess(root)
					.addResultListener(new SwingResultListener<IExternalAccess>(new IResultListener<IExternalAccess>()
				{
					public void resultAvailable(final IExternalAccess rootea)
					{
						cms.getExternalAccess(cid)
							.addResultListener(new SwingResultListener<IExternalAccess>(new IResultListener<IExternalAccess>()
						{
							public void resultAvailable(final IExternalAccess ea)
							{
	//							System.out.println("search childs: "+ea);
								
								SNFPropertyProvider.getNFPropertyNames(ea)
//								((INFPropertyProvider)ea.getExternalComponentFeature(INFPropertyComponentFeature.class)).getNFPropertyNames()
									.addResultListener(new SwingResultListener<String[]>(new IResultListener<String[]>()
								{
									public void resultAvailable(String[] names)
									{
										if(names!=null && names.length>0)
										{
											NFPropertyContainerNode cn = (NFPropertyContainerNode)getModel().getNode(getId()+NFPropertyContainerNode.NAME);
											if(cn==null)
												cn = new NFPropertyContainerNode(null, null, ComponentTreeNode.this, getModel(), getTree(), ea, null, null, null);
											children.add(0, cn);
											cont(ea);
//											final NFPropertyContainerNode node = cn;
//											
//											final List<ISwingTreeNode>	results	= new ArrayList<ISwingTreeNode>();
//											Iterator<String> it = SReflect.getIterator(names);
											
//											createNFPropertyNodes(it, results, ea, rootea, cn).addResultListener(new IResultListener<Void>()
//											{
//												public void resultAvailable(Void result)
//												{
//													Collections.sort(results, new java.util.Comparator<ISwingTreeNode>()
//													{
//														public int compare(ISwingTreeNode t1, ISwingTreeNode t2)
//														{
//															String si1 = ((NFPropertyNode)t1).getMetaInfo().getName();
//															String si2 = ((NFPropertyNode)t2).getMetaInfo().getName();
//															return si1.compareTo(si2);
//														}
//													});
//													
//													node.setChildren(results);
//													cont(ea, node, results);
//												}
//												
//												public void exceptionOccurred(Exception exception)
//												{
//													cont(ea, null, null);
//												}
//											});
										}
										else
										{
											cont(ea);
										}
									}
									
									public void exceptionOccurred(Exception exception)
									{
										cont(ea);
									}
								}));
							}
							
							public void cont(final IExternalAccess ea)
							{
								SRemoteGui.getServiceInfos(ea)
									.addResultListener(new SwingResultListener<Object[]>(new IResultListener<Object[]>()
								{
									public void resultAvailable(final Object[] res)
									{
										final ProvidedServiceInfo[] pros = (ProvidedServiceInfo[])res[0];
										final RequiredServiceInfo[] reqs = (RequiredServiceInfo[])res[1];
										final IServiceIdentifier[] sis = (IServiceIdentifier[])res[2];
	//									if(sis.length>0 && sis[0].getProviderId().getName().indexOf("Mandel")!=-1)
	//										System.out.println("gotacha: "+sis[0].getProviderId().getName());
										if((pros!=null && pros.length>0 || (reqs!=null && reqs.length>0)))
										{
											ServiceContainerNode	scn	= (ServiceContainerNode)getModel().getNode(getId()+ServiceContainerNode.NAME);
											if(scn==null)
												scn	= new ServiceContainerNode(ComponentTreeNode.this, getModel(), getTree(), ea);
	//										System.err.println(getModel().hashCode()+", "+ready.hashCode()+" searchChildren.add "+scn);
											children.add(0, scn);
											
											final List<ISwingTreeNode>	subchildren	= new ArrayList<ISwingTreeNode>();
											if(pros!=null)
											{
												for(int i=0; i<pros.length; i++)
												{
													try
													{
														String id	= ProvidedServiceInfoNode.getId(scn, pros[i]);
														ProvidedServiceInfoNode	sn	= (ProvidedServiceInfoNode)getModel().getNode(id);
														if(sn==null)
															sn	= new ProvidedServiceInfoNode(scn, getModel(), getTree(), pros[i], sis[i], ea, access);
														subchildren.add(sn);
													}
													catch(Exception e)
													{
														e.printStackTrace();
													}
												}
												
												Collections.sort(subchildren, new java.util.Comparator<ISwingTreeNode>()
												{
													public int compare(ISwingTreeNode t1, ISwingTreeNode t2)
													{
														ProvidedServiceInfo si1 = ((ProvidedServiceInfoNode)t1).getServiceInfo();
														ProvidedServiceInfo si2 = ((ProvidedServiceInfoNode)t2).getServiceInfo();
														return SReflect.getUnqualifiedTypeName(si1.getType().getTypeName())
															.compareTo(SReflect.getUnqualifiedTypeName(si2.getType().getTypeName()));
													}
												});
											}
											
											if(reqs!=null)
											{
												Arrays.sort(reqs, new java.util.Comparator<RequiredServiceInfo>()
												{
													public int compare(RequiredServiceInfo o1, RequiredServiceInfo o2)
													{
														return SReflect.getUnqualifiedTypeName(o1.getType().getTypeName())
															.compareTo(SReflect.getUnqualifiedTypeName(o2.getType().getTypeName()));
													}
												});
												
												for(int i=0; i<reqs.length; i++)
												{
													String nid = ea.getComponentIdentifier()+"."+reqs[i].getName();
													RequiredServiceNode	sn = (RequiredServiceNode)getModel().getNode(nid);
													if(sn==null)
														sn	= new RequiredServiceNode(scn, getModel(), getTree(), reqs[i], nid, ea);
													subchildren.add(sn);
												}
											}
											
											final ServiceContainerNode	node	= scn;
											ret.addResultListener(new SwingResultListener<List<ITreeNode>>(new IResultListener<List<ITreeNode>>()
											{
												public void resultAvailable(List<ITreeNode> result)
												{
													node.setChildren(subchildren);
//													if(pcnode!=null)
//														pcnode.setChildren(pcchilds);
												}
												public void exceptionOccurred(Exception exception)
												{
													// Children not found -> don't add services.
												}
											}));
										}
										
										ready[1]	= true;
										if(ready[0] &&  ready[1])
										{
											ret.setResult(children);
										}
									}
									
									public void exceptionOccurred(Exception exception)
									{
										ready[1]	= true;
										if(ready[0] &&  ready[1])
										{
											ret.setExceptionIfUndone(exception);
										}
									}
								}));
							}
							
							public void exceptionOccurred(Exception exception)
							{
								ready[1]	= true;
								if(ready[0] &&  ready[1])
								{
									ret.setExceptionIfUndone(exception);
								}
							}
						}));
					}
					public void exceptionOccurred(Exception exception)
					{
						ready[1]	= true;
						if(ready[0] &&  ready[1])
						{
							ret.setExceptionIfUndone(exception);
						}
					}
				}));
			}
			public void exceptionOccurred(Exception exception)
			{
				ready[1]	= true;
				if(ready[0] &&  ready[1])
				{
					ret.setExceptionIfUndone(exception);
				}
			}
		}));
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected IFuture<Void> createNFPropertyNodes(final Iterator<String> names, final List<ISwingTreeNode> results, 
		final IExternalAccess provider, final IExternalAccess rootea, final NFPropertyContainerNode cn)
	{
		final Future<Void> ret = new Future<Void>();
		if(names.hasNext())
		{
			final String name = names.next();
			String id = NFPropertyNode.getId(cn.getId(), name);
			NFPropertyNode nfpn	= (NFPropertyNode)getModel().getNode(id);
			if(nfpn==null)
			{
				SNFPropertyProvider.getNFPropertyMetaInfo(provider, name)
//				((INFPropertyProvider)provider.getExternalComponentFeature(INFPropertyComponentFeature.class))
					.addResultListener(new SwingResultListener<INFPropertyMetaInfo>(new IResultListener<INFPropertyMetaInfo>()
				{
					public void resultAvailable(INFPropertyMetaInfo pmi) 
					{
//						NFPropertyNode nfpn	= new NFPropertyNode(cn, getModel(), getTree(), pmi, rootea);
						NFPropertyNode nfpn	= new NFPropertyNode(cn, getModel(), getTree(), pmi, provider, null, null, null);
						results.add(nfpn);
						createNFPropertyNodes(names, results, provider, rootea, cn).addResultListener(new DelegationResultListener<Void>(ret));
					}
					
					public void exceptionOccurred(Exception exception)
					{
						System.out.println("Could not fetch nf property meta info: "+name);
						createNFPropertyNodes(names, results, provider, rootea, cn).addResultListener(new DelegationResultListener<Void>(ret));
					}
				}));
			}
			else
			{
				results.add(nfpn);
				createNFPropertyNodes(names, results, provider, rootea, cn).addResultListener(new DelegationResultListener<Void>(ret));
			}
		}
		else
		{
			ret.setResult(null);
		}
		return ret;
	}
	
	/**
	 *  Add a CMS listener for tree updates of components from the given (platform) id.
	 */
	protected void	addCMSListener(IComponentIdentifier cid)
	{
		assert cmslistener==null;
		CMSUpdateHandler	cmshandler	= (CMSUpdateHandler)getTree().getClientProperty(CMSUpdateHandler.class);
		this.listenercid	= cid;
		this.cmslistener	= new ICMSComponentListener()
		{
			public IFuture<Void> componentRemoved(final IComponentDescription desc, Map<String, Object> results)
			{
				final ISwingTreeNode node = getModel().getNodeOrAddZombie(desc.getName());
//				if(desc.getName().toString().startsWith("ANDTest@"))
//				System.out.println("Component removed0: "+desc.getName().getName()+", zombie="+(node==null));
				if(node!=null)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							if(getModel().getNodeOrAddZombie(desc.getName())!=null)
							{
//								if(desc.getName().toString().startsWith("ANDTest@"))
//									System.out.println("Component removed: "+desc.getName().getName());
								((AbstractSwingTreeNode)node.getParent()).removeChild(node);
							}
						}
					});
				}
				return IFuture.DONE;
			}
			
			public IFuture<Void> componentChanged(final IComponentDescription desc)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						ComponentTreeNode	node	= (ComponentTreeNode)getModel().getAddedNode(desc.getName());
						if(node!=null)
						{
							node.setDescription(desc);
							getModel().fireNodeChanged(node);
						}
					}
				});
				return IFuture.DONE;
			}
			
			public IFuture<Void> componentAdded(final IComponentDescription desc)
			{
//				System.out.println("Component added0: "+desc.getName().getName());
//				System.err.println(""+model.hashCode()+" Panel->addChild queued: "+desc.getName()+", "+desc.getParent());
				
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
//						System.err.println(""+model.hashCode()+" Panel->addChild queued2: "+desc.getName()+", "+desc.getParent());
						final ComponentTreeNode	parentnode = desc.getName().getParent()==null ? null
								: desc.getName().getParent().equals(getComponentIdentifier()) ? ComponentTreeNode.this	// For proxy nodes.
								: (ComponentTreeNode)getModel().getAddedNode(desc.getName().getParent());
						if(parentnode!=null)
						{
							ISwingTreeNode	node = (ISwingTreeNode)parentnode.createComponentNode(desc);
//							System.out.println("addChild: "+parentnode+", "+node);
							try
							{
								if(parentnode.getIndexOfChild(node)==-1)
								{
//									if(desc.getName().toString().startsWith("ANDTest@"))
//										System.out.println("Component added: "+desc.getName().getName());
//									System.err.println(""+model.hashCode()+" Panel->addChild: "+node+", "+parentnode);
									boolean ins = false;
									for(int i=0; i<parentnode.getChildCount() && !ins; i++)
									{
										ISwingTreeNode child = parentnode.getChild(i);
										if(child instanceof ServiceContainerNode || child instanceof NFPropertyContainerNode)
											continue;
										if(child.toString().toLowerCase().compareTo(node.toString().toLowerCase())>=0)
										{
											parentnode.addChild(i, node);
											ins = true;
										}
									}
									if(!ins)
									{
										parentnode.addChild(node);
									}
								}
//								else
//								{
//									if(desc.getName().toString().startsWith("ANDTest@"))
//										System.out.println("Not added: "+desc.getName().getName());
//								}
							}
							catch(Exception e)
							{
								System.err.println(""+getModel().hashCode()+" Broken node: "+node);
								System.err.println(""+getModel().hashCode()+" Parent: "+parentnode+", "+parentnode.getCachedChildren());
								e.printStackTrace();
//								model.fireNodeAdded(parentnode, node, parentnode.getIndexOfChild(node));
							}
						}
//						else
//						{
//							System.out.println("no parent, addChild: "+desc.getName()+", "+desc.getParent());
//						}
					}
				});
				
				return IFuture.DONE;
			}
		};
		cmshandler.addCMSListener(listenercid, cmslistener);
	}

	/**
	 *  Remove listener, if any.
	 */
	public void dispose()
	{
		if(cmslistener!=null)
		{
			CMSUpdateHandler	cmshandler	= (CMSUpdateHandler)getTree().getClientProperty(CMSUpdateHandler.class);
			cmshandler.removeCMSListener(listenercid, cmslistener);
		}
	}
}
