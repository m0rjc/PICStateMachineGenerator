package m0rjc.ax25.generator.simulatorBuilder;

public abstract class SimulatedAction
{
	private static int m_counter;
	
	private int m_id;
	
	public SimulatedAction()
	{
		m_id = m_counter++;
	}
	
	/**
	 * Return the ID of this instruction.
	 */
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * For single instructions, is this the instruction's ID.
	 * For composites, does it contain the Id?
	 * @param instructionId
	 * @return
	 */
	public boolean containsInstructionId(int instructionId)
	{
		return instructionId == m_id;
	}
	
	/**
	 * Run only actions from and after the given Id.
	 * This is used when returning from subroutines to simulate a GOTO.
	 * 
	 * @return result of running this command
	 * @throws SimulationException
	 */
	public ActionResult runFromId(int id) throws SimulationException
	{
		if(m_id < id)
		{
			return ActionResult.CONTINUE_TO_NEXT_ACTION;
		}
		return run();
	}
	
	/** 
	 * Perform the action.
	 * 
	 * @return result of running this command
	 * @throws SimulationException
	 */
	public abstract ActionResult run() throws SimulationException;
}
