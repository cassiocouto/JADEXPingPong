package jadex.transformation.jsonserializer.processors.write;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

/**
 *  Processor for java util date.
 */ 
public class JsonDateProcessor implements ITraverseProcessor
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
		// Timestamp is handled separately
		return SReflect.isSupertype(Date.class, clazz) && !SReflect.isSupertype(Timestamp.class, clazz);
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
		wr.addObject(traversed, object);
	
		Date d  = (Date)object;
		
		if(!wr.isWriteClass() && !wr.isWriteId())
		{
			wr.writeString(SUtil.dateToIso8601(d));
		}
		else
		{
			wr.write("{");
			wr.writeNameValue("value", d.getTime());
			if(wr.isWriteClass())
				wr.write(",").writeClass(object.getClass());
			if(wr.isWriteId())
				wr.write(",").writeId();
			wr.write("}");
		}
	
		return object;
	}
}

