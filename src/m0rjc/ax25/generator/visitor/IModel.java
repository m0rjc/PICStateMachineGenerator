package m0rjc.ax25.generator.visitor;

import m0rjc.ax25.generator.model.Node;

/**
 * Callback interface for Model
 */
public interface IModel
{
	/** Return the initial state - root node */
	public abstract Node getInitialState();

	/** Return the node with the given name */
	public abstract Node getNode(String name);

	/** Return the code segment to use to escape the state engine */
	public abstract String[] getReturnCode();
	
}
