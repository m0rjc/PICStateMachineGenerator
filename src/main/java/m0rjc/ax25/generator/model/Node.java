package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import m0rjc.ax25.generator.visitor.IModelVisitor;
import m0rjc.ax25.generator.visitor.INode;

/**
 * A node in a state model.
 * @author Richard Corfield
 */
public class Node implements INode
{
	/** Name for this state */
	private String m_stateName;
	
	/** Owning model */
	private StateModel m_model;
	
	/** Code to be run on entry to the state */
	private List<Command> m_entryCode = new ArrayList<Command>();

	/** Does this node use shared entry code? */
	private boolean m_useSharedEntryCode;
	
	/** 
	 * Transitions out of this state.
	 * If empty then this is an end-state and should "become" the
	 * initial state once any entry code has run.
	 */
	private List<Transition> m_transitions = new ArrayList<Transition>();
	
	/**
	 * Guard conditions on entry. 
	 * do not allow entry
	 */
	private List<Precondition> m_entryPreconditions = new ArrayList<Precondition>();
	
	/**
	 * Create a node with given name belonging to given model
	 * @param stateName
	 * @param stateModel
	 */
	public Node(String stateName, StateModel stateModel)
	{
		m_stateName = stateName;
		m_model = stateModel;
	}

	/**
	 * Return the owning state model.
	 */
	public StateModel getModel()
	{
		return m_model;
	}
	
	/**
	 * Encode the transitions needed to read the given string.
	 * This will create a chain of nodes and transitions (reusing any
	 * that already exist.)
	 * 
	 * @param string
	 * @return the node that is entered once the last character is read
	 * @exception should the state conflict with existing code.
	 */
	public Node addString(String string) 
	{
		char ch = string.charAt(0);
		Node node = getOrCreateTransition(ch).getNode();
		if(string.length() > 1)
		{
			return node.addString(string.substring(1));
		}
		return node;
	}

	/**
	 * Get or create a transition that covers a literal value
	 * @param ch
	 * @return
	 */
	private Transition getOrCreateTransition(int ch) 
	{
		Variable input = m_model.getInputVariable();
		
		for(Transition t : m_transitions)
		{
			if(t.accepts(input, ch))
			{
				return t;
			}
		}
		
		Transition t = new Transition(m_model.createNode())
			.whenEqual(input, ch);
		
		m_transitions.add(t);
		return t;
	}

	/**
	 * Create an empty transition to a new node.
	 * Add it to the transition list.
	 */
	private Transition createEmptyTransitionToNewNode()
	{
		Transition transition = new Transition(m_model.createNode());
		m_transitions.add(transition);
		return transition;
	}
	
	/**
	 * Create an empty transition to this node.
	 * Add it to the transition list.
	 */
	public Transition createSelfTransition()
	{
		Transition transition = new Transition(this);
		m_transitions.add(transition);
		return transition;
	}
	
	/**
	 * Add the given transition
	 */
	public void addTransition(Transition transition)
	{
		m_transitions.add(transition);
	}
	
	/**
	 * Add a condition to check before allowing entry to this node
	 */
	public Node addEntryCondition(Precondition p)
	{
		m_entryPreconditions.add(p);
		return this;
	}

	/**
	 * Skip to the next ',' or if we see a $ go to the "I've just read $" node.
	 * @param dollar
	 * @return
	 */
	public Node skipToCommaElse(Transition... alternatePaths)
	{
		Node exitNode = createEmptyTransitionToNewNode()
			.whenEqual(m_model.getInputVariable(), ',')
			.getNode();

		addChoices(alternatePaths);
		
		createSelfTransition();

		return exitNode;
	}
	
	/**
	 * Keep evaluating the given transitions until one comes up.
	 * @param transitions
	 */
	public void skipTo(Transition... transitions)
	{
		addChoices(transitions);
		createSelfTransition();
	}
	
	/**
	 * Create node or nodes to capture numbers.
	 * 
	 * If max > min then the result will be a null terminated string. Allow space for the null.
	 * If max == min then the result will not be null terminated.
	 * 
	 * @return the node that is entered on reading the last number
	 */
	public Node addNumbers(int min, int max, Variable storage) 
	{
		Variable input = m_model.getInputVariable();
		CompositePrecondition p = new CompositePrecondition(
				VariableValuePrecondition.createGE(input, '0'),
				VariableValuePrecondition.createLE(input, '9'));
		return addInputClassSequence(p, min, max, storage);
	}

