package jadex.transformation.jsonserializer.processors.read;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jadex.commons.SReflect;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;
import jadex.transformation.jsonserializer.JsonTraverser;

/**
 * 
 */
public class JsonCollectionProcessor implements ITraverseProcessor
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
		return (object instanceof JsonArray && SReflect.isSupertype(Collection.class, clazz) || 
			(object instanceof JsonObject && ((JsonObject)object).get(JsonTraverser.COLLECTION_MARKER)!=null));
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
		JsonArray array;
		JsonValue idx = null;
		Class<?> clazz = SReflect.getClass(type);
		Class<?> compclazz = SReflect.unwrapGenericType(type);
		if(((JsonValue)object).isArray())
		{
			array = (JsonArray)object;
		}
		else
		{
			JsonObject obj = (JsonObject)object;
//			compclazz = JsonTraverser.findClazzOfJsonObject(obj, targetcl);
			array = (JsonArray)obj.get(JsonTraverser.COLLECTION_MARKER);
			idx = (JsonValue)obj.get(JsonTraverser.ID_MARKER);
		}
		
		Collection ret = (Collection)getReturnObject(object, clazz);
//		traversed.put(object, ret);
//		((JsonReadContext)context).addKnownObject(ret);
		
		if(idx!=null)
			((JsonReadContext)context).addKnownObject(ret, idx.asInt());
		
		for(int i=0; i<array.size(); i++)
		{
			Object val = array.get(i);
			Object newval = traverser.doTraverse(val, compclazz!=null? compclazz: val.getClass(), traversed, processors, clone, targetcl, context);
			
			if(newval != Traverser.IGNORE_RESULT)
			{
				ret.add(newval);
			}
		}
		
		return ret;
	}

	/**
	 *  Get the return object.
	 */
	public Object getReturnObject(Object object, Class<?> clazz)
	{
		Object ret = object;
		
		try
		{
			ret = clazz.newInstance();
		}
		catch(Exception e)
		{
			if(SReflect.isSupertype(Set.class, clazz))
			{
				ret = new HashSet();
			}
			else
			{
				ret = new ArrayList();
			}
		}
		
		return ret;
	}
}
