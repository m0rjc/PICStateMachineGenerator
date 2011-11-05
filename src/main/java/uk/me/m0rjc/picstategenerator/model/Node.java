package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;
import uk.me.m0rjc.picstategenerator.visitor.INode;


/**
 * A node in a state model.
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
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
		Transition t = getOrCreateTransition(ch);
		Node node = m_model.getNode(t.getTargetNodeName());

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
		
		Transition t = new Transition()
			.whenEqual(input, ch)
			.goTo(m_model.createNode());
		
		m_transitions.add(t);
		return t;
	}

	/**
	 * Create an empty transition to a new node.
	 * Add it to the transition list.
	 */
	private Transition createEmptyTransitionToNewNode()
	{
		Transition transition = new Transition().goTo(m_model.createNode());
		m_transitions.add(transition);
		return transition;
	}
	
	/**
	 * Create an empty transition to this node.
	 * Add it to the transition list.
	 */
	public Transition createSelfTransition()
	{
		Transition transition = new Transition().goTo(this);
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
		String exitNode = createEmptyTransitionToNewNode()
			.whenEqual(m_model.getInputVariable(), ',')
			.getTargetNodeName();

		addChoices(alternatePaths);
		
		createSelfTransition();

		return m_model.getNode(exitNode);
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
	 * Create node or nodes to capture numbers as a character array.
	 * 
	 * If max > min then the result will be a null terminated string. Allow space for the null.
	 * If max == min then the result will not be null terminated.
	 * 
	 * @param min the minimum amount of characters to read.
	 * @param max the maximum amount of characters to read.
	 * @param storage character array to store the result.
	 * @return the node that is entered on reading the last number
	 */
	public Node addNumbers(final int min, final int max, final Variable storage) 
	{
		Variable input = m_model.getInputVariable();
		CompositePrecondition p = new CompositePrecondition(
				VariableValuePrecondition.createGE(input, '0'),
				VariableValuePrecondition.createLE(input, '9'));
		return addInputClassSequence(p, min, max, storage);
	}
	
    /**
     * Create node or nodes to capture numbers as a character array.
     * 
     * @param size the amount of characters to read.
     * @param storage character array to store the result.
     * @return the node that is entered on reading the last number
     */
	public Node addNumbers(final int size, final Variable storage)
	{
	    return addNumbers(size, size, storage);
	}

	/** 
	 * Add numbers but do not store the result.
	 * @param min minimum amount to read.
	 * @param max maximum amount to read.
	 * @return the Node that the numbers node feeds into, to which new Transitions can be appended.
	 */
	public Node addNumbers(final int min, final int max)
	{
		return addNumbers(min,max,null);
	}
	
   /**
     * Create a node or nodes to skip over numbers.
     * @param size amount of numbers to skip.
     * @return the output Node to which new Transitions may be appended.
     */
    public Node addNumbers(final int size)
    {
        return addNumbers(size, size, null);
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
			Transition t = createEmptyTransitionToNewNode()
				.when(condition)
				.doCommand(storeCommand);
			exitNode = m_model.getNode(t.getTargetNodeName());
		}
		else if(min == max && m_transitions.isEmpty())
		{
			Transition t;
			// We can stay on the start node until we hit max. It's a generalisation
			// that fails if there are non-number transitions, but in my examples I don't
			// do that so will take advantage of optimising out one state.
			t = createSelfTransition()
				.ignoreTargetNodeEntry()
				.when(condition)
				.when(Precondition.lessThan(counter, max - 1))
				.doCommand(storeCommand);

			exitNode = m_model.getNode(t.getTargetNodeName());
			
			t = createEmptyTransitionToNewNode()
				.when(condition)
				.when(Precondition.equals(counter, max - 1))
				.doCommand(storeCommand);
			
			exitNode = m_model.getNode(t.getTargetNodeName());
		}
		else
		{
			Transition t;
			// First number takes us into a state where we start collecting numbers until we reach the minimum
			t = createEmptyTransitionToNewNode()
							.when(condition)
							.when(VariableValuePrecondition.createLE(counter, min - 2))
							.doCommand(storeCommand);

			Node numbersNode = m_model.getNode(t.getTargetNodeName());

			// Subsequent numbers less than min keep us in this state
			if(min > 2)
			{
				numbersNode.createSelfTransition()
						   .when(condition)
						   .when(VariableValuePrecondition.createLE(counter, min - 2))
						   .doCommand(storeCommand);
			}
			
			// Once we hit min then we can go to the exit state
			t = numbersNode.createEmptyTransitionToNewNode()
						   .when(condition)
						   .when(VariableValuePrecondition.createGE(counter, min - 1))
						   .doCommand(storeCommand);
			
			exitNode = m_model.getNode(t.getTargetNodeName());
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

	/**
	 * Build the command needed as part of an
	 * {@link #addInputClassSequence(Precondition, int, int, Variable) input class sequence}
	 * that will store the value in the right place and increment the counter.
	 * 
	 * @param input input variable
	 * @param storage where to store the value. May be null to not store.
	 * @param counter position counter within the variable
	 * @param requiresNullTerminatedString if true then the next space will be set to null.
	 * @return the storage command.
	 */
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
	 * @see uk.me.m0rjc.picstategenerator.visitor.INode#getStateName()
	 */
	@Override
	public String getStateName()
	{
		return m_stateName;
	}

	/**
	 * Does this node have commands to run on entry?
	 */
	public boolean hasEntryCommands()
	{
		return !m_entryCode.isEmpty();
	}
	
	/**
	 * @see uk.me.m0rjc.picstategenerator.visitor.INode#hasTransitions()
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
			if(isScriptEndPoint())
			{
				acceptAsScriptEndPoint(seenNodes, visitor);
			}
			else
			{
				acceptAsNodeWithTransitions(seenNodes, visitor);
			}
		}
	}

	/**
	 * Build this node as a node with outgoing transitions.
	 * This is used if not {@link #isScriptEndPoint()}.
	 * 
	 * See {@link #renderGoToThisNode(IModelVisitor, boolean)} which works in tandem with
	 * this method.
	 * 
	 * @param seenNodes
	 * @param visitor
	 */
	private void acceptAsNodeWithTransitions(Set<String> seenNodes, IModelVisitor visitor)
	{
		if(isUseSharedEntryCode())
		{
			renderSharedEntryCode(visitor);
		}
		
		visitor.startNode(this);
		for(Transition t : m_transitions)
		{
			t.accept(m_model, visitor);
		}
		
		if(isFallbackTransitionNeeded())
		{
			Transition t = new Transition();
			t.goTo(m_model.getInitialState());
			t.accept(m_model, visitor);
		}
		visitor.endNode(this);
		
		// Ask target nodes to render themselves.
		for (String targetName : getAllTargetNodeNames())
		{
			Node n = m_model.getNode(targetName);
			n.accept(seenNodes, visitor);
		}
	}

	/**
	 * Optionally build this node. It is a script end point.
	 * Script end points normally direct straight to the root node.
	 * It will only be rendered if there is shared code that needs rendering.
	 * See {@link #renderGoToRootNode(IModelVisitor, boolean)}, which works in
	 * tandem with this method.
	 * 
	 * This is used if {@link #isScriptEndPoint()}.
	 * 
	 * @param seenNodes
	 * @param visitor
	 */
	private void acceptAsScriptEndPoint(Set<String> seenNodes, IModelVisitor visitor)
	{
		if(isUseSharedEntryCode() && hasEntryCommands())
		{
			renderSharedEntryCode(visitor);
		}
	}

	
	/**
	 * Render the code to switch to this node as part of a transition.
	 * If this is a {@link #isScriptEndPoint()} then render code to switch to the 
	 * root node instead.
	 * 
	 * @param visitor
	 * @param ignoreTargetNodeEntry true if this transition must ignore the node's entry code.
	 */
	void renderGoToNode(IModelVisitor visitor, boolean ignoreTargetNodeEntry)
	{
		if(isScriptEndPoint())
		{
			renderGoToRootNode(visitor, ignoreTargetNodeEntry);
		}
		else
		{
			renderGoToThisNode(visitor, ignoreTargetNodeEntry);
		}
	}
	
	
	/**
	 * Render the code to switch to this node as part of a transition.
	 * 
	 * @param visitor
	 * @param ignoreTargetNodeEntry true if this transition must ignore the node's entry code.
	 */
	private void renderGoToThisNode(IModelVisitor visitor, boolean ignoreTargetNodeEntry)
	{
		if(ignoreTargetNodeEntry && hasEntryCommands())
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
				c.accept(m_model, visitor);
			}
			visitor.visitTransitionGoToNode(this);
		}
	}

	/**
	 * Render the code to switch to the root node as part of a transition.
	 * This node may have entry commands which need to be rendered first.
	 * 
	 * @param visitor
	 * @param ignoreTargetNodeEntry true if this transition must ignore the node's entry code.
	 */
	private void renderGoToRootNode(IModelVisitor visitor, boolean ignoreTargetNodeEntry)
	{
		Node rootNode = m_model.getInitialState();
		
		if(ignoreTargetNodeEntry && hasEntryCommands())
		{
			visitor.visitTransitionGoToNode(rootNode);
		}
		else if(isUseSharedEntryCode() && hasEntryCommands())
		{
			visitor.visitTransitionGoToSharedEntryCode(this);
		}
		else
		{
			for(Command c : getEntryCommands())
			{
				c.accept(m_model, visitor);
			}
			// Direct call prevents an infinite loop in the case of an empty model.
			rootNode.renderGoToThisNode(visitor, ignoreTargetNodeEntry);
		}
	}

	
	/**
	 * Nodes left at the end of scripts should be replaced by a jump to the start node.
	 * We derive this because such nodes have no outgoing transitions.
	 * This will also capture Numbers Nodes that have been left dangling.
	 * Such a node may still have entry commands.
	 * 
	 * @param model
	 * @return
	 */
	public boolean isScriptEndPoint()
	{
		return m_transitions.isEmpty();
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
			c.accept(m_model, visitor);
		}
		
		if(!isScriptEndPoint())
		{
			visitor.visitTransitionGoToNode(this);
		}
		else
		{
			// Script end point jumps straight to the root node.
			m_model.getInitialState().renderGoToThisNode(visitor, false);
		}
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
		for(String target : getAllTargetNodeNames())
		{
			Node targetNode = m_model.getNode(target);
			if(targetNode != null)
			{
				if(!seenEntries.add(target))
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

	/**
	 * Return the names of all nodes reachable from this one.
	 * @return
	 */
	private Set<String> getAllTargetNodeNames()
	{
		Set<String> targets = new HashSet<String>();
		
		for(Command c : m_entryCode)
		{
			String target = c.getTargetNode();
			if(target != null) targets.add(target);
		}
		
		for(Transition t : m_transitions)
		{
			// Don't count self transitions as they use shorter code
			for(String targetName : t.getAllTargetNodeNames())
			{
				if(targetName != getStateName())
				{
					targets.add(targetName);
				}
			}
		}
		
		if(isFallbackTransitionNeeded())
		{
			Node rootNode = m_model.getInitialState();
			targets.add(rootNode.getStateName());
		}

		return targets;
	}

	/**
	 * True if this Node uses the subroutine stack.
	 */
	public boolean requiresSubroutineStack()
	{
		for(Command c : m_entryCode)
		{
			if(c.requiresSubroutineStack()) return true;
		}

		for(Transition t : m_transitions)
		{
			if(t.requiresSubroutineStack()) return true;
		}
		
		return false;
	}

}
