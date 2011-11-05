package uk.me.m0rjc.picstategenerator.visitor;

import java.util.Collection;

import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.Variable;

/**
 * Callback interface for Model.
 */
public interface IModel
{
    /** @return the name given to the model. */
    String getModelName();

    /** @return the initial state - root node */
    Node getInitialState();

    /**
     * Variable lookup.
     * 
     * @param name
     *            to look for
     * @return variable with the given name or null for not found.
     */
    Variable getVariable(String name);

    /**
     * @param name
     *            name to look up.
     * @return the node with the given name, or null for not found.
     */
    Node getNode(String name);

    /** @return does this model require a stack for subroutines? */
    boolean requiresSubroutineStack();

    /** @return all defined variables. */
    Collection<Variable> getVariables();
}
