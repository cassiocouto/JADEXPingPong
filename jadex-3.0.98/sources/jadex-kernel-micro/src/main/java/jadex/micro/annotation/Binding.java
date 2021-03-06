package jadex.micro.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Value;
import jadex.bridge.service.component.BasicServiceInvocationHandler;

/**
 *  The argument annotation.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding
{
	//-------- constants --------
	
	/** None component scope (nothing will be searched, forces required service creation). */
	public static final String SCOPE_NONE = RequiredServiceInfo.SCOPE_NONE;
	
	/** Local component scope. */
	public static final String SCOPE_LOCAL = RequiredServiceInfo.SCOPE_LOCAL;
	
	/** Component scope. */
	public static final String SCOPE_COMPONENT = RequiredServiceInfo.SCOPE_COMPONENT;
	
	/** Application scope. */
	public static final String SCOPE_APPLICATION = RequiredServiceInfo.SCOPE_APPLICATION;

	/** Platform scope. */
	public static final String SCOPE_PLATFORM = RequiredServiceInfo.SCOPE_PLATFORM;

	/** Global scope. */
	public static final String SCOPE_GLOBAL = RequiredServiceInfo.SCOPE_GLOBAL;
	
	/** Parent scope. */
	public static final String SCOPE_PARENT = RequiredServiceInfo.SCOPE_PARENT;
	
	/** The raw proxy type (i.e. no proxy). */
	public static final String	PROXYTYPE_RAW	= BasicServiceInvocationHandler.PROXYTYPE_RAW;
	
	/** The direct proxy type (supports custom interceptors, but uses caller thread). */
	public static final String	PROXYTYPE_DIRECT	= BasicServiceInvocationHandler.PROXYTYPE_DIRECT;
	
	/** The (default) decoupled proxy type (decouples from component thread to caller thread). */
	public static final String	PROXYTYPE_DECOUPLED	= BasicServiceInvocationHandler.PROXYTYPE_DECOUPLED;
	
	//-------- properties --------
	
	/**
	 *  The proxy type.
	 */
	public String proxytype() default PROXYTYPE_DECOUPLED;
	
	/**
	 *  The binding name.
	 */
	public String name() default "";
	
	/**
	 *  The component name.
	 */
	public String componentname() default "";
	
	/**
	 *  The component type.
	 */
	public String componenttype() default "";
	
//	/**
//	 *  The component filename.
//	 */
//	public String componentfilename() default "";

//	/**
//	 *  The creation name.
//	 */
//	public String creationname() default "";
//	
//	/**
//	 *  The creation type.
//	 */
//	public String creationtype() default "";
	
	/**
	 *  The creation info (cannot use @Component as no cycles are allowed in annotations). 
	 */
	public CreationInfo creationinfo() default @CreationInfo;
	
	/**
	 *  The search scope.
	 */
	public String scope() default RequiredServiceInfo.SCOPE_APPLICATION;

	/**
	 *  The dynamic binding flag.
	 */
	public boolean dynamic() default false;

	/**
	 *  The create component flag.
	 */
	public boolean create() default false;
	
	/**
	 *  The error recover flag.
	 */
	public boolean recover() default false;
	
	/**
	 *  The interceptors.
	 */
	public Value[] interceptors() default {};
}
