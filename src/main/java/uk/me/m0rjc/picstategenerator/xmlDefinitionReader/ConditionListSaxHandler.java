package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.Variable;


/**
 * SAX Handler to handle a state:ConditionList
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class ConditionListSaxHandler extends ChainedSaxHandler
{
	/**
	 * Callback interface for Condition creation.
	 * @author Richard Corfield <m0rjc@raynet-uk.net>
	 */
	public interface Callback
	{
		/** Callback to create a new Precondition and attach it to wherever it needs to go. */
		void onNewCondition(Precondition condition) throws SAXException;
	}
	
	private Callback m_callbackHandler;
	private StateModel m_model;
	
	
	public ConditionListSaxHandler(StateModel model, Callback callbackHandler)
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
		else if("FlagCheck".equals(localName))
		{
			onFlagCheck(attributes);
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
	}

	/**
	 * Create a Flag Check condition
	 * @param attributes
	 * @throws SAXException 
	 */
	private void onFlagCheck(Attributes attributes) throws SAXException
	{
		String variableName = getString(attributes, "variable");
		String flag = getString(attributes, "flag");
		boolean value = getBoolean(attributes, "value");
		
		Variable v = m_model.getVariable(variableName);
		if(v == null) throw new SAXException("Variable " + variableName + " not defined");
		
		m_callbackHandler.onNewCondition(Precondition.checkFlag(v, flag, value));
	}
	
	
}
