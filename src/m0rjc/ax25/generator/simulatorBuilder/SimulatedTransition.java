package m0rjc.ax25.generator.simulatorBuilder;

import java.util.ArrayList;
import java.util.List;

public class SimulatedTransition
{
	private List<SimulatedAction> m_actions = new ArrayList<SimulatedAction>();
	
	public void addAction(SimulatedAction a)
	{
		m_actions.add(a);
	}
	
	public ActionResult run() throws SimulationException
	{
		for(SimulatedAction action : m_actions)
		{
			ActionResult result = action.run();
			switch(result)
			{
			case NEXT_TRANSITION:
				return result;
			case RETURN_FROM_STATE_ENGINE:
				return result;
			}
		}
		
		return ActionResult.CONTINUE_TO_NEXT_ACTION;
	}

}
