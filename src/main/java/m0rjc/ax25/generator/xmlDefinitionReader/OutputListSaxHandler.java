package m0rjc.ax25.generator.xmlDefinitionReader;

import java.util.logging.Logger;

import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.picAsmBuilder.Pic18AsmBuilder;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler for the state:OutputList element.
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class OutputListSaxHandler extends ChainedSaxHandler
{
	private final PicOutputSaxHandler m_picOutputHandler;
	private final IgnoreBranchSaxHandler m_ignoreHandler = new IgnoreBranchSaxHandler();
	
	private Logger m_log = Logger.getLogger(OutputListSaxHandler.class.getName());
	
	
	public OutputListSaxHandler(StateModel model)
	{
		m_picOutputHandler = new PicOutputSaxHandler(model);
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
		}
		else if("Pic18".equals(localName))
		{
			m_picOutputHandler.setBuilder(new Pic18AsmBuilder());
			setChild(m_picOutputHandler);
			m_picOutputHandler.startElement(uri, localName, qName, attributes);
		}
		else
		{
			m_log.warning("Output " + localName + " not recognised.");
			setChild(m_ignoreHandler);
			m_ignoreHandler.startElement(uri, localName, qName, attributes);
		}
	}
}
