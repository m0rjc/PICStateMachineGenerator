package m0rjc.ax25.generator.visitor;

/**
 * @author Richard Corfield
 */
public interface INode
{

	/**
	 * Get the name of this state
	 */
	String getStateName();

	/**
	 * True if this node has no transitions - making it a termination point.
	 * State engines that reach here should find themselves back at the start
	 * having executed any entry code. Guard conditions don't make sense.
	 */
	boolean hasTransitions();

}