package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import uk.me.m0rjc.picstategenerator.model.Command;
import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.Transition;

/**
 * SAX handler to read a state:NodeDefinition element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class NodeSaxHandler extends ChainedSaxHandler
	implements ConditionListSaxHandler.Callback, CommandListSaxHandler.Callback, TransitionSaxHandler.Callback
{
	/** The model under construction */
	private StateModel m_model;
	/** The Node under construction */
	private Node m_currentNode;
	
	private final ConditionListSaxHandler m_conditionListHandler;
	private final CommandListSaxHandler m_commandListHandler;
	private final TransitionSaxHandler m_transitionHandler;
	private final ScriptSaxHandler m_scriptHandler;
	
	public NodeSaxHandler(StateModel model)
	{
		m_model = model;
		m_conditionListHandler = new ConditionListSaxHandler(model, this);
		m_commandListHandler = new CommandListSaxHandler(model, this);
		m_transitionHandler = new TransitionSaxHandler(model, this);
		m_scriptHandler = new ScriptSaxHandler(model);
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			// The start of this Node element
			String name = getString(attributes, "name");
			m_currentNode = m_model.createNamedNode(name);
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
		else if("Script".equals(localName))
		{
			m_scriptHandler.setCurrentNode(m_currentNode);
			setChild(m_scriptHandler);
			m_scriptHandler.startElement(uri, localName, qName, attributes);
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
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

	@Override
	public void onNewTransition(Transition t) throws SAXException
	{
		m_currentNode.addTransition(t);
	}
}
