package uk.me.m0rjc.picstategenerator.swingui;

import java.util.logging.LogRecord;

/**
 * Completion callback for the ProcessWorkerListener
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
interface ProcessWorkerListener
{
	/**
	 * The process has completed.
	 * This method will be called on the Swing UI thread.
	 * @param succeeded was it successful?
	 */
	void done(boolean succeeded);

	/**
	 * Process a log record from the background process.
	 * This method will be called on the Swing UI thread.
	 */
	void process(LogRecord entry);
}
