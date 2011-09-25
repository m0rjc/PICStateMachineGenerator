package m0rjc.ax25.generator.visitor;

import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.RomLocation;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.model.Variable;

/**
 * Visitor interface, to build things based on the model.
 * (A visitor/builder cross)
 * 
 * @author Richard Corfield
 */
public interface IModelVisitor
{
	/**
	 * Start of visiting a model
	 * @param model
	 */
	void visitStartModel(IModel model);
	
	/**
	 * Declare an external symbol
	 * @param name
	 */
	void visitDeclareExternalSymbol(Variable name);
	void visitDeclareExternalSymbol(RomLocation name);

	/**
	 * Declare a global symbol - defined in this module to be exported
	 * @param name
	 */
	void visitDeclareGlobalSymbol(String name);
	
	/**
	 * Declare the start of access variable definitions
	 */
	void visitStartAccessVariables(boolean modelDefinesAccessVariables);
	
	/**
	 * Create a variable definition
	 * @param name
	 * @param size
	 */
	void visitCreateVariableDefinition(String name, int size);

	/**
	 * Create a #define for a flag bit
	 * @param name
	 * @param bit
	 */
	void visitCreateFlagDefinition(String name, int bit);
	
	/**
	 * Declare the start of banked variable definition
	 * @param bankNumber
	 */
	void visitStartBankedVariables(int bankNumber, boolean modelDefinesVariablesInThisBank);
	
	/**
	 * Declare the start of code.
	 * The builder may wish to output any boilerplate code here.
	 */
	void visitStartCode();

	/**
	 * Start rendering commands to run on entering the node at the end of a state transition.
	 * Will be followed by visitCommand* followed by a visitTransitionGoToNode
	 * @param node
	 */
	void startSharedEntryCode(INode node);
	
	/**
	 * Start a Node.
	 * The Node will be started, all its transitions visited, then the node ended before any other
	 * nodes are visited.
	 * 
	 * The first node to be started is the root node.
	 * @param node
	 */
	void startNode(INode node);

	/**
	 * Visit a Transition on the current Node
	 * @param rangeTransition
	 */
	void visitTransition(Transition transition);
	
	/** Encode a transition precondition for Greater or Equals. Variable may need substitution */
	void visitTransitionPreconditionGE(Variable variable, int value);

	/** Encode a transition precondition for Equals */
	void visitTransitionPreconditionEQ(Variable variable, int value);

	/** Encode a transition precondition for Less than or Equals */
	void visitTransitionPreconditionLE(Variable variable, int value);

	/** Encode a precondition checking that the given flag has the given value */
	void visitTransitionPreconditionFlag(Variable flag, int bit, boolean expectedValue);
		
	/** Encode storing the input at the given variable+indexed offset location. */
	void visitCommandCopyVariableToIndexedVariable(Variable source, Variable output, Variable indexer);

	/** Encode copy input to output */
	void visitCommandCopyVariable(Variable input, Variable output);
	
	/** Encode clearing a variable's value. */
	void visitCommandClearVariable(Variable variable);
	
	/** Encode clearing a variable's value using indexing. */
	void visitCommandClearIndexedVariable(Variable variable, Variable indexer);
	
	/** Encode incrementing a variable's value */
	void visitCommandIncrementVariable(Variable variable);	

	/** Encode a command to set or clear a flag. If bit is more than 7 then more than one byte is used. */
	void visitCommandSetFlag(Variable flags, int bit, boolean newValue);

	/** Encode a CALL to the given method */
	void visitCommandMethodCall(RomLocation method);
	
	/** Encode a "Go to named node" using shared entry code. */
	void visitTransitionGoToSharedEntryCode(INode node);
	
	/** Encode a "Go to named node and return control" in the transition */
	void visitTransitionGoToNode(INode node);
	
	/**
	 * End of visiting a Node
	 * @param node
	 */
	void endNode(Node node);

	/**
	 * End of visiting
	 */
	void finished();

}
