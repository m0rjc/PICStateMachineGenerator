package m0rjc.ax25.generator.xmlDefinitionReader;

import javax.inject.Inject;

import m0rjc.ax25.generator.cdi.GeneratorRunScoped;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Outer-most SAX handler that reads the StateGeneratorRun element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@GeneratorRunScoped
class DefinitionSaxHandler extends ChainedSaxHandler
{
	@Inject
	private ModelSaxHandler m_modelHandler;
	
	@Inject
	private UnitTestSaxHandler m_testHandler;
	
	@Inject
	private OutputListSaxHandler m_outputHandler;
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if("Model".equals(localName))
		{
			startChild(m_modelHandler, uri, localName, qName, attributes);
		}
		else if("UnitTests".equals(localName))
		{
			startChild(m_testHandler, uri, localName, qName, attributes);
		}
		else if("Output".equals(localName))
		{
			startChild(m_outputHandler, uri, localName, qName, attributes);
		}
	}
}
