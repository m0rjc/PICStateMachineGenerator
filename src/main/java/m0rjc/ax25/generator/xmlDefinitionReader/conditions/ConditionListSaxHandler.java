package m0rjc.ax25.generator.xmlDefinitionReader.conditions;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Variable;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

/**
 * SAX Handler to handle a state:ConditionList
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@Dependent
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
	
	@Inject
	public ConditionListSaxHandler(StateModel model)
	{
		m_model = model;
	}
	
	public Callback getCallbackHandler()
	{
		return m_callbackHandler;
	}

	public void setCallbackHandler(Callback callbackHandler)
	{
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
