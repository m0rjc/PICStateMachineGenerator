package m0rjc.ax25.generator.xmlDefinitionReader;

import java.util.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import m0rjc.ax25.generator.cdi.OutputTypeSaxHandler;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.IgnoreBranchSaxHandler;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.NamedTagHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler for the state:OutputList element.
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class OutputListSaxHandler extends ChainedSaxHandler
{
	@Inject
	private IgnoreBranchSaxHandler m_ignoreHandler;
	
	@Inject
	private Logger m_log;

	@Inject
	public OutputListSaxHandler(@OutputTypeSaxHandler Instance<NamedTagHandler> outputHandlers)
	{
		registerNamedHandlers(outputHandlers);
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if(!isHandlingOuterElement())
		{
			m_log.warning("Output " + localName + " not recognised.");
			startChild(m_ignoreHandler, uri, localName, qName, attributes);
		}
	}
}
