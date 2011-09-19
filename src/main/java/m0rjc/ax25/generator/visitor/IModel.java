package m0rjc.ax25.generator.visitor;

import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Variable;

/**
 * Callback interface for Model
 */
public interface IModel
{
	/** Return the initial state - root node */
	public abstract Node getInitialState();

	/** Variable lookup */
	public Variable getVariable(String name);
	
	/** Return the node with the given name */
	public abstract Node getNode(String name);	
}