	/** Add numbers but do not store the result */
	public Node addNumbers(int min, int max) throws Exception
	{
		return addNumbers(min,max,null);
	}
	
	/**
	 * Create node or nodes to capture based on the given condition
	 * 
	 * If max > min then the result will be a null terminated string. Allow space for the null.
	 * If max == min then the result will not be null terminated.
	 * 
	 * @return the node that is entered on reading the last input
	 */
	private Node addInputClassSequence(Precondition condition, int min, int max, Variable storage) 
	{
		Node exitNode;
		Variable counter = m_model.getCountVariable();
		Variable input = m_model.getInputVariable();
		boolean requiresNullTerminatedString = max > min;
		
		CompositeCommand storeCommand = buildStoreCommand(input, storage, counter, requiresNullTerminatedString);
		
		assert storage == null || storage.getSize() >= (requiresNullTerminatedString ? max+1 : max);
		
		// This node - clear the counter on entry
		addEntryCommand(Command.clearValue(counter));
		if(storage != null) addEntryCommand(Command.clearValue(storage));
		
		// The target state
		if(min == 0)
		{
			exitNode = this;
		}
		else if(min == 1)
		{
			// Any numbers allow us to exit
			exitNode = createEmptyTransitionToNewNode()
				.when(condition)
				.doCommand(storeCommand)
				.getNode();
		}
		else if(min == max && m_transitions.isEmpty())
		{
			// We can stay on the start node until we hit max. It's a generalisation
			// that fails if there are non-number transitions, but in my examples I don't
			// do that so will take advantage of optimising out one state.
			createSelfTransition()
				.ignoreTargetNodeEntry()
				.when(condition)
				.when(Precondition.lessThan(counter, max - 1))
				.doCommand(storeCommand)
				.getNode();
			
			exitNode = createEmptyTransitionToNewNode()
				.when(condition)
				.when(Precondition.equals(counter, max - 1))
				.doCommand(storeCommand)
				.getNode();
		}
		else
		{
			// First number takes us into a state where we start collecting numbers until we reach the minimum
			Node numbersNode = createEmptyTransitionToNewNode()
							.when(condition)
							.when(VariableValuePrecondition.createLE(counter, min - 2))
							.doCommand(storeCommand)
							.getNode();

			// Subsequent numbers less than min keep us in this state
			if(min > 2)
			{
				numbersNode.createSelfTransition()
						   .when(condition)
						   .when(VariableValuePrecondition.createLE(counter, min - 2))
						   .doCommand(storeCommand);
			}
			
			// Once we hit min then we can go to the exit state
			exitNode = numbersNode.createEmptyTransitionToNewNode()
						   .when(condition)
						   .when(VariableValuePrecondition.createGE(counter, min - 1))
						   .doCommand(storeCommand)
						   .getNode();
		}

		if(max > min)
		{
			exitNode.createSelfTransition()
				.when(condition)
				.when(VariableValuePrecondition.createLE(counter, max - 1))
				.doCommand(storeCommand);
		}
		
		return exitNode;
	}

	private CompositeCommand buildStoreCommand(Variable input,
			Variable storage, Variable counter,
			boolean requiresNullTerminatedString)
	{
		CompositeCommand storeCommand = new CompositeCommand();
		if(storage != null)
		{
			storeCommand.add(Command.storeValueIndex(input, storage, counter))
					    .add(Command.incrementValue(counter));
			if(requiresNullTerminatedString)
				storeCommand.add(Command.clearIndexedValue(storage, counter));
		}
		else
		{
			storeCommand.add(Command.incrementValue(counter));
		}
		return storeCommand;
	}
	
	/**
	 * Create a node or nodes to skip over numbers
	 * @return
	 * @throws Exception 
	 */
	public Node addNumbers(int i) throws Exception
	{
		return addNumbers(i,i,null);
	}


	/**
	 * Add transitions out of this node.
	 */
	public void addChoices(Transition... transitions)
	{
		for(Transition t : transitions)
		{
			m_transitions.add(t);
		}
	}

	/**
	 * Add commands to execute on entry to this node.
	 * @return this node
	 */
	public Node addEntryCommand(Command c)
	{
		m_entryCode.add(c);
		return this;
	}

	/**
	 * @see m0rjc.ax25.generator.visitor.INode#getStateName()
	 */
	@Override
	public String getStateName()
	{
		return m_stateName;
	}

