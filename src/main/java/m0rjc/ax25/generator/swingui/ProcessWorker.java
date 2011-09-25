package m0rjc.ax25.generator.swingui;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import m0rjc.ax25.generator.xmlDefinitionReader.XmlDefinitionLoader;

/**
 * SwingWorker to run the process.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class ProcessWorker extends SwingWorker<Boolean, LogRecord>
{
	/** We look for log messages within this package scope */
	public static final String APPLICATION_PACKAGE_ROOT = "m0rjc.ax25.generator";

	private String m_inputFile;
	
	private ProcessWorkerListener m_listener;
	
	private static boolean s_initialised;
	
	/**
	 * Construct the worker. It can only be used once.
	 *
	 * @param inputfileName file to process
	 * @param listener callback interface
	 */
	public ProcessWorker(String inputfileName, ProcessWorkerListener listener)
	{
		m_inputFile = inputfileName;
		m_listener = listener;
		
		// TODO: Consider a log framework that allows me to set this up on a per-task
		//       level. 
		if(!s_initialised)
		{
			s_initialised = true;
			Logger javaLogger = Logger.getLogger(APPLICATION_PACKAGE_ROOT);
//			javaLogger.setLevel(Level.FINE); // TODO: Capture all and filter on display
			javaLogger.addHandler(new Handler() {
				
				@Override
				public void publish(LogRecord record)
				{
					ProcessWorker.this.publish(record);
				}
				
				@Override
				public void flush()
				{
				}
				
				@Override
				public void close() throws SecurityException
				{
				}
			});
		}
	}
	
	@Override
	protected Boolean doInBackground() throws Exception
	{
		return new XmlDefinitionLoader().loadAndProcessDefinition(m_inputFile);
	}

	@Override
	protected void done()
	{
		m_listener.done(true);
	}

	@Override
	protected void process(List<LogRecord> chunks)
	{
		for(LogRecord entry : chunks)
		{
			m_listener.process(entry);
		}
	}
	
	

}
