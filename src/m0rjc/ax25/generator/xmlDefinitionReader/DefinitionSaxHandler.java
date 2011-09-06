package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Outer-most SAX handler that reads the StateGeneratorRun element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class DefinitionSaxHandler extends ChainedSaxHandler
{
	private ModelSaxHandler m_modelHandler = new ModelSaxHandler();
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if("Model".equals(localName))
		{
			setChild(m_modelHandler);
			m_modelHandler.startElement(uri, localName, qName, attributes);
		}
	}
	
}