	/**
	 * @see m0rjc.ax25.generator.visitor.INode#hasTransitions()
	 */
	@Override
	public boolean hasTransitions()
	{
		return !m_transitions.isEmpty();
	}
	
	/**
	 * Visit the node, then its transitions.
	 * Then visit child nodes.
	 * @param seenNodes 
	 * @param visitor
	 */
	public void accept(Set<String> seenNodes, IModelVisitor visitor)
	{
		if(seenNodes.add(getStateName()))
		{
			if(isUseSharedEntryCode())
			{
				renderSharedEntryCode(visitor);
			}
			
			visitor.startNode(this);
			for(Transition t : m_transitions)
			{
				t.accept(visitor, m_model);
			}
			if(isFallbackTransitionNeeded())
			{
				Transition t = new Transition();
				t.goTo(m_model.getInitialState());
				t.accept(visitor, m_model);
			}
			visitor.endNode(this);
			
			for(Transition t : m_transitions)
			{
				if(!t.isOptimiseOutTargetNode(m_model))
				{
					Node n = t.getNode(m_model);
					n.accept(seenNodes, visitor);
				}
			}
			
		}
	}

	/**
	 * Render the code to switch to this node as part of a transition.
	 * @param visitor
	 * @param ignoreTargetNodeEntry true if this transition must ignore the node's entry code.
	 */
	void renderGoToNode(IModelVisitor visitor, boolean ignoreTargetNodeEntry)
	{
		if(ignoreTargetNodeEntry)
		{
			visitor.visitTransitionGoToNode(this);
		}
		else if(isUseSharedEntryCode())
		{
			visitor.visitTransitionGoToSharedEntryCode(this);
		}
		else
		{
			for(Command c : getEntryCommands())
			{
				c.accept(visitor);
			}
			visitor.visitTransitionGoToNode(this);
		}
	}

	
	/**
	 * Render the shared entry code for the node.
	 * This is the code that is GOTO by any transition to enter this node.
	 * The exception is if the transition is marked {@link Transition#ignoreTargetNodeEntry()}.
	 */
	private void renderSharedEntryCode(IModelVisitor visitor)
	{
		visitor.startSharedEntryCode(this);
		for(Command c : m_entryCode)
		{
			c.accept(visitor);
		}
		visitor.visitTransitionGoToNode(this);
	}

	/** Return the preconditions for entry to this node */
	public List<Precondition> getEntryPreconditions()
	{
		return m_entryPreconditions;
	}

	/** Return the list of commands to perform on entry to this node. */
	public List<Command> getEntryCommands()
	{
		return m_entryCode;
	}

	/**
	 * Does the assembler need to create code for a fallback position, should
	 * none of the transitions for this node be satisfied?
	 * 
	 */
	private boolean isFallbackTransitionNeeded()
	{
		// Initial simple implementation just looks for wildcard transitions.
		// If any transition accepts all inputs then there is no need for the fallback.
		for(Transition t : m_transitions)
		{
			if(t.acceptsAllInputs())
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Does this node use shared entry code?
	 */
	public boolean isUseSharedEntryCode()
	{
		return m_useSharedEntryCode;
	}

	/**
	 * Indicate that this node should use shared entry code.
	 */
	public void setUseSharedEntryCode()
	{
		m_useSharedEntryCode = true;
	}

	/**
	 * Work through the tree setting all nodes with multiple entrants to "use shared entry code"
	 * @param seenNodes Set of nodes that have been visited
	 * @param seenEntries Set of node entry names that have been seen without {@link Transition#ignoreTargetNodeEntry()}
	 */
	public void setSharedEntryCodeOnMultipleEntryNodes(HashSet<String> seenEntries)
	{
		for(Transition t : m_transitions)
		{
			// Don't count self transitions as they use shorter code
			String targetName = t.getTargetNodeName();
			if(targetName != getStateName())
			{
				Node targetNode = m_model.getNode(targetName);
				if(targetNode != null)
				{
					if(!seenEntries.add(targetName))
					{
						targetNode.setUseSharedEntryCode();
					}
					else
					{
						targetNode.setSharedEntryCodeOnMultipleEntryNodes(seenEntries);
					}
				}
			}
		}
		
		if(isFallbackTransitionNeeded())
		{
			Node rootNode = m_model.getInitialState();
			if(!seenEntries.add(rootNode.getStateName()))
			{
				rootNode.setUseSharedEntryCode();
			}
		}
	}

}
