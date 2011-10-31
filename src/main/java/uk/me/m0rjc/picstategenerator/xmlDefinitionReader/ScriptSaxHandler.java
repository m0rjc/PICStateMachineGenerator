package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.me.m0rjc.picstategenerator.model.Command;
import uk.me.m0rjc.picstategenerator.model.GosubCommand;
import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.ReturnFromSubroutineCommand;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.Transition;
import uk.me.m0rjc.picstategenerator.model.Variable;


/**
 * Handler for the state:ScriptDefinition element
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class ScriptSaxHandler extends ChainedSaxHandler
	implements TransitionSaxHandler.Callback, CommandListSaxHandler.Callback, ConditionListSaxHandler.Callback
{
	private static final String STEP_RETURN = "Return";
	private static final String STEP_GO_SUB = "GoSub";
	private static final String STEP_SKIP_TO = "SkipTo";
	private static final String STEP_NUMBERS = "Numbers";
	private static final String STEP_CHOICES = "Choices";
	private static final String STEP_LITERAL = "Literal";
	private static final String STEP_COMMANDS = "Commands";
	private static final String STEP_GUARD_CONDITION = "GuardCondition";
	private final StateModel m_model;
	private final CommandListSaxHandler m_commandHandler;
	private final ConditionListSaxHandler m_conditionHandler;
	private Node m_currentNode;
	/** Next node - following Choices */
	private Node m_nextNode;
	
	private final TransitionSaxHandler m_choicesHandler;

	public ScriptSaxHandler(StateModel model)
	{
		m_model = model;
		m_choicesHandler = new TransitionSaxHandler(model, this);
		m_commandHandler = new CommandListSaxHandler(model, this);
		m_conditionHandler = new ConditionListSaxHandler(model, this);
	}

	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(STEP_GUARD_CONDITION.equals(localName))
		{
			// The guard condition applies to the "current node". This is the node that is entered
			// as a result of whatever transition we've just scripted, and the node that whatever
			// we script next will come out of. A guard condition guards entry to this node.
			setChild(m_conditionHandler);
			m_conditionHandler.startElement(uri, localName, qName, attributes);
		}
		else if(STEP_COMMANDS.equals(localName))
		{
			setChild(m_commandHandler);
			m_commandHandler.startElement(uri, localName, qName, attributes);
		}
		else if(STEP_LITERAL.equals(localName))
		{
			startReadingText();
		}
		else if(STEP_CHOICES.equals(localName) || STEP_SKIP_TO.equals(localName))
		{
			onStartChoices(uri, localName, qName, attributes);
		}
		else if(STEP_NUMBERS.equals(localName))
		{
			onNumbers(attributes);
		}
		else if(STEP_GO_SUB.equals(localName))
		{
			startReadingText();
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
		setChild(m_choicesHandler);
		m_choicesHandler.startElement(uri, localName, qName, attributes);
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
		if(STEP_CHOICES.equals(localName))
		{
			m_currentNode = m_nextNode;
		}
		else if(STEP_SKIP_TO.equals(localName))
		{
			m_currentNode.createSelfTransition();
			m_currentNode = m_nextNode;
		}
		else if(STEP_LITERAL.equals(localName))
		{
			String literal = finishReadingText();
			if(literal != null && literal.length() > 0)
			{
				m_currentNode = m_currentNode.addString(literal);
			}
		}
		else if(STEP_GO_SUB.equals(localName))
		{
			// The gosub is applied to the "current node". This is the node that is entered
			// as a result of whatever transition we've just scripted, and the node that whatever
			// we script next will come out of.
			String target = finishReadingText();
			m_currentNode.addEntryCommand(new GosubCommand(target));
		}
		else if(STEP_RETURN.equals(localName))
		{
			m_currentNode.addEntryCommand(new ReturnFromSubroutineCommand());
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
