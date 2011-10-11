package m0rjc.ax25.generator.simulatorBuilder;

import java.util.logging.Logger;

/**
 * Simulated command to save a location on the subroutine stack.
 *
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public class SimulatedActionSaveLocationOnSubroutineStack extends SimulatedAction
{
	private static Logger s_log = Logger.getLogger(SimulatedActionSaveLocationOnSubroutineStack.class.getName());
	
	private Simulation m_simulation;
	private SubroutineReturnPointer m_pointer;
	
	public SimulatedActionSaveLocationOnSubroutineStack(Simulation simulation)
	{
		m_simulation = simulation;
	}
	
	public void setReturnPointer(SubroutineReturnPointer pointer)
	{
		m_pointer = pointer;
	}
	
	@Override
	public ActionResult run() throws SimulationException
	{
		s_log.fine(String.format("    Command: Save return to subroutine stack: %s:%d", m_pointer.getStateName(), m_pointer.getInstructionId()));
		m_simulation.saveLocationToSubroutineStack(m_pointer.getStateName(), m_pointer.getInstructionId());
		return ActionResult.CONTINUE_TO_NEXT_ACTION;
	}

}
