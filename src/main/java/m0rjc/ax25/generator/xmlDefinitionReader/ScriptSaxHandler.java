package m0rjc.ax25.generator.xmlDefinitionReader;

import javax.enterprise.inject.New;
import javax.inject.Inject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.model.Variable;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

/**
 * Handler for the state:ScriptDefinition element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class ScriptSaxHandler extends ChainedSaxHandler
	implements TransitionSaxHandler.Callback, CommandListSaxHandler.Callback, ConditionListSaxHandler.Callback
{
	private final StateModel m_model;
	
	@Inject @New
	private CommandListSaxHandler m_commandHandler;
	@Inject @New
	private ConditionListSaxHandler m_conditionHandler;
	@Inject @New
	private TransitionSaxHandler m_choicesHandler;

	private Node m_currentNode;
	/** Next node - following Choices */
	private Node m_nextNode;

	@Inject
	public ScriptSaxHandler(StateModel model)
	{
		m_model = model;
		m_choicesHandler.setCallbackHandler(this);
		m_commandHandler.setCallbackHandler(this);
		m_conditionHandler.setCallbackHandler(this);
	}

	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if("GuardCondition".equals(localName))
		{
			// The guard condition applies to the "current node". This is the node that is entered
			// as a result of whatever transition we've just scripted, and the node that whatever
			// we script next will come out of. A guard condition guards entry to this node.
			startChild(m_conditionHandler, uri, localName, qName, attributes);
		}
		else if("Commands".equals(localName))
		{
			startChild(m_commandHandler, uri, localName, qName, attributes);
		}
		else if("Literal".equals(localName))
		{
			startReadingText();
		}
		else if("Choices".equals(localName) || "SkipTo".equals(localName))
		{
			onStartChoices(uri, localName, qName, attributes);
		}
		else if("Numbers".equals(localName))
		{
			onNumbers(attributes);
		}
	}

	/** Start reading Choices on the node. */
	public void onStartChoices(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		// It doesn't matter if the new node is orphaned. The model will contain it,
		// but it will not be built.
		m_nextNode = m_model.createNode();
		m_choicesHandler.setDefaultTarget(m_nextNode.getStateName());
		startChild(m_choicesHandler, uri, localName, qName, attributes);
	}

	/** Add nodes to read a series of numbers. */
	private void onNumbers(Attributes attributes) throws SAXException
	{
		int min = getInt(attributes, "min");
		int max = getInt(attributes, "max");
		String store = attributes.getValue("store");
		
		Variable storage = store != null ? m_model.getVariable(store) : null;
		
		m_currentNode = m_currentNode.addNumbers(min, max, storage);
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if("Choices".equals(localName))
		{
			m_currentNode = m_nextNode;
		}
		else if("SkipTo".equals(localName))
		{
			m_currentNode.createSelfTransition();
			m_currentNode = m_nextNode;
		}
		else if("Literal".equals(localName))
		{
			String literal = finishReadingText();
			if(literal != null && literal.length() > 0)
			{
				m_currentNode = m_currentNode.addString(literal);
			}
		}
	}

	public void setCurrentNode(Node currentNode)
	{
		m_currentNode = currentNode;
	}

	@Override
	public void onNewCondition(Precondition condition) throws SAXException
	{
		m_currentNode.addEntryCondition(condition);
	}

	@Override
	public void onNewCommand(Command command) throws SAXException
	{
		m_currentNode.addEntryCommand(command);
	}

	@Override
	public void onNewTransition(Transition t) throws SAXException
	{
		m_currentNode.addTransition(t);
	}
	
	
}
