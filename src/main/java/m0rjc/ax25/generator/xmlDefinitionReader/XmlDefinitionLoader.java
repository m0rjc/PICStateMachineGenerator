package m0rjc.ax25.generator.xmlDefinitionReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entry point for the handler of the XML definition file.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class XmlDefinitionLoader
{
	private static final Logger s_log = Logger.getLogger(XmlDefinitionLoader.class.getName());

	/**
	 * Load the given file and process it.
	 *
	 * @param fileName
	 * @return
	 */
	public boolean loadAndProcessDefinition(String fileName)
	{
		try
		{
			InputStream in = new FileInputStream(fileName);
			new XmlDefinitionLoader().loadAndProcessDefinition(in);
			return true;
		}
		catch (FileNotFoundException e)
		{
			s_log.severe(String.format("Input file '%s' not found", fileName));
		}
		catch (SAXException e)
		{
			s_log.severe(String.format("Error reading file '%s': %s", fileName, e.getMessage()));
		}
		catch (IOException e)
		{
			s_log.severe(String.format("Error reading file '%s': %s", fileName, e.getMessage()));
		}
		return false;
	}
	
	/**
	 * Read the given input and process it.
	 * 
	 * @param in
	 * @throws SAXException
	 * @throws IOException
	 */
	public void loadAndProcessDefinition(InputStream in) throws SAXException, IOException
	{
		Schema schema = getSchema();
		Validator validator = schema.newValidator();

		// prepare SAX handler and SAX result receiving validate data:
		DefinitionSaxHandler handler = new DefinitionSaxHandler();
		SAXResult sax = new SAXResult(handler);

		// at last send valid data to out SAX handler:
		SAXSource source = new SAXSource(new InputSource(in));
		validator.validate(source, sax);
	}

	/**
	 * Return the Schema for the XML definition file.
	 * @return
	 * @throws SAXException
	 */
	private Schema getSchema() throws SAXException
	{
		String schemaLang = "http://www.w3.org/2001/XMLSchema";
		SchemaFactory jaxp = SchemaFactory.newInstance(schemaLang);
		Schema schema = jaxp.newSchema(new StreamSource(XmlDefinitionLoader.class.getResourceAsStream("stategenerator.xsd")));
		return schema;
	}
}
