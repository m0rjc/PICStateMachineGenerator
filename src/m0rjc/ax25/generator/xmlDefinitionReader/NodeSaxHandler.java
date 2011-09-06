package m0rjc.ax25.generator.xmlDefinitionReader;

import java.util.concurrent.locks.Condition;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;

/**
 * SAX handler to read a state:NodeDefinition element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class NodeSaxHandler extends ChainedSaxHandler
	implements ConditionListSaxHandler.Callback, CommandListSaxHandler.Callback
{
	/** The model under construction */
	private StateModel m_model;
	/** The Node under construction */
	private Node m_currentNode;
	
	private ConditionListSaxHandler m_conditionListHandler;
	private CommandListSaxHandler m_commandListHandler;
	private TransitionSaxHandler m_transitionHandler;
	private ScriptSaxHandler m_scriptHandler;
	
	public NodeSaxHandler(StateModel model)
	{
		m_model = model;
		m_conditionListHandler = new ConditionListSaxHandler(model, this);
		m_commandListHandler = new CommandListSaxHandler(model, this);
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			// The start of this Node element
			onNode(attributes);
		}
		else if("EntryConditions".equals(localName))
		{
			setChild(m_conditionListHandler);
			m_conditionListHandler.startElement(uri, localName, qName, attributes);
		}
		else if("EntryCommands".equals(localName))
		{
			setChild(m_commandListHandler);
			m_commandListHandler.startElement(uri, localName, qName, attributes);
		}
		else if("Transition".equals(localName))
		{
			setChild(m_transitionHandler);
			m_transitionHandler.startElement(uri, localName, qName, attributes);
		}
		else if("Script".eqauls(localName))
		{
			setChild(m_scriptHandler);
			m_scriptHandler.startElement(uri, localName, qName, attributes);
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
	}

	/** Start of the Element */
	private void onNode(Attributes attributes)
	{
		String name = attributes.getValue("name");
		m_currentNode = m_model.createNamedNode(name);
	}

	@Override
	public void onNewCommand(Command command) throws SAXException
	{
		m_currentNode.addEntryCommand(command);
	}

	@Override
	public void onNewCondition(Precondition condition) throws SAXException
	{
		m_currentNode.addEntryCondition(condition);
	}
	
	
}
