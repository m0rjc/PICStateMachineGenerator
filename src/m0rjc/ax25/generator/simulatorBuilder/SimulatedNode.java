package m0rjc.ax25.generator.simulatorBuilder;

import java.util.ArrayList;
import java.util.List;

class SimulatedNode
{
	private final String m_name;
	private List<SimulatedTransition> m_transitions = new ArrayList<SimulatedTransition>();
	
	SimulatedNode(String name)
	{
		m_name = name;
	}
	
	void addTransition(SimulatedTransition t)
	{
		m_transitions.add(t);
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

	public String getName()
	{
		return m_name;
	} 
}
