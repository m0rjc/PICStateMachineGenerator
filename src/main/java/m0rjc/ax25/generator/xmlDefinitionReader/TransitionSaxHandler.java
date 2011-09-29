package m0rjc.ax25.generator.xmlDefinitionReader;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handler for the script:Transition element, and the ScriptChoiceList and
 * ScriptChoice elements. Both are the same apart from that the Transition needs
 * a target to go to, but the ScriptChoice has a default to the next part of the
 * script. The different behaviour is determined by the presence of a default
 * target.
 * 
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@Dependent
class TransitionSaxHandler extends ChainedSaxHandler implements ConditionListSaxHandler.Callback, CommandListSaxHandler.Callback
{
	private final StateModel m_model;
	@Inject @New
	private CommandListSaxHandler m_commandHandler;
	@Inject @New
	private ConditionListSaxHandler m_conditionHandler;
	
	private Callback m_callback;

	private Transition m_currentTransition;
	private String m_defaultTarget;

	@Inject
	public TransitionSaxHandler(StateModel model)
	{
		m_model = model;
		m_commandHandler.setCallbackHandler(this);
		m_conditionHandler.setCallbackHandler(this);
	}

	public interface Callback
	{
		void onNewTransition(Transition t) throws SAXException;
	}

	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if ("Choice".equals(localName) || "Transition".equals(localName))
		{
			startTransition(localName, attributes);
		}
		else if("Conditions".equals(localName))
		{
			startChild(m_conditionHandler, uri, localName, qName, attributes);
		}
		else if("Commands".equals(localName))
		{
			startChild(m_commandHandler, uri, localName, qName, attributes);
		}
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if ("Choice".equals(localName) || "Transition".equals(localName))
		{
			if (m_callback != null)
				m_callback.onNewTransition(m_currentTransition);
			m_currentTransition = null;
		}
	}

	private void startTransition(String localName, Attributes attributes) throws SAXException
	{
		String input = attributes.getValue("input");
		String target = attributes.getValue("target");
		
		boolean hasTarget = target != null && target.length() > 0;
		boolean hasDefault = m_defaultTarget != null && m_defaultTarget.length() > 0;
		
		if(!hasTarget)
		{
			if(!hasDefault)
			{
				throw new SAXException(localName + ": Transition must have a target.");
			}
			target = m_defaultTarget;
		}
		
		m_currentTransition = new Transition(target);
		
		if(input != null && input.length() > 0)
		{
			Precondition condition = InputSpecificationParser.INSTANCE.parseCondition(m_model.getInputVariable(), input);
			if(condition != null)
			{
				m_currentTransition.when(condition);
			}
		}
		
	}

	@Override
	public void onNewCommand(Command command) throws SAXException
	{
		m_currentTransition.doCommand(command);
	}

	@Override
	public void onNewCondition(Precondition condition) throws SAXException
	{
		m_currentTransition.when(condition);
	}

	public void setDefaultTarget(String stateName)
	{
		m_defaultTarget = stateName;
	}

	public void setCallbackHandler(Callback handler)
	{
		m_callback = handler;
	}
}
