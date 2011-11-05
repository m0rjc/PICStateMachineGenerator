package uk.me.m0rjc.picstategenerator.simulatorBuilder;

import java.util.Stack;

import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.RomLocation;
import uk.me.m0rjc.picstategenerator.model.Transition;
import uk.me.m0rjc.picstategenerator.model.Variable;
import uk.me.m0rjc.picstategenerator.visitor.IModel;
import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;
import uk.me.m0rjc.picstategenerator.visitor.INode;


/**
 * Build a simulator following instructions from the model.
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class SimulatorBuilder implements IModelVisitor
{
	private Simulation m_simulation = new Simulation();
	private SimulatedNode m_currentNode;
	private SimulatedVariable m_currentVariable;
	private String m_rootNodeName;
	private SimulatedActionSaveLocationOnSubroutineStack m_nextPopMustAddLocationToSubroutineStack;
	
	private Stack<SimulatedInstructionBlock> m_instructionBlockStack = new Stack<SimulatedInstructionBlock>();
	
	public void registerSpecialFunctionRegister(String name)
	{
		m_simulation.addVariable(new SimulatedVariable(name, 1));
	}
	
	public Simulation getSimulation()
	{
		return m_simulation;
	}
	
	@Override
	public void visitStartModel(final IModel model)
	{
	    for(Variable variable : model.getVariables())
	    {
	        if(variable.isImplicitlyImported())
	        {
	            createSimulatedVariable(variable);
	        }
	    }
	}

	/**
	 * Declare an external symbol
	 * @param name
	 */
	@Override
	public void visitDeclareExternalSymbol(Variable name)
	{
	    
	}

	/**
	 * Declare an external symbol
	 * @param name
	 */
	public void visitDeclareExternalSymbol(RomLocation name)
	{
		m_simulation.registerExternalSymbol(name);
	}

	
	/**
	 * Declare a global symbol - defined in this module to be exported
	 * @param name
	 */
	public void visitDeclareGlobalSymbol(String name)
	{
		// Nothing to do
	}
	
	/**
	 * Declare the start of access variable definitions
	 */
	public void visitStartAccessVariables(boolean modelDefinesAccessVariables)
	{
		// Nothing to do
	}
	
	@Override
	public void visitCreateVariableDefinition(final Variable v)
	{
		m_currentVariable = createSimulatedVariable(v);
	}

	/**
	 * Create a simulated variable based on the given template.
	 * @param v
	 */
    private SimulatedVariable createSimulatedVariable(final Variable v)
    {
        SimulatedVariable variable = new SimulatedVariable(v.getName(), v.getSize());
		m_simulation.addVariable(variable);
		String[] flags = v.getFlagNames();
		for(int i = 0; i < flags.length; i++)
		{
		    variable.registerBit(flags[i], i);
		}
		return variable;
    }

	
	/**
	 * Declare the start of banked variable definition
	 * @param bankNumber
	 */
	public void visitStartBankedVariables(int bankNumber, boolean modelDefinesVariablesInThisBank)
	{
		// Nothing to do
	}
	
	/**
	 * Declare the start of code.
	 * The builder may wish to output any boilerplate code here.
	 */
	public void visitStartCode()
	{
		// Nothing to do
	}
	
	@Override
	public void startSharedEntryCode(INode node)
	{
		SimulatedNode sim = initialiseNode(node);
		sim.declareSharedEntryCode();
		m_instructionBlockStack.add(sim.getSharedEntryCode());
	}

	/**
	 * Initialse a Node, whether we're building shared entry or step code.
	 * @param node
	 * @return
	 */
	private SimulatedNode initialiseNode(INode node)
	{
		assert m_nextPopMustAddLocationToSubroutineStack == null : "Previous node jumped to a subroutine but did not save a return address";
		
		m_currentNode = m_simulation.getNode(node.getStateName());
		if(m_currentNode == null)
		{
			assert m_instructionBlockStack.empty() : "Previous node did not empty the instruction block stack";
			m_currentNode = new SimulatedNode(node.getStateName());
			m_simulation.addNode(m_currentNode);
			
			if(m_rootNodeName == null)
			{
				m_rootNodeName = node.getStateName();
				m_simulation.setCurrentState(m_currentNode);
			}			
		}
		m_instructionBlockStack.clear();
		return m_currentNode;
	}

	/**
	 * Start a Node.
	 * The Node will be started, all its transitions visited, then the node ended before any other
	 * nodes are visited.
	 * 
	 * The first node to be started is the root node.
	 * @param node
	 */
	public void startNode(INode node)
	{
		SimulatedNode sim = initialiseNode(node);
		m_instructionBlockStack.add(sim.getStepCode());
	}

	/**
	 * Visit a Transition on the current Node
	 * @param rangeTransition
	 */
	public void visitTransition(Transition transition)
	{
		push();
		m_currentNode.addTransition(getCurrentInstructionBlock());
	}
	
	/**
	 * Return the instruction block currently being worked on.
	 * @return
	 */
	private SimulatedInstructionBlock getCurrentInstructionBlock()
	{
		return m_instructionBlockStack.peek();
	}
	
	/** Encode a transition precondition for Greater or Equals. Variable may need substitution */
	public void visitTransitionPreconditionGE(Variable variable, final int expectedValue)
	{
		final String name = variable.getName();
		final Simulation simulation = m_simulation;
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				byte actualValue = simulation.getVariable(name).getValue();
				Log.fine(String.format("    Condition: %s >= %s, value=%s",
							name, Log.formatByte((byte)expectedValue), Log.formatByte(actualValue)));
				
				if(actualValue >= expectedValue)
					return ActionResult.CONTINUE_TO_NEXT_ACTION;
				return ActionResult.POP;
			}
		});
	}

	/** Encode a transition precondition for Equals */
	public void visitTransitionPreconditionEQ(Variable variable, final int expectedValue)
	{
		final String name = variable.getName();
		final Simulation simulation = m_simulation;
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				byte actualValue = simulation.getVariable(name).getValue();
				Log.fine(String.format("    Condition: %s == %s, value=%s",
					name, Log.formatByte((byte)expectedValue), Log.formatByte(actualValue)));
				
				if(actualValue == expectedValue)
					return ActionResult.CONTINUE_TO_NEXT_ACTION;
				return ActionResult.POP;
			}
		});
	}
	
	/** Encode a transition precondition for Less than or Equals */
	public void visitTransitionPreconditionLE(Variable variable, final int expectedValue)
	{
		final String name = variable.getName();
		final Simulation simulation = m_simulation;
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				byte actualValue = simulation.getVariable(name).getValue();
				Log.fine(String.format("    Condition: %s <= %s, value=%s",
					name, Log.formatByte((byte)expectedValue), Log.formatByte(actualValue)));

				if(simulation.getVariable(name).getValue() <= expectedValue)
					return ActionResult.CONTINUE_TO_NEXT_ACTION;
				return ActionResult.POP;
			}
		});		
	}

	/** Encode a precondition checking that the given flag has the given value */
	public void visitTransitionPreconditionFlag(Variable flag, final int bit, final boolean expectedValue)
	{
		final String name = flag.getName();
		final Simulation simulation = m_simulation;
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				boolean actualValue = simulation.getVariable(name).getBit(bit);
				Log.fine(String.format("    Condition: %s:%d = %b. value=%b", name, bit, expectedValue, actualValue));
				
				if(actualValue == expectedValue)
					return ActionResult.CONTINUE_TO_NEXT_ACTION;
				return ActionResult.POP;
			}
		});				
	}
		
	/** Encode storing the input at the given variable+indexed offset location. */
	public void visitCommandCopyVariableToIndexedVariable(Variable source, Variable output, Variable indexer)
	{
		final String sourceName = source.getName();
		final String outputName = output.getName();
		final String indexerName = indexer.getName();
		
		final Simulation simulation = m_simulation;

		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable outVar = simulation.getVariable(outputName);
				SimulatedVariable indexVar = simulation.getVariable(indexerName);
				SimulatedVariable sourceVar = simulation.getVariable(sourceName);
				
				byte value = sourceVar.getValue();
				Log.fine(String.format("    Command: %s[%s] := %s. index=%d, value=%s",
					outputName, indexerName, sourceName, indexVar.getValue(), Log.formatByte(value))); 
				outVar.setValue(indexVar, value);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});
	}

	/** Encode copy input to output */
	public void visitCommandCopyVariable(Variable input, Variable output)
	{
		final String sourceName = input.getName();
		final String outputName = output.getName();
		
		final Simulation simulation = m_simulation;

		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable outVar = simulation.getVariable(outputName);
				SimulatedVariable sourceVar = simulation.getVariable(sourceName);
				
				byte value = sourceVar.getValue();
				Log.fine(String.format("    Command: %s := %s. value=%s", outputName, sourceName, Log.formatByte(value)));
				
				outVar.setValue(value);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});

	}
	
	/** Encode clearing a variable's value. */
	public void visitCommandClearVariable(Variable variable)
	{
		final String variableName = variable.getName();
		final Simulation simulation = m_simulation;
		final int size = variable.getSize();

		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable v = simulation.getVariable(variableName);
				Log.fine(String.format("    Command: CLRF %s (%d bytes)", variableName, size));
				for(int i = 0; i < size; i++)
				{
					v.setValue(i,(byte)0);
				}
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});		
	}
	
	/** Encode clearing a variable's value using indexing. */
	public void visitCommandClearIndexedVariable(Variable variable, Variable indexer)
	{
		final String variableName = variable.getName();
		final String indexerName = indexer.getName();
		final Simulation simulation = m_simulation;
	
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable v = simulation.getVariable(variableName);
				SimulatedVariable i = simulation.getVariable(indexerName);
				Log.fine(String.format("    Command: %s[%s] := 0. index=%d", variableName, indexerName, i.getValue()));

				v.setValue(i,(byte)0);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});		
	}
	
	/** Encode incrementing a variable's value */
	public void visitCommandIncrementVariable(Variable variable)
	{
		if(variable.getSize() > 1) throw new UnsupportedOperationException("Does not support incrementing multibyte values");
		
		final String variableName = variable.getName();
		final Simulation simulation = m_simulation;
	
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable v = simulation.getVariable(variableName);
				byte newValue = (byte)(v.getValue() + 1);
				Log.fine(String.format("    Command: %s++. newValue=%d", variableName, newValue));
				v.setValue(newValue);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});		
	}

	/** Encode a command to set or clear a flag. If bit is more than 7 then more than one byte is used. */
	public void visitCommandSetFlag(Variable flags, final int bit, final boolean newValue)
	{
		final String variableName = flags.getName();
		final Simulation simulation = m_simulation;
	
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				SimulatedVariable v = simulation.getVariable(variableName);
				Log.fine(String.format("    Command: %s:%d = %b", variableName, bit, newValue));
				v.setBit(bit, newValue);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});		
	}
	
	@Override
    public void visitCommandMethodCall(final RomLocation method)
    {
		final Simulation simulation = m_simulation;
	
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				Log.fine(String.format("    Command: CALL %s", method.getName()));
				simulation.call(method);
				return ActionResult.CONTINUE_TO_NEXT_ACTION;
			}
		});		
    }

	/** Encode a "Go to named node and return control" in the transition */
	public void visitTransitionGoToNode(INode node)
	{
		final String stateName = node.getStateName();
		final Simulation simulation = m_simulation;

		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				Log.fine(String.format("    Command: GOTO %s", stateName));
				simulation.setCurrentState(stateName);
				return ActionResult.RETURN_FROM_STATE_ENGINE;
			}
		});		
	}
	
	@Override
	public void visitTransitionGoToSharedEntryCode(INode node)
	{
		final String stateName = node.getStateName();
		final Simulation simulation = m_simulation;

		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				Log.fine(String.format("    Command: GOTO %s (Shared)", stateName));
				return simulation.runSharedEntryCode(stateName);
			}
		});				
	}
	
	@Override
	public void push()
	{
		SimulatedInstructionBlock newBlock = new SimulatedInstructionBlock();
		addAction(newBlock);
		m_instructionBlockStack.add(newBlock);
	}

	@Override
	public void saveReturnOnSubroutineStack()
	{
		m_nextPopMustAddLocationToSubroutineStack = new SimulatedActionSaveLocationOnSubroutineStack(m_simulation);
		addAction(m_nextPopMustAddLocationToSubroutineStack);
	}

	@Override
	public void exitCodeBlock(int levels)
	{
		if(levels > 0)
		{
			throw new RuntimeException("Simulator has not implemented multi depth jumps");
		}
		getCurrentInstructionBlock().addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				return ActionResult.POP;
			}
		});
	}

	@Override
	public void pop()
	{
		m_instructionBlockStack.pop();
		if(m_nextPopMustAddLocationToSubroutineStack != null)
		{
			SimulatedAction dummyAction = new SimulatedAction() {
				@Override
				public ActionResult run() throws SimulationException
				{
					return ActionResult.CONTINUE_TO_NEXT_ACTION;
				}
			};
			addAction(dummyAction);
			m_nextPopMustAddLocationToSubroutineStack.setReturnPointer(new SubroutineReturnPointer(m_currentNode.getName(), dummyAction.getId()));
			m_nextPopMustAddLocationToSubroutineStack = null;
		}
	}

	@Override
	public void visitTransitionReturnFromSubroutineStack()
	{
		final Simulation simulation = m_simulation;
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				Log.fine(String.format("    Command: RETURN FROM SUBROUTINE."));
				simulation.returnFromSubroutine();
				return ActionResult.RETURN_FROM_STATE_ENGINE;
			}
		});
	}

	@Override
	public void endTransition(Transition transition)
	{
		pop();
	}

	/**
	 * End of visiting a Node
	 * @param node
	 */
	public void endNode(Node node)
	{
		// Encode the fallback case
//		visitTransition(null);
		
		addAction(new SimulatedAction() {
			@Override
			public ActionResult run() throws SimulationException
			{
				throw new SimulationException("Node logic fell through");
			}
		});		
		pop();
	}

	/**
	 * End of visiting
	 */
	public void finished()
	{
		Log.fine(String.format("Simulator initialised for test. %d states", m_simulation.getStateCount()));
	}
	
	/**
	 * Add a action to whatever is currently being built.
	 * @param a
	 */
	private void addAction(SimulatedAction a)
	{
		getCurrentInstructionBlock().addAction(a);
	}
}
