package m0rjc.ax25.generator.cdi;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

/**
 * Component to process an input file.
 */
public interface BatchProcessor
{
	/**
	 * Load the given file and process it.
	 *
	 * @param fileName
	 * @return
	 */
	boolean loadAndProcessDefinition(String fileName);

	/**
	 * Read the given input and process it.
	 * 
	 * @param in
	 * @throws SAXException
	 * @throws IOException
	 */
	void loadAndProcessDefinition(InputStream in)
		throws SAXException, IOException;

}