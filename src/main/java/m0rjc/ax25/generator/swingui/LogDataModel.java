package m0rjc.ax25.generator.swingui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.table.AbstractTableModel;

/**
 * Present the log to Swing as a Table Model
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class LogDataModel extends AbstractTableModel
{
	/** Prefix to remove from log source names */
	private static final String LOG_SOURCE_PREFIX = ProcessWorker.APPLICATION_PACKAGE_ROOT + ".";

	/** A message for display */
	public static class Message
	{
		private static final ProcessLogFormatter m_formatter = new ProcessLogFormatter();
		
		private LogRecord m_record;

		private Message(LogRecord record)
		{
			m_record = record;
		}

		public Level getLevel()
		{
			return m_record.getLevel();
		}

		public String getText()
		{
			return m_formatter.formatMessage(m_record);
		}

		@Override
		public String toString()
		{
			return m_formatter.format(m_record);
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	public static final int COL_LEVEL = 0;
	public static final int COL_LOGNAME = 1;
	public static final int COL_MESSAGE = 2;
	public static final int COL_EXCEPTION = 3;
	
	private List<LogRecord> m_records = new ArrayList<LogRecord>();
	
	@Override
	public int getColumnCount()
	{
		return 4;
	}

	@Override
	public int getRowCount()
	{
		return m_records.size();
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		switch(columnIndex)
		{
		case COL_LEVEL:
			return "Level";
		case COL_LOGNAME:
			return "Source";
		case COL_MESSAGE:
			return "Message";
		case COL_EXCEPTION:
			return "Exception";
		}
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch(columnIndex)
		{
		case COL_LEVEL:
			return Level.class;
		case COL_LOGNAME:
			return String.class;
		case COL_MESSAGE:
			return Message.class;
		case COL_EXCEPTION:
			return Throwable.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		LogRecord record = m_records.get(rowIndex);
		switch(columnIndex)
		{
		case COL_LEVEL:
			return record.getLevel();
		case COL_LOGNAME:
			return formatLoggerName(record);
		case COL_MESSAGE:
			return formatMessage(record);
		case COL_EXCEPTION:
			return record.getThrown();
		}
		return null;
	}

	/**
	 * Remvoe the common prefix from the logger name.
	 * @param record
	 * @return
	 */
	private String formatLoggerName(LogRecord record)
	{
		String loggerName = record.getLoggerName();
		if(loggerName.startsWith(LOG_SOURCE_PREFIX))
		{
			loggerName = loggerName.substring(LOG_SOURCE_PREFIX.length());
		}
		return loggerName;
	}

	/**
	 * Produce a Message for display
	 * @param record
	 * @return
	 */
	private Message formatMessage(LogRecord record)
	{
		return new Message(record);
	}

	/**
	 * Clear the table to start the log.
	 */
	public void startLog()
	{
		int lastRow = m_records.size() - 1;
		if(lastRow >= 0)
		{
			m_records.clear();
			fireTableRowsDeleted(0, lastRow);
		}
	}
	
	/**
	 * Add a record to the log
	 * @param entry
	 */
	public void addRecord(LogRecord entry)
	{
		int newIndex = m_records.size();
		m_records.add(entry);
		fireTableRowsInserted(newIndex, newIndex);
	}

}
