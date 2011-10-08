package m0rjc.ax25.generator.visitor;

import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Variable;

/**
 * Callback interface for Model
 */
public interface IModel
{
	/** The name given to the model. Must be a short MPASM compatible name */
	String getModelName();
	
	/** Return the initial state - root node */
	Node getInitialState();

	/** Variable lookup */
	Variable getVariable(String name);
	
	/** Return the node with the given name */
	Node getNode(String name);	
	
	/** Does this model require a stack for subroutines? */
	boolean requiresSubroutineStack();
}
