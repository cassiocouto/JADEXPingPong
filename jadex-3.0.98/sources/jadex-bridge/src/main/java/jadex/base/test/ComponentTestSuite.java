package jadex.base.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import jadex.base.Starter;
import jadex.base.test.impl.ComponentLoadTest;
import jadex.base.test.impl.ComponentStartTest;
import jadex.base.test.impl.ComponentTest;
import jadex.bridge.IErrorReport;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.modelinfo.IArgument;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Timeout;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.factory.SComponentFactory;
import jadex.bridge.service.types.library.ILibraryService;
import jadex.commons.SNonAndroid;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.future.Future;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;


/**
 * Execute multiple component tests in a test suite.
 */
@RunWith(AllTests.class)
public class ComponentTestSuite extends TestSuite implements IAbortableTestSuite
{
	//-------- constants --------
	
	/**
	 *  The default test platform arguments.
	 */
	public static final String[]	DEFARGS	= new String[]
	{
		"-platformname", "testcases_*",
//		"-kernels", "\"all\"",	// Required for old hudson build, otherwise wrong bdi kernel is used as dependencies are not in correct order
		"-simulation", "true",
		"-asyncexecution", "true",
//		"-libpath", "new String[]{\""+root.toURI().toURL().toString()+"\"}",
//		"-logging", "true",
//		"-logging", path.toString().indexOf("bdiv3")!=-1 ? "true" : "false",
//		"-logging_level", "java.util.logging.Level.WARNING",
//		"-debugfutures", "true",
//		"-nostackcompaction", "true",
		"-gui", "false",
		"-awareness", "false",
		"-saveonexit", "false",
		"-welcome", "false",
		"-autoshutdown", "false",
		"-opengl", "false",
		"-cli", "false",
//		"-persist", "true", // for testing persistence
//		"-niotcptransport", "false",
//		"-tcptransport", "true",
//		"-deftimeout", "-1",
		"-printpass", "false",
		// Hack!!! include ssl transport if available
		"-ssltcptransport", (SReflect.findClass0("jadex.platform.service.message.transport.ssltcpmtp.SSLTCPTransport", null, ComponentTestSuite.class.getClassLoader())!=null ? "true" : "false"),  
	};

	//-------- attributes --------
	
	/** Indicate when the suite is aborted due to excessive run time. */
	public boolean	aborted;
	
	/** The platform. */
	protected IExternalAccess	platform;
	
	/** The class loader. */
	protected ClassLoader	classloader;
	
	/** The timeout (if any). */
	protected long	timeout;
	
	/** The timeout timer (if any). */
	protected Timer	timer;
	
	//-------- constructors --------

	/**
	 * Create a component test suite for components contained in class directories on the class path (i.e. not jars).
	 * @param excludes	Files to exclude (if a pattern is contained in file path).
	 */
	public ComponentTestSuite(String[] excludes) throws Exception
	{
		this(findClassDirectories(), null, excludes);
	}
	
	/**
	 * Create a component test suite for components contained in a given path.
	 * @param projectDir	The project directory.
	 * @param excludes	Files to exclude (if a pattern is contained in file path).
	 */
	public ComponentTestSuite(String projectDir, String[] excludes) throws Exception
	{
		this(new File[][]{SUtil.findOutputDirs(projectDir)}, null, excludes);
	}

	/**
	 * Create a component test suite for components contained in a given path.
	 * @param projectDir	The project directory.
	 * @param tests	The tests (full qualified names) to include. Includes all, if null.
	 * @param excludes	Files to exclude (if a pattern is contained in file path). 
	 */
	public ComponentTestSuite(String projectDir, String[] tests, String[] excludes) throws Exception
	{
		this(new File[][]{SUtil.findOutputDirs(projectDir)}, tests, excludes);
	}
	
	/**
	 * Create a component test suite for components contained in a given path.
	 * @param root	The class path root corresponding to the path.
	 * @param excludes	Files to exclude (if a pattern is contained in file path). 
	 */
//	public ComponentTestSuite(File[] root, String[] excludes) throws Exception
//	{
//		this(root, root, excludes);
//	}
	
