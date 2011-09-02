package m0rjc.ax25.generator.simulatorBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shorthand for logging for the simulator
 */
class Log
{
	private static Logger s_logger = Logger.getLogger(Log.class.getName());

	static void fine(String msg)
	{
		s_logger.fine(msg);
	}

	static void finer(String msg)
	{
		s_logger.finer(msg);
	}

	public static void finest(String msg)
	{
		s_logger.finest(msg);
	}

	static void info(String msg)
	{
		s_logger.info(msg);
	}

	static void log(Level level, String msg, Throwable thrown)
	{
		s_logger.log(level, msg, thrown);
	}

	static void severe(String msg)
	{
		s_logger.severe(msg);
	}

	static void warning(String msg)
	{
		s_logger.warning(msg);
	}
	
	/** Return the input byte formatted as a character or number */
	static String formatByte(byte b)
	{
		if(b >= 0x20 && b <= 0x7E)
		{
			return "'" + Character.toString((char)b) + "'"; 
		}
		return String.format("0x%02X",b);
	}
}
