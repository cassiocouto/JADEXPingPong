package jadex.transformation.jsonserializer.processors.write;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jadex.commons.SReflect;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;
import jadex.transformation.jsonserializer.JsonTraverser;

public class JsonLocalDateTimeProcessor implements ITraverseProcessor
{
	/**
	 *  Test if the processor is applicable.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return True, if is applicable. 
	 */
	public boolean isApplicable(Object object, Type type, boolean clone, ClassLoader targetcl)
	{
		Class<?> clazz = SReflect.getClass(type);
		boolean ret = (jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.CHRONOLOCALDATECLASS != null && SReflect.isSupertype(jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.CHRONOLOCALDATECLASS, clazz)) ||
					  (jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.LOCALTIMECLASS != null && SReflect.isSupertype(jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.LOCALTIMECLASS, clazz)) ||
					  (jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.CHRONOLOCALDATETIMECLASS != null && SReflect.isSupertype(jadex.transformation.jsonserializer.processors.read.JsonLocalDateTimeProcessor.CHRONOLOCALDATETIMECLASS, clazz));
		
		return ret;
	}
	
	/**
	 *  Process an object.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return The processed object.
	 */
	public Object process(Object object, Type type, List<ITraverseProcessor> processors, 
		Traverser traverser, Map<Object, Object> traversed, boolean clone, ClassLoader targetcl, Object context)
	{
		JsonWriteContext wr = (JsonWriteContext)context;
		
		wr.write("{");
		
		if(wr.isWriteClass())
		{
			wr.writeClass(object.getClass());
			wr.write(", ");
		}
		wr.writeNameString("value", object.toString());
		wr.write("}");
		
		return object;
	}
}
