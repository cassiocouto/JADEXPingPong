package jadex.commons.transformation.binaryserializer;

import java.util.List;
import java.util.Map;

/**
 *  Decoding context interface.
 *
 */
public interface IDecodingContext
{
	/**
	 *  Gets the current class name.
	 *  @return The current class name.
	 */
	public String getCurrentClassName();
	
	/**
	 *  Returns the last object decoded.
	 *  @return The last object decoded.
	 */
	public Object getLastObject();
	
	/**
	 *  Sets the last object decoded.
	 *  @param lastobject The last object decoded.
	 */
	public void setLastObject(Object lastobject);
	
	/**
	 *  Returns the known objects.
	 *  @return Known objects.
	 */
	public Map<Integer, Object> getKnownObjects();
	
	/**
	 * Gets the classloader.
	 * @return The classloader.
	 */
	public ClassLoader getClassloader();
	
	/**
	 *  Returns the handlers used to decode objects.
	 *  
	 *  @return The handlers.
	 */
	public List<IDecoderHandler> getDecoderHandlers();
	
	/**
	 *  Gets the error reporter.
	 *  @return The error reporter.
	 */
	public IErrorReporter getErrorReporter();
	
	/**
	 *  Returns the handlers used for post-processing.
	 *  @return Post-processing handlers.
	 */
	public List<IDecoderHandler> getPostProcessors();
	
	/**
	 *  Reads a byte from the buffer.
	 *  
	 *  @return A byte.
	 */
	public byte readByte();
	
	/**
	 *  Reads a number of bytes from the buffer and fills the array.
	 *  
	 *  @param array The byte array.
	 *  @return The byte array for convenience.
	 */
	public byte[] read(byte[] array);
	
	/**
	 *  Reads a boolean value from the buffer.
	 *  @return Boolean value.
	 */
	public boolean readBoolean();
	
	/**
	 *  Helper method for decoding a string.
	 *  @return String encoded at the current position.
	 */
	public String readString();
	
	/**
	 *  Helper method for decoding a variable-sized integer (VarInt).
	 *  @return The decoded value.
	 */
	public long readVarInt();
	
	/**
	 *  Helper method for decoding a signed variable-sized integer (VarInt).
	 *  @return The decoded value.
	 */
	public long readSignedVarInt();
	
	/**
	 *  Helper method for decoding a class name.
	 *  @return String encoded at the current position.
	 */
	public String readClassname();
	
	/**
	 *  Returns the current offset of the decoding process for debugging.
	 *  @return Current offset.
	 */
	public int getCurrentOffset();
}
