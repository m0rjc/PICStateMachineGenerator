package uk.me.m0rjc.picstategenerator.visitor;

import uk.me.m0rjc.picstategenerator.model.GosubCommand;
import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.RomLocation;
import uk.me.m0rjc.picstategenerator.model.SymbolOwnership;
import uk.me.m0rjc.picstategenerator.model.Transition;
import uk.me.m0rjc.picstategenerator.model.Variable;

/**
 * Visitor interface, to build things based on the model.
 * (A visitor/builder cross)
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public interface IModelVisitor
{
	/**
	 * Start of visiting a model.
	 * @param model the model being visited.
	 */
	void visitStartModel(IModel model);
	
	/**
	 * Declare a symbol that is defined outside the generated module but must be
	 * declared as EXTERNAL in the generated code.
	 * @param variable a variable who's {@link SymbolOwnership} is External.
	 */
	void visitDeclareExternalSymbol(Variable variable);
	
    /**
     * Declare a symbol that is defined outside the generated module but must be
     * declared as EXTERNAL in the generated code.
     * @param name a RomLocation who's {@link SymbolOwnership} is External.
     */
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
	 * Create a variable definition. Include any flags.
	 * @param v Variable to define.
	 */
	void visitCreateVariableDefinition(Variable v);
	
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
     * Start rendering commands to run on entering the node at the end of a
     * state transition. Will be followed by visitCommand* followed by a
     * visitTransitionGoToNode
     * 
     * @param node the node to render shared entry code for.
     */
	void startSharedEntryCode(INode node);
	
	/**
	 * Start a Node.
	 * The Node will be started, all its transitions visited, then the node ended before any other
	 * nodes are visited.
	 * 
	 * The first node to be started is the root node.
	 * @param node the node to start code for.
	 */
	void startNode(INode node);

	/**
	 * Visit a Transition on the current Node.
	 * @param rangeTransition
	 */
	void visitTransition(Transition transition);
	
	/** 
	 * Encode a transition precondition for Greater or Equals. 
	 * If variable &gt;= value then continue, otherwise if possible {@link #pop()} or
	 * continue to the next transition.
	 */
	void visitTransitionPreconditionGE(Variable variable, int value);

	/** 
	 * Encode a transition precondition for Equals.
	 * If variable == value then continue, otherwise if possible {@link #pop()} or
	 * continue to the next transition.
	 */
	void visitTransitionPreconditionEQ(Variable variable, int value);

	/** 
	 * Encode a transition precondition for Less than or Equals.
	 * If variable &lt;= value then continue, otherwise if possible {@link #pop()} or
	 * continue to the next transition.
	 */
	void visitTransitionPreconditionLE(Variable variable, int value);

	/** 
	 * Encode a precondition checking that the given flag has the given value
	 *
	 * If flag:bit == expectedValue then continue, otherwise if possible {@link #pop()} or
	 * continue to the next transition.
	 */
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
	
	/** End of the transition */
	void endTransition(Transition transition);
	
	/**
	 * End of visiting a Node
	 * @param node
	 */
	void endNode(Node node);

	/**
	 * End of visiting
	 */
	void finished();

	/**
	 * Enter a sub-block of code. Equivalent to open brace in C. In Assembler a label will
	 * be created at the corresponding pop(). The location of that label will be used by
	 * the {@link #saveReturnOnStack()} method and the {@link #exitCodeBlock(int)} method.
	 * 
	 * <p>This can be used in condition processing to create a decision tree, and in
	 * subroutine processing to create the {@link GosubCommand GOSUB} command.</p>
	 * 
	 * <p><strong>Client code must ensure that all push and pop balance within a transition.</strong></p>
	 */
	void push();
	
	/**
	 * After a {@link #push()} store the location of the corresponding {@link #pop()} on the
	 * subroutine stack.
	 * 
	 * <p>Usage:</p>
	 * <pre>
	 * push()
	 *    saveReturnOnSubroutineStack() // Saves LABEL1
	 *    {@link #visitTransitionGoToSharedEntryCode(INode) visitTransitionGoToSharedEntryCode}(subroutineNode)
	 * pop() // LABEL1
	 * </pre>
	 * 
	 * @see GosubCommand
	 */
	void saveReturnOnSubroutineStack();

	/**
	 * After a {@link #push()} GOTO the corresponding POP if levels = 0. If levels is more than
	 * 0 then more levels will be exited. This is like the break command in Java and C.
	 * 
	 * <p>Examples:</p>
	 * <pre>
	 * push()
	 *   exitCodeBlock(0)  // GOTO LABEL1
	 *   push()
	 *     exitCodeBlock(0)   // GOTO LABEL2
	 *     exitCodeBlock(1)   // GOTO LABEL1
	 *   pop()  // LABEL2
	 * pop()   // LABEL1
	 * </pre>
	 * 
	 * @param levels 0 for the current level, more for greater depths of local code blocks.
	 */
	void exitCodeBlock(int levels);
	
	/**
	 * Exit a sub-block of code. Equivalent to close brace in Java and C. In Assembler a label
	 * will be created, its name determined by the corresponding {@link #push()}.
	 */
	void pop();

	/**
	 * GOTO the location held on the Subroutine Stack.
	 * This will have been saved by a previous {@link #saveReturnOnSubroutineStack()}
	 */
	void visitTransitionReturnFromSubroutineStack();
}
