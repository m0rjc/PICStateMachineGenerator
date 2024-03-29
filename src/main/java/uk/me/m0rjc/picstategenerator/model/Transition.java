package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.me.m0rjc.picstategenerator.visitor.IModel;
import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;


/**
 * A transition between states
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class Transition
{
	/** One of targetNamedNode or targetNode will be set on construction to say where we're going */
	private String m_targetNamedNode;
	/** Additional preconditions to allow entry to this transition */
	private List<Precondition> m_preconditions = new ArrayList<Precondition>();
	/** Additional commands to perform on transition */
	private List<Command> m_transitionCommands = new ArrayList<Command>();
	/** Ignore entry conditions and actions on the target node */
	private boolean m_ignoreTargetNodeEntry;
	
	/**
	 * Create an undefined transition.
	 * Must use one of the goTo methods.
	 */
	public Transition()
	{
	}
		
	/**
	 * Create a precondition for equality
	 */
	public Transition whenEqual(Variable v, int value)
	{
		when(VariableValuePrecondition.createEQ(v, value));
		return this;
	}
	
	/**
	 * Create a precondition that accepts values of variable in the given range
	 */
	public Transition whenInRange(Variable v, int min, int max)
	{
		when(VariableValuePrecondition.createGE(v, min));
		when(VariableValuePrecondition.createLE(v, max));
		return this;
	}
	
	/**
	 * Add a precondition
	 * @param p
	 */
	public Transition when(Precondition p)
	{
		m_preconditions.add(p);
		return this;
	}
	
	/**
	 * Set the target node name. Support for fluent writing style
	 */
	public Transition goTo(String targetNode)
	{
		m_targetNamedNode = targetNode;
		return this;
	}

	/**
	 * Set the target node. Support for fluent writing style
	 */
	public Transition goTo(Node targetNode)
	{
		goTo(targetNode.getStateName());
		return this;
	}
	
	/**
	 * The node this transition points to - using the model to resolve a named node if necessary.
	 * @param model
	 * @return
	 */
	public Node getNode(IModel model)
	{
		return model.getNode(m_targetNamedNode);
	}

	/**
	 * Name of the target node.
	 */
	public String getTargetNodeName()
	{
		return m_targetNamedNode;
	}
	
	/**
	 * Add a command to be performed on transition
	 * @param c
	 * @return
	 */
	public Transition doCommand(Command... commands)
	{
		for(Command c : commands) m_transitionCommands.add(c);
		return this;
	}
	
	/**
	 * Visit this transition. Does not recurse into the Node
	 * @param visitor
	 */
	public void accept(StateModel model, IModelVisitor visitor)
	{
		visitor.visitTransition(this);
		for(Precondition p : m_preconditions)
		{
			p.accept(visitor);
		}
		
		Node node = getNode(model);		
		if(!m_ignoreTargetNodeEntry)
		{
			for(Precondition p : node.getEntryPreconditions())
			{
				p.accept(visitor);
			}
		}

		for(Command c : m_transitionCommands)
		{
			c.accept(model, visitor);
		}
		
		node.renderGoToNode(visitor, m_ignoreTargetNodeEntry);
		visitor.endTransition(this);
	}
	
	/**
	 * True if this value depends on this variable and only this variable and accepts the given
	 * input.
	 * @param variable variable in use
	 * @param value expected value
	 */
	public boolean accepts(Variable variable, int value)
	{
		for(Precondition p : m_preconditions)
		{
			if(!p.accepts(variable, value)) return false;
		}
		return true;
	}

	/**
	 * Set to ignore entry conditions and actions on the target node.
	 * @return
	 */
	public Transition ignoreTargetNodeEntry()
	{
		m_ignoreTargetNodeEntry = true;
		return this;
	}

	/**
	 * Does this transition accept any input?
	 * @return
	 */
	public boolean acceptsAllInputs()
	{
		return m_preconditions.isEmpty();
	}

	/**
	 * True if this Node uses the subroutine stack.
	 */
	public boolean requiresSubroutineStack()
	{
		for(Command c : m_transitionCommands)
		{
			if(c.requiresSubroutineStack()) return true;
		}
		return false;
	}

	/**
	 * Return all nodes this transition references. These are its own target and any
	 * gosubs.
	 */
	public Iterable<String> getAllTargetNodeNames()
	{
		Set<String> result = new HashSet<String>();
		result.add(getTargetNodeName());
		for(Command c : m_transitionCommands)
		{
			String name = c.getTargetNode();
			if(name != null) result.add(name);
		}
		return result;
	}
}
