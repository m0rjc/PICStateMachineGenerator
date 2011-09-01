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
		for(SimulatedTransition t : m_transitions)
		{
			ActionResult result;
			try {
				result = t.run();
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
			}
		}
		
		throw new SimulationException("Node " + m_name + " did not RETURN.");
	}

	public String getName()
	{
		return m_name;
	} 
}
