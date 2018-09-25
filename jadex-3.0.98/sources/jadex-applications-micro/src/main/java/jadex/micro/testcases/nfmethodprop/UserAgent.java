package jadex.micro.testcases.nfmethodprop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IArgumentsResultsFeature;
import jadex.bridge.nonfunctional.SNFPropertyProvider;
import jadex.bridge.sensor.service.ExecutionTimeProperty;
import jadex.bridge.service.IService;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.commons.MethodInfo;
import jadex.commons.SReflect;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.ComponentType;
import jadex.micro.annotation.ComponentTypes;
import jadex.micro.annotation.CreationInfo;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

/**
 *  Tests waitqueue and execution time non-functional properties on 
 *  provided services.
 */
@Agent
@Service
@RequiredServices(@RequiredService(name="testser", type=ITestService.class, 
	binding=@Binding(create=true, creationinfo=@CreationInfo(type="provider"))))
@ComponentTypes(@ComponentType(name="provider", filename="jadex.micro.testcases.nfmethodprop.ProviderAgent.class"))
@Results(@Result(name="testresults", description= "The test results.", clazz=Testcase.class))
public class UserAgent
{
	/** The agent. */
	@Agent
	protected IInternalAccess agent;
	
	/**
	 *  The agent body. 
	 */
	@AgentBody
	public void body()
	{
		ITestService ser = (ITestService)agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("testser").get();
		
		final List<TestReport> results = new ArrayList<TestReport>();
		final long wa = SReflect.isAndroid() ? 5000 : 500;
		final long wb = SReflect.isAndroid() ? 10000 : 1000;
		
		for(int i=0; i<5; i++)
		{
			ser.methodA(wa).get();
			ser.methodB(wb).get();
		}
		
		try
		{
			TestReport tr1 = new TestReport("#1", "Test if wait time of method a is ok");
			results.add(tr1);
			Method ma = ser.getClass().getMethod("methodA", new Class[]{long.class});
//			INFMixedPropertyProvider prov = (INFMixedPropertyProvider)((IService)ser).getExternalComponentFeature(INFPropertyComponentFeature.class);
			double w = ((Long)SNFPropertyProvider.getMethodNFPropertyValue(agent.getExternalAccess(), ((IService)ser).getServiceIdentifier(), new MethodInfo(ma), ExecutionTimeProperty.NAME).get()).doubleValue();
//			double w = ((Long)((IService)ser).getMethodNFPropertyValue(new MethodInfo(ma), ExecutionTimeProperty.NAME).get()).doubleValue();
			double d = Math.abs(w-wa)/wa;
			if(d<0.15)
			{
				tr1.setSucceeded(true);
			}
			else
			{
				tr1.setReason("Value differs more than 15 percent: "+d+" "+w+" "+wa);
			}
			
			TestReport tr2 = new TestReport("#2", "Test if wait time of method b is ok");
			results.add(tr2);
			Method mb = ser.getClass().getMethod("methodB", new Class[]{long.class});
			w = ((Long)SNFPropertyProvider.getMethodNFPropertyValue(agent.getExternalAccess(), ((IService)ser).getServiceIdentifier(), new MethodInfo(mb), ExecutionTimeProperty.NAME).get()).doubleValue();
//			w = ((Long)((IService)ser).getMethodNFPropertyValue(new MethodInfo(mb), ExecutionTimeProperty.NAME).get()).doubleValue();
			d = Math.abs(w-wb)/wb;
			if(d<0.15)
			{
				tr2.setSucceeded(true);
			}
			else
			{
				tr2.setReason("Value differs more than 15 percent: "+d+" "+w+" "+wb);
			}
			
			TestReport tr3 = new TestReport("#3", "Test if wait time of service is ok");
			results.add(tr3);
//			w = ((Long)((IService)ser).getNFPropertyValue(ExecutionTimeProperty.NAME).get()).doubleValue();
			w = ((Long)SNFPropertyProvider.getNFPropertyValue(agent.getExternalAccess(), ((IService)ser).getServiceIdentifier(), ExecutionTimeProperty.NAME).get()).doubleValue();
			long wab = (wa+wb)/2;
			d = Math.abs(w-wab)/wab;
			if(d<0.15)
			{
				tr3.setSucceeded(true);
			}
			else
			{
				tr3.setReason("Value differs more than 15 percent: "+d+" "+w+" "+wab);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		agent.getComponentFeature(IArgumentsResultsFeature.class).getResults().put("testresults", new Testcase(results.size(), 
			(TestReport[])results.toArray(new TestReport[results.size()])));
		agent.killComponent();
	}
}

