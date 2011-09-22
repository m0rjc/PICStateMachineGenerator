package m0rjc.ax25.generator.xmlDefinitionReader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlDefinitionLoader
{
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

	private Schema getSchema() throws SAXException
	{
		String schemaLang = "http://www.w3.org/2001/XMLSchema";
		SchemaFactory jaxp = SchemaFactory.newInstance(schemaLang);
		Schema schema = jaxp.newSchema(new StreamSource(XmlDefinitionLoader.class.getResourceAsStream("stategenerator.xsd")));
		return schema;
	}
}
