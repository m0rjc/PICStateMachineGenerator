package m0rjc.ax25.generator.simulatorBuilder;

public interface SimulatedAction
{
	/** Perform the action.
	 * 
	 * @return
	 * @throws SimulationException
	 */
	ActionResult run() throws SimulationException;
}
