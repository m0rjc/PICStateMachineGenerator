package m0rjc.ax25.generator.xmlDefinitionReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import m0rjc.ax25.generator.cdi.BatchProcessor;
import m0rjc.ax25.generator.cdi.GeneratorRunContext;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entry point for the handler of the XML definition file.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class XmlDefinitionLoader implements BatchProcessor
{
	@Inject
	private Logger m_logger;
	
	@Inject
	private GeneratorRunContext m_runContext;
	
	@Inject
	private DefinitionSaxHandler m_saxHandler;
	
	/* (non-Javadoc)
	 * @see m0rjc.ax25.generator.xmlDefinitionReader.InputFileProcessor#loadAndProcessDefinition(java.lang.String)
	 */
	@Override
	public boolean loadAndProcessDefinition(String fileName)
	{
		try
		{
			InputStream in = new FileInputStream(fileName);
			loadAndProcessDefinition(in);
			return true;
		}
		catch (FileNotFoundException e)
		{
			m_logger.severe(String.format("Input file '%s' not found", fileName));
		}
		catch (SAXException e)
		{
			m_logger.severe(String.format("Error reading file '%s': %s", fileName, e.getMessage()));
		}
		catch (IOException e)
		{
			m_logger.severe(String.format("Error reading file '%s': %s", fileName, e.getMessage()));
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see m0rjc.ax25.generator.xmlDefinitionReader.InputFileProcessor#loadAndProcessDefinition(java.io.InputStream)
	 */
	@Override
	public void loadAndProcessDefinition(InputStream in) throws SAXException, IOException
	{
		m_runContext.activate();
		try
		{
			Schema schema = getSchema();
			Validator validator = schema.newValidator();
	
			// prepare SAX handler and SAX result receiving validate data:
			SAXResult sax = new SAXResult(m_saxHandler);
	
			// at last send valid data to out SAX handler:
			SAXSource source = new SAXSource(new InputSource(in));
			validator.validate(source, sax);
		}
		finally
		{
			m_runContext.deactivate();
		}
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
