package m0rjc.ax25.generator.simulatorBuilder;

import java.util.ArrayList;
import java.util.List;

public class SimulatedNode
{
	private final String m_name;
	private List<SimulatedTransition> m_transitions = new ArrayList<SimulatedTransition>();
	private List<SimulatedAction> m_sharedEntryCode = null;
	
	SimulatedNode(String name)
	{
		m_name = name;
	}
	
	void addTransition(SimulatedTransition t)
	{
		m_transitions.add(t);
	}
	
	/**
	 * Declare shared entry code for this node.
	 */
	public void declareSharedEntryCode()
	{
		m_sharedEntryCode = new ArrayList<SimulatedAction>();
	}

	/**
	 * Has shared entry code been declared (even if not built?)
	 */
	public boolean isSharedEntryCodeDeclared()
	{
		return m_sharedEntryCode != null;
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
		m_sharedEntryCode.add(a);
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
		
		ActionResult result = ActionResult.CONTINUE_TO_NEXT_ACTION;
		for(SimulatedAction a : m_sharedEntryCode)
		{
			result = a.run();
			if(result == ActionResult.RETURN_FROM_STATE_ENGINE)
			{
				return result;
			}
			else if(result == ActionResult.NEXT_TRANSITION)
			{
				throw new SimulationException("Shared entry code atttempted 'go to next transition'");
			}
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
		
		for(SimulatedTransition t : m_transitions)
		{
			Log.finest(String.format("Node %s starting transition", m_name));
			ActionResult result;
			try {
				result = t.run();
				Log.finer(String.format("Node %s transition returned %s", m_name, result.name()));
			} catch (SimulationException e) {
				throw new SimulationException("Exception in Node " + m_name, e);
			}
			
			switch(result)
			{
			case CONTINUE_TO_NEXT_ACTION:
				// Probably a bug in this simulator
				throw new SimulationException("Node " + m_name + " transition fell through");
			case RETURN_FROM_STATE_ENGINE:
				return;
			case NEXT_TRANSITION:
				Log.fine("  Next transition");
			}
		}
		
		throw new SimulationException("Node " + m_name + " did not RETURN.");
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
