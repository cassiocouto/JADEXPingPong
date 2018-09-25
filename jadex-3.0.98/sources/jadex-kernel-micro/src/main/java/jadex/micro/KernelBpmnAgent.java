package jadex.micro;

import jadex.bridge.nonfunctional.annotation.NameValue;
import jadex.bridge.service.types.factory.IComponentFactory;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.Properties;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

/**
 *  Bpmn kernel.
 */
// Hack!!! allows starting bpmn kernel when micro kernel is available, but avoids dependency from bpmn to micro
// and vice versa.
@Properties({@NameValue(name="system", value="true"), @NameValue(name="kernel.types", value="new String[]{\"bpmn\", \"bpmn2\"}")})
@ProvidedServices({
	@ProvidedService(type=IComponentFactory.class, implementation=@Implementation(
	expression="new jadex.bpmn.BpmnFactory($component, jadex.commons.SUtil.createHashMap("
	+		"new String[]"
	+		"{"
	+		"	\"debugger.panels\""
	+		"},"
	+		"new Object[]"
	+		"{"
	+		"	\"jadex.tools.debugger.bpmn.BpmnDebuggerPanel\""
	+		"})"
	+	")"))
})
@Agent
public class KernelBpmnAgent
{
}
