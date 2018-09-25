package jadex.commons;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *  Helper class to remember stack traces.
 *  Prints out a warning, when used.
 */
public class DebugException extends RuntimeException
{
	//-------- static part --------
	
	/**
	 *  Print out warning, when class is loaded.
	 */
	static
	{
		System.err.println("Warning: Using debug exceptions.");
	}
	
	//-------- attributes --------
	
	/** The stack trace. */
//	protected StackTraceElement[] stacktrace;
	
	//-------- constructors --------
	
	/**
	 * 	Create a debug exception.
	 */
	public DebugException()
	{
		fillInStackTrace();
	}
	
	/**
	 * 	Create a debug exception.
	 */
	public DebugException(String msg)
	{
		super(msg);
		fillInStackTrace();
	}
	
//	public void printStackTrace()
//	{
//		Thread.dumpStack();
//		super.printStackTrace();
//	}
	
	public synchronized Throwable fillInStackTrace()
	{
		Throwable throwable = super.fillInStackTrace();
		// when getStackTrace() is called, super saves the stacktrace elements, so we don't have to.
		StackTraceElement[] stackTrace = getStackTrace();
		return throwable;
	}
}