	/**
	 * Create a component test suite for components contained in a given path.
	 * @param roots The paths to search for testcases in and to load classes from.
	 * @param tests	The tests (full qualified names) to include. Includes all, if null.
	 * @param excludes	Files to exclude (if a pattern is contained in file path). 
	 */
	public ComponentTestSuite(File[][] roots, String[] tests, String[] excludes) throws Exception
	{
		this(roots, tests, excludes, true, true, true);
//		this(path, roots, excludes, false, false, true);
	}
	
	/**
	 * Create a component test suite for components contained in a given path.
	 * @param roots The paths to search for testcases in and to load classes from.
	 * @param tests	The tests (full qualified names) to include. Includes all, if null.
	 * @param excludes	Files to exclude (if a pattern is contained in file path). 
	 * @param test	Run test components.
	 * @param load	Include broken components (will cause test failure if any). Also shows loadable, but not startable components as succeeded tests.
	 * @param start	Try starting components, which are no test cases.
	 */
	public ComponentTestSuite(File[][] roots, String[] tests, String[] excludes, boolean test, boolean load, boolean start) throws Exception
	{
		this(DEFARGS, roots, tests, excludes, test, load, start);
	}
	
	protected void startTimer()
	{
		final Thread	runner	= Thread.currentThread();

		if(timeout!=Timeout.NONE)
		{
			timer	= new Timer(true);
			timer.schedule(new TimerTask()
			{
				public void run()
				{
					aborted	= true;
					System.out.println("Aborting test suite "+getName()+" due to excessive run time (>"+timeout+" ms).");
					if(!SReflect.isAndroid())
					{
						try
						{
							runner.stop(new RuntimeException("Aborting test suite "+getName()+" due to excessive run time (>"+timeout+" ms)."));
						}
						catch(UnsupportedOperationException e)
						{
							runner.stop();
						}
					}
					else
					{
						System.err.println("Aborting test suite "+getName()+" due to excessive run time (>"+timeout+" ms).");
						System.exit(1);
					}
				}
			}, timeout);
		}
	}

