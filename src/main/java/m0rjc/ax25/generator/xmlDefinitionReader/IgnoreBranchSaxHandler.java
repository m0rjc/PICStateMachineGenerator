package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A Sax Handler that just ignores the branch it's in
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class IgnoreBranchSaxHandler extends ChainedSaxHandler
{
	@Override
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		// So that any text does not cause an error
		startReadingText();
	}
}
