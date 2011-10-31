package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

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
	private UnitTestSaxHandler m_testHandler = new UnitTestSaxHandler();
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if("Model".equals(localName))
		{
			setChild(m_modelHandler);
			m_modelHandler.startElement(uri, localName, qName, attributes);
		}
		else if("UnitTests".equals(localName))
		{
			m_testHandler.setModel(m_modelHandler.getModel());
			setChild(m_testHandler);
			m_testHandler.startElement(uri, localName, qName, attributes);
		}
		else if("Output".equals(localName))
		{
			OutputListSaxHandler handler = new OutputListSaxHandler(m_modelHandler.getModel());
			setChild(handler);
			handler.startElement(uri, localName, qName, attributes);
		}
	}
	
}
