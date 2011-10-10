package m0rjc.ax25.generator.simulatorBuilder;

/**
 * Information about how to return from a subroutine.
 *
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public class SubroutineReturnPointer
{
	private final String m_stateName;
	private final int m_instructionId;

	/**
	 * Construct the return information.
	 * 
	 * @param stateName Name of the state to return to.
	 * @param instructionId Id of the instruction to start executing from.
	 */
	public SubroutineReturnPointer(String stateName, int instructionId)
	{
		m_stateName = stateName;
		m_instructionId = instructionId;
	}

	/**
	 * The name of the state to return to.
	 */
	public String getStateName()
	{
		return m_stateName;
	}

	/**
	 * The Id of the instruction to start executing from.
	 */
	public int getInstructionId()
	{
		return m_instructionId;
	}
}
