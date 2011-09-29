package m0rjc.ax25.generator.xmlDefinitionReader;

import javax.enterprise.inject.New;
import javax.inject.Inject;

import m0rjc.ax25.generator.cdi.GeneratorRunScoped;
import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * SAX handler to read a state:NodeDefinition element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@GeneratorRunScoped
class NodeSaxHandler extends ChainedSaxHandler
	implements ConditionListSaxHandler.Callback, CommandListSaxHandler.Callback, TransitionSaxHandler.Callback
{
	/** The model under construction */
	private StateModel m_model;
	/** The Node under construction */
	private Node m_currentNode;
	
	@Inject @New
	private ConditionListSaxHandler m_conditionListHandler;
	@Inject @New
	private CommandListSaxHandler m_commandListHandler;
	@Inject @New
	private TransitionSaxHandler m_transitionHandler;
	@Inject @New
	private ScriptSaxHandler m_scriptHandler;
	
	@Inject
	public NodeSaxHandler(StateModel model)
	{
		m_model = model;
		m_conditionListHandler.setCallbackHandler(this);
		m_commandListHandler.setCallbackHandler(this);
		m_transitionHandler.setCallbackHandler(this);
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
			startChild(m_conditionListHandler, uri, localName, qName, attributes);
		}
		else if("EntryCommands".equals(localName))
		{
			startChild(m_commandListHandler, uri, localName, qName, attributes);
		}
		else if("Transition".equals(localName))
		{
			startChild(m_transitionHandler, uri, localName, qName, attributes);
		}
		else if("Script".equals(localName))
		{
			m_scriptHandler.setCurrentNode(m_currentNode);
			startChild(m_scriptHandler, uri, localName, qName, attributes);
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
