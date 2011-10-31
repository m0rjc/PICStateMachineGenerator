package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import uk.me.m0rjc.picstategenerator.model.Command;
import uk.me.m0rjc.picstategenerator.model.GosubCommand;
import uk.me.m0rjc.picstategenerator.model.ReturnFromSubroutineCommand;
import uk.me.m0rjc.picstategenerator.model.RomLocation;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.Variable;


/**
 * SAX Handler to handle a state:CommandList
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class CommandListSaxHandler extends ChainedSaxHandler
{
	private static final String COMMAND_CLEAR_INDEXED_VALUE = "ClearIndexedValue";
	private static final String COMMAND_STORE_VALUE = "StoreValue";
	private static final String COMMAND_SET_FLAG = "SetFlag";
	private static final String COMMAND_RETURN = "Return";
	private static final String COMMAND_GO_SUB = "GoSub";
	private static final String COMMAND_CALL = "Call";
	private static final String COMMAND_CLEAR_VALUE = "ClearValue";

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
		else if(COMMAND_CLEAR_VALUE.equals(localName))
		{
			startReadingText();
		}
		else if(COMMAND_CLEAR_INDEXED_VALUE.equals(localName))
		{
			String variable = getString(attributes, "variable");
			String indexer = getString(attributes, "indexer");
			newCommand(Command.clearIndexedValue(getVariable(variable), getVariable(indexer)));
		}
		else if(COMMAND_STORE_VALUE.equals(localName))
		{
			String sourceName = attributes.getValue("source");
			Variable source = (sourceName != null ? getVariable(sourceName) : m_model.getInputVariable());
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
		else if(COMMAND_SET_FLAG.equals(localName))
		{
			Variable v = getVariable(getString(attributes, "variable"));
			String flag = getString(attributes,"flag");
			boolean value = getBoolean(attributes,"value");
			newCommand(Command.setFlag(v, flag, value));
		}
		else if(COMMAND_CALL.equals(localName))
		{
			startReadingText();
		}
		else if(COMMAND_GO_SUB.equals(localName))
		{
			startReadingText();
		}
		else if(COMMAND_RETURN.equals(localName))
		{
			// Handle on end of node.
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
		if(COMMAND_CLEAR_VALUE.equals(localName))
		{
			Variable v = getVariable(finishReadingText());
			newCommand(Command.clearValue(v));
		}
		else if(COMMAND_CALL.equals(localName))
		{
			RomLocation method = getRomLocation(finishReadingText());
			newCommand(Command.call(method));
		}
		else if(COMMAND_GO_SUB.equals(localName))
		{
			String state = finishReadingText();
			newCommand(new GosubCommand(state));
		}
		else if(COMMAND_RETURN.equals(localName))
		{
			newCommand(new ReturnFromSubroutineCommand());
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
