package m0rjc.ax25.generator.swingui;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Format logs from the unit test run in a slightly more readable way
 */
class ProcessLogFormatter extends Formatter
{
	@Override
	public String format(LogRecord record)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPrefix(record));
		sb.append(formatMessage(record));
		return sb.toString();
	}

	private String getPrefix(LogRecord record)
	{
		Level level = record.getLevel();
		if(Level.SEVERE.equals(level)) return  " SEVERE: "; 
		if(Level.WARNING.equals(level)) return "WARNING: "; 
		if(Level.INFO.equals(level)) return    "   INFO: "; 
		if(Level.FINE.equals(level)) return    "  debug: ";
		if(Level.FINER.equals(level)) return   "  debug: .   "; 
		if(Level.FINEST.equals(level)) return  "  debug: .   .   ";
		return                                 "  ?????: ";
	}

}
