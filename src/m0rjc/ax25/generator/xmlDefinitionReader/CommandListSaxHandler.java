package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.RomLocation;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Variable;

/**
 * SAX Handler to handle a state:CommandList
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class CommandListSaxHandler extends ChainedSaxHandler
{
	/**
	 * Callback interface for command creation
	 * @author Richard Corfield <m0rjc@raynet-uk.net>
	 */
	public interface Callback
	{
		void onNewCommand(Command command) throws SAXException;
	}
	
	private Callback m_callbackHandler;
	private StateModel m_model;

	public CommandListSaxHandler(StateModel model, Callback callbackHandler)
	{
		m_model = model;
		m_callbackHandler = callbackHandler;
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			// Nothing
		}
		else if("ClearValue".equals(localName))
		{
			startReadingText();
		}
		else if("ClearIndexedValue".equals(localName))
		{
			String variable = getString(attributes, "variable");
			String indexer = getString(attributes, "indexer");
			newCommand(Command.clearIndexedValue(getVariable(variable), getVariable(indexer)));
		}
		else if("StoreValue".equals(localName))
		{
			Variable source = getVariable(getString(attributes, "source"));
			Variable destination = getVariable(getString(attributes, "destination"));
			String indexer = attributes.getValue("destinationIndexer");
			if(indexer != null)
			{
				newCommand(Command.storeValueIndex(source, destination, getVariable(indexer)));
			}
			else
			{
				newCommand(Command.storeValue(source, destination));
			}
		}
		else if("SetFlag".equals(localName))
		{
			Variable v = getVariable(getString(attributes, "variable"));
			String flag = getString(attributes,"flag");
			boolean value = getBoolean(attributes,"value");
			newCommand(Command.setFlag(v, flag, value));
		}
		else if("Call".equals(localName))
		{
			startReadingText();
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if("ClearValue".equals(localName))
		{
			Variable v = getVariable(finishReadingText());
			newCommand(Command.clearValue(v));
		}
		else if("Call".equals(localName))
		{
			RomLocation method = getRomLocation(finishReadingText());
			newCommand(Command.call(method));
		}
	}
	
	/**
	 * Get a ROM location by name. Throw an exception if not defined.
	 * @param name
	 * @return
	 * @throws SAXException 
	 */
	private RomLocation getRomLocation(String name) throws SAXException
	{
		RomLocation result = m_model.getRomLocation(name);
		if(result == null)
		{
			throw new SAXException("ROM symbol '" + name + "' not defined.");
		}
		return result;
	}


	private void newCommand(Command c) throws SAXException
	{
		m_callbackHandler.onNewCommand(c);
	}
	
	private Variable getVariable(String name) throws SAXException
	{
		Variable v = m_model.getVariable(name);
		if(v == null) throw new SAXException("Variable '" + v + "' not defined.");
		return v;
	}

}
