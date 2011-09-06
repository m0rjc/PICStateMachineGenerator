package m0rjc.ax25.generator.xmlDefinitionReader;

import m0rjc.ax25.generator.model.StateModel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler for the Model element
 */
public class StateModelSaxHandler extends DefaultHandler implements ChainedSaxHandlerListener
{
	private DefaultHandler m_child;
	private StateModel m_model;
	
	@Override
	public void childReturned()
	{
		m_child = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException
	{
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException
	{
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
	}

	
}