	/**
	 * Create a component test suite for components contained in a given path.
	 * @param args	The platform arguments.
     * @param roots The paths to search for testcases in and to load classes from.
	 *              Grouped by project, e.g. proj1/build/classes and proj1/build/resources
	 *              should be both at index [0] of this array.
	 * @param tests	The tests (full qualified names) to include. Includes all, if null.
	 * @param excludes	Files to exclude (if a pattern is contained in file path).
	 * @param runtests	Run test components.
	 * @param load	Include broken components (will cause test failure if any).
	 * @param start	Try starting components, which are no test cases.
	 */
	public ComponentTestSuite(String[] args, File[][] roots, String[] tests, String[] excludes, final boolean runtests, final boolean load, final boolean start) throws Exception
	{
		super(roots[0][0].toString());
		this.timeout	= Starter.getLocalDefaultTimeout(null);	// Initial timeout for starting platform.
		startTimer();

		if (tests != null) {
			for (int i = 0; i < tests.length; i++) {
				tests[i] = tests[i].replace('.', File.separatorChar);
			}
		}

		// Tests must be available after constructor execution.

//		System.out.println("start platform");
		platform	= Starter.createPlatform(args).get();
		this.timeout	= Starter.getLocalDefaultTimeout(platform.getComponentIdentifier());
//		System.out.println("end platform");
		IComponentManagementService cms = SServiceProvider.getService(platform, IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		ILibraryService libsrv	= SServiceProvider.getService(platform, ILibraryService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();

		// Only works with x-rids hack or maven dependency service, because rms cannot use default classloader for decoding application messages.
//		final IResourceIdentifier	rid	= null;
		long ctimeout = Starter.getLocalDefaultTimeout(platform.getComponentIdentifier());	// Start with normal timeout for platform startup/shutdown.

		IResourceIdentifier[] rids = new IResourceIdentifier[roots.length];
		for (int projectIndex=0; projectIndex < roots.length; projectIndex++) {
			File[] project = roots[projectIndex];
			IResourceIdentifier	parentRid	= null;
			for(int rootIndex=0; rootIndex<project.length; rootIndex++)
			{
				try
				{
					URL url = project[rootIndex].toURI().toURL();
					if(parentRid==null)
					{
						parentRid	= libsrv.addURL(null, url).get();
						rids[projectIndex] = parentRid;
					}
					else
					{
						libsrv.addURL(parentRid, url).get();
					}
				}
				catch(Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		this.classloader	= libsrv.getClassLoader(null).get();
		stopTimer();

		// disable Future debugging during testcase scan for performance reasons.
		boolean originalDebug = Future.DEBUG;
		Future.DEBUG = false;
		for (int projectIndex=0; projectIndex < roots.length; projectIndex++) {
			File[] project = roots[projectIndex];
			for (int rootIndex = 0; rootIndex < project.length; rootIndex++) {
				// Scan for test cases.
				List<String> scanForTestCases = getAllFiles(project[rootIndex]);
				this.timeout = Starter.getScaledLocalDefaultTimeout(platform.getComponentIdentifier(), 1 + 0.05 * scanForTestCases.size());    // Timeout for loading models.
				startTimer();
				Logger.getLogger("ComponentTestSuite").info("Scanning for testcases: " + project[rootIndex] + " (scan timeout: " + timeout + ")");
				for (String abspath : scanForTestCases) {
					boolean exclude = false;
					boolean include = (tests == null);

					for (int i = 0; !exclude && excludes != null && i < excludes.length; i++) {
						exclude = abspath.indexOf(excludes[i]) != -1;
					}

					for (int i = 0; !include && i < tests.length; i++) {
						include = abspath.indexOf(tests[i]) != -1;
					}

					if (!exclude && include) {
						try {
							IResourceIdentifier rid = rids[projectIndex];

							if ((SComponentFactory.isLoadable(platform, abspath, rid).get()).booleanValue()) {
								boolean startable = SComponentFactory.isStartable(platform, abspath, rid).get().booleanValue();
								IModelInfo model = SComponentFactory.loadModel(platform, abspath, rid).get();
								boolean istest = false;
								if (model != null && model.getReport() == null && startable) {
									IArgument[] results = model.getResults();
									for (int i = 0; !istest && i < results.length; i++) {
										if (results[i].getName().equals("testresults") && Testcase.class.equals(
												results[i].getClazz().getType(libsrv.getClassLoader(rid).get(), model.getAllImports()))) {
											istest = true;
										}
									}
								}

								if (istest) {
									System.out.print(".");
									if (runtests) {
										ComponentTest test = new ComponentTest(cms, model, this);
										test.setName(abspath);
										addTest(test);
										if (ctimeout == Timeout.NONE || test.getTimeout() == Timeout.NONE) {
											ctimeout = Timeout.NONE;
										} else {
											ctimeout += test.getTimeout();
										}
									}
								} else if (startable && model.getReport() == null) {
									System.out.print(".");
									if (start) {
										ComponentStartTest test = new ComponentStartTest(cms, model, this);
										test.setName(abspath);
										addTest(test);
										if (ctimeout == Timeout.NONE) {
											ctimeout = Timeout.NONE;
										} else {
											// Delay instead of timeout as start test should be finished after that.
											ctimeout += test.getTimeout();
										}
									}
								} else if (load) {
									System.out.print(".");
									ComponentLoadTest test = new ComponentLoadTest(model, model.getReport());
									test.setName(abspath);
									addTest(test);
								}
							}
						} catch (final RuntimeException e) {
							if (load) {
								ComponentLoadTest test = new ComponentLoadTest(abspath, new IErrorReport() {
									public String getErrorText() {
										StringWriter sw = new StringWriter();
										e.printStackTrace(new PrintWriter(sw));
										return "Error loading model: " + sw.toString();
									}

									public String getErrorHTML() {
										return getErrorText();
									}

									public Map<String, String> getDocuments() {
										return null;
									}
								});
								test.setName(abspath);
								addTest(test);
							}
						}
					}
				}

				stopTimer();
				Future.DEBUG = originalDebug;
				Logger.getLogger("ComponentTestSuite").info("Finished Building Suite for " + project[rootIndex] + ", cumulated execution timeout is: " + ctimeout);
			}
		}
		this.timeout	= ctimeout;
	}

	protected void	stopTimer()
	{
		if(timer!=null)
		{
			timer.cancel();
			timer	= null;
		}
	}

	protected List<String> getAllFiles(File root)
	{
		List<String> result = new ArrayList<String>();

		if (SReflect.isAndroid())
		{
			try
			{
				// Scan for resource files in .apk
				String	template	= root.toString().replace('.', '/');
				ZipFile	zip	= new ZipFile(root);
				Enumeration< ? extends ZipEntry>	entries	= zip.entries();
				while(entries.hasMoreElements())
				{
					ZipEntry	entry	= entries.nextElement();
					String name	= entry.getName();
					if(name.startsWith(template))
					{
						result.add(name);
//						System.out.println("Found potential Testcase: "+name);
					}
				}
				zip.close();

				// Scan for classes in .dex
				Enumeration<String> dexEntries = SUtil.androidUtils().getDexEntries(root);
				String nextElement;
				while(dexEntries.hasMoreElements())
				{
					nextElement = dexEntries.nextElement();
					if(nextElement.toLowerCase().startsWith(root.toString().toLowerCase()))
//						&& nextElement.toLowerCase().split("\\.").length  (path.toString().split("\\.").length +1))
					{
						if(!nextElement.matches(".*\\$.*"))
						{
							// path-style identifier needed for Factories, but android doesn't use a classical classpath
							nextElement = nextElement.replaceAll("\\.", "/") + ".class";
							result.add(nextElement);
//							System.out.println("Found potential Testcase: " + nextElement);
						}
					}
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			List<File>	todo	= new LinkedList<File>();
//			if(path.toString().indexOf("micro")!=-1)
			todo.add(root);

			while(!todo.isEmpty())
			{
				File	file	= (File)todo.remove(0);
				final String	abspath	= file.getAbsolutePath();
	//			System.out.println("todo: "+abspath);

				if(file.isDirectory())
				{
					File[]	subs	= file.listFiles();
					todo.addAll(Arrays.asList(subs));
				}
				else
				{
					result.add(abspath);
				}
			}
		}

		return result;
		
	}

	/**
	 *  Indicate when the suite is aborted due to excessive run time.
	 */
	public boolean isAborted()
	{
		return aborted;
	}
	
	/**
	 *  Overridden for pre and post code.
	 */
	public void run(TestResult result)
	{
		startTimer();
		if(timer==null)
		{
			this.timer	= new Timer(true);
		}
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				System.out.println("Memory: free="+SUtil.bytesToString(Runtime.getRuntime().freeMemory())
					+", max="+SUtil.bytesToString(Runtime.getRuntime().maxMemory())
					+", total="+SUtil.bytesToString(Runtime.getRuntime().totalMemory()));
			}
		}, 0, 30000);

		super.run(result);

		cleanup(result);
	}
	
	/**
	 *  Called after test suite is finished.
	 */
	protected void	cleanup(TestResult result)
	{
		try
		{
			platform.killComponent().get();
		}
		catch(Exception e)
		{
			result.addError(this, e);
		}
		platform	= null;
		
		clearAWT();
		
		stopTimer();
	}
	
	/**
	 *  Workaround for AWT/Swing memory leaks.
	 */
	public static void	clearAWT()
	{
		if(SReflect.HAS_GUI)
		{
			SNonAndroid.clearAWT();
		}
	}

	/**
	 *  Get the class loader.
	 */
	public ClassLoader getClassLoader()
	{
		return classloader;
	}
	
	/**
	 *  Find class directories on classpath.
	 */
	public static File[][]	findClassDirectories()
	{
		Set<File>	dirs	= new LinkedHashSet<File>();
		collectClasspathDirectories(ComponentTestSuite.class.getClassLoader(), dirs);
		File[][]	ret	= new File[dirs.size()][];
		
		int	i=0;
		for(File dir: dirs)
		{
			ret[i++]	= new File[]{dir};
		}
		return ret;
	}

	/**
	 *  Collect all directory URLs belonging to a class loader.
	 */
	protected static void	collectClasspathDirectories(ClassLoader classloader, Set<File> set)
	{
		assert classloader!=null;
		
		if(classloader.getParent()!=null)
		{
			collectClasspathDirectories(classloader.getParent(), set);
		}
		
		if(classloader instanceof URLClassLoader)
		{
			URL[] urls = ((URLClassLoader)classloader).getURLs();
			for(int i=0; i<urls.length; i++)
			{
				File	file	= SUtil.getFile(urls[i]);
				if(file.isDirectory())
				{
					set.add(file);
				}
			}
		}
	}
//
//	@Override
//	public void addTest(Test test)
//	{
//		System.out.println("Test added: "+test);
//		super.addTest(test);
//	}
}
