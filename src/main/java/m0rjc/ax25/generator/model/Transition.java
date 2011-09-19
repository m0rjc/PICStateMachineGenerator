package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.List;

import m0rjc.ax25.generator.visitor.IModel;
import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * A transition between states
 * 
 * @author Richard Corfield
 */
public class Transition
{
	/** One of targetNamedNode or targetNode will be set on construction to say where we're going */
	private String m_targetNamedNode;
	/** One of targetNamedNode or targetNode will be set on construction to say where we're going */
	private Node m_targetNode;
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
	 * Create a transition that will target the given named node.
	 */
	public Transition(String targetNamedNode)
	{
		m_targetNamedNode = targetNamedNode;
	}

	/**
	 * Create a transition that targets the given node.
	 */
	public Transition(Node targetNode)
	{
		m_targetNode = targetNode;
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
		m_targetNode = targetNode;
		return this;
	}

	
	/**
	 * The node this points to, if defined.
	 */
	public Node getNode()
	{
		return m_targetNode;
	}
	
	/**
	 * The node this transition points to - using the model to resolve a named node if necessary.
	 * @param model
	 * @return
	 */
	public Node getNode(IModel model)
	{
		if(m_targetNode != null)
		{
			return m_targetNode;
		}
		return model.getNode(m_targetNamedNode);
	}

	/**
	 * Name of the target node.
	 */
	public String getTargetNodeName()
	{
		return m_targetNode != null ? m_targetNode.getStateName() : m_targetNamedNode;
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
	public void accept(IModelVisitor visitor, IModel model)
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
			c.accept(visitor);
		}
		
		if(!m_ignoreTargetNodeEntry)
		{
			for(Command c : node.getEntryCommands())
			{
				c.accept(visitor);
			}
		}

		if(node.hasTransitions())
		{
			visitor.visitTransitionGoToNode(node.getStateName());
		}
		else // It's an End State, so reset
		{
			visitor.visitTransitionGoToNode(model.getInitialState().getStateName());
		}
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
	
}
