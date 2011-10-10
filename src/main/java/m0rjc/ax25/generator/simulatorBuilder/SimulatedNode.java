package m0rjc.ax25.generator.simulatorBuilder;


public class SimulatedNode
{
	private final String m_name;
	private SimulatedInstructionBlock m_stepCode = new SimulatedInstructionBlock();
	private SimulatedInstructionBlock m_sharedEntryCode = null;
	
	SimulatedNode(String name)
	{
		m_name = name;
	}
	
	void addTransition(SimulatedAction t)
	{
		m_stepCode.addAction(t);
	}
	
	/**
	 * Declare shared entry code for this node.
	 */
	public void declareSharedEntryCode()
	{
		m_sharedEntryCode = new SimulatedInstructionBlock();
	}

	/**
	 * Has shared entry code been declared (even if not built?)
	 */
	public boolean isSharedEntryCodeDeclared()
	{
		return m_sharedEntryCode != null;
	} 
	
	/**
	 * Return the shared entry code block.
	 * This will be null if {@link #declareSharedEntryCode()} has not been called.
	 */
	public SimulatedInstructionBlock getSharedEntryCode()
	{
		return m_sharedEntryCode;
	}
	
	/**
	 * Return the step code block.
	 */
	public SimulatedInstructionBlock getStepCode()
	{
		return m_stepCode;
	}
	
	/**
	 * Add an action to the shared entry code for this node.
	 * Shared entry code must have been declared using {@link #declareSharedEntryCode()}. 
	 * This tests that the builder is building in the correct order.
	 * 
	 * @param a
	 * @throws SimulationException
	 */
	public void addSharedEntryCode(SimulatedAction a) throws SimulationException
	{
		if(m_sharedEntryCode == null)
		{
			throw new SimulationException("Builder attempting to add shared actions without declaring shared entry");
		}
		m_sharedEntryCode.addAction(a);
	}

	/**
	 * Simulate the running of shared entry code for this node.
	 * @return always {@link ActionResult#RETURN_FROM_STATE_ENGINE} in current code.
	 * @throws SimulationException if no shared entry code is defined, or if the action does not end in {@link ActionResult#RETURN_FROM_STATE_ENGINE}
	 */
	public ActionResult runSharedEntryCode() throws SimulationException
	{
		if(m_sharedEntryCode == null)
		{
			throw new SimulationException("Attempt to call shared entry code on a node without shared entry code.");
		}
		
		ActionResult result = m_sharedEntryCode.run();
		
		if(result == ActionResult.RETURN_FROM_STATE_ENGINE)
		{
			return result;
		}
		else if(result == ActionResult.POP)
		{
			throw new SimulationException("Shared entry code atttempted 'go to next transition'");
		}
		throw new SimulationException("Shared entry code did not terminate");
	}
	
	/**
	 * Execute a state step with this as the current node.
	 * @throws SimulationException 
	 */
	void step() throws SimulationException
	{
		Log.fine(String.format("Node %s performing step", m_name));
		
		ActionResult result;
		try 
		{
			result = m_stepCode.run();
		} 
		catch (SimulationException e) 
		{
			throw new SimulationException("Exception in Node " + m_name, e);
		}

		if(result != ActionResult.RETURN_FROM_STATE_ENGINE)
		{
			throw new SimulationException("Node " + m_name + " transition fell through");
		}			
	}

	/**
	 * Execute a step starting from the given instruction Id.
	 * Used when returning from a subroutine.
	 * @param instructionId
	 * @throws SimulationException
	 */
	public void stepFromInstructionId(int instructionId) throws SimulationException
	{
		Log.fine(String.format("Node %s resuming step from instruction Id %d", m_name, instructionId));
		
		ActionResult result;
		try 
		{
			result = m_stepCode.runFromId(instructionId);
		} 
		catch (SimulationException e) 
		{
			throw new SimulationException("Exception in Node " + m_name, e);
		}

		if(result != ActionResult.RETURN_FROM_STATE_ENGINE)
		{
			throw new SimulationException("Node " + m_name + " transition fell through");
		}			
	}

	
	/**
	 * Return the state name for this node
	 * @return
	 */
	public String getName()
	{
		return m_name;
	}
}
