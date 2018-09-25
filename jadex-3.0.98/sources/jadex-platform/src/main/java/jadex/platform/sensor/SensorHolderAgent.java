package jadex.platform.sensor;

import jadex.bridge.nonfunctional.annotation.NameValue;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Component;
import jadex.micro.annotation.ComponentType;
import jadex.micro.annotation.ComponentTypes;
import jadex.micro.annotation.Configuration;
import jadex.micro.annotation.Configurations;
import jadex.micro.annotation.Properties;

/**
 *  Component that holds the sensors in the platform.
 */
@Agent
@ComponentTypes(
{
	@ComponentType(name="cpusensor", filename="jadex/platform/sensor/system/SystemSensorAgent.class")
})
@Configurations(@Configuration(name="def", components=
{
	@Component(type="cpusensor")
}))
@Properties(@NameValue(name="system", value="true"))
public class SensorHolderAgent
{
}
