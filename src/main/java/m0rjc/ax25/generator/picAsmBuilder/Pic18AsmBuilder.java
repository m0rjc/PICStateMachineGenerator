package m0rjc.ax25.generator.picAsmBuilder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.RomLocation;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.model.Variable;
import m0rjc.ax25.generator.model.Variable.Ownership;
import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Build assembly source code for the PIC18
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class Pic18AsmBuilder implements IModelVisitor
{
	private Logger m_log = Logger.getLogger(Pic18AsmBuilder.class.getName());
	private PicAssemblyWriter m_assembler;
	private boolean m_hasBankedVariables;
	private boolean m_hasOutputEntryPointGlobal;
	private Variable m_statePointer;
	private boolean m_lastTransitionCanFallThrough;
	private String m_rootStateName;
	private int m_numberOfBanksUsed = 0;
	
	/** Code to execute to exit from the step method */
	private List<String> m_returnCode;
	
	/** True to generate a large ROM model */
	private boolean m_largeRomModel;
	
	/** Bank currently BANKSEL if known */
	private int m_currentBank = -1;
	
	/** Counter for creating transition label names */
	private int m_transitionCounter = 0;
	
	private String m_modelName;
	
	/**
	 * Declare an external symbol
	 * @param name
	 */
	@Override
	public void visitDeclareExternalSymbol(Variable name)
	{
		m_assembler.opCode("EXTERN", name.getName());
	}

	@Override
	public void visitDeclareExternalSymbol(RomLocation name)
	{
		m_assembler.opCode("EXTERN", name.getName());
	}

	/**
	 * Declare a global symbol - defined in this module to be exported
	 * @param name
	 */
	@Override
	public void visitDeclareGlobalSymbol(String name)
	{
		if(!m_hasOutputEntryPointGlobal)
		{
			outputEntryPointGlobals();
		}
		m_assembler.opCode("GLOBAL", name);
	}

	/** Output globals for the module entry points */
	public void outputEntryPointGlobals()
	{
		m_hasOutputEntryPointGlobal = true;
		m_assembler.opCode("GLOBAL", getInitMethodName());
		m_assembler.opCode("GLOBAL", getStepMethodName());
	}
	
	/**
	 * Declare the start of access variable definitions
	 */
	@Override
	public void visitStartAccessVariables(boolean modelDefinesAccessVariables)
	{
		if(!m_hasOutputEntryPointGlobal)
		{
			outputEntryPointGlobals();
		}

		if(modelDefinesAccessVariables)
		{
			m_assembler.writeSection(m_modelName + "Acs", "UDATA_ACS");
			if(m_statePointer == null)
			{
				m_statePointer = new Variable("_statePtr", Ownership.INTERNAL, Variable.ACCESS_BANK, m_largeRomModel ? 3 : 2);
				visitCreateVariableDefinition(m_statePointer.getName(), m_statePointer.getSize());
			}
		}
	}

	/**
	 * Public name of the initialise method
	 * @return
	 */
	private String getInitMethodName()
	{
		return m_modelName + "Init";
	}

	/**
	 * Public name of the step method
	 * @return
	 */
	private String getStepMethodName()
	{
		return m_modelName + "Step";
	}

	/**
	 * Create a variable definition
	 * @param name
	 * @param size
	 */
	@Override
	public void visitCreateVariableDefinition(String name, int size)
	{
		m_assembler.ramResourceAllocation(name, size);
	}

	/**
	 * Create a #define for a flag bit
	 * @param name
	 * @param bit
	 */
	@Override
	public void visitCreateFlagDefinition(String name, int bit)
	{
		// Nothing to do
	}
	
	/**
	 * Declare the start of banked variable definition
	 * @param bankNumber
	 */
	@Override
	public void visitStartBankedVariables(int bankNumber, boolean modelDefinesVariablesInThisBank)
	{
		if(modelDefinesVariablesInThisBank)
		{
			m_numberOfBanksUsed++;
			m_assembler.blankLine();
			m_assembler.writeComment("Please set up the linker to locate this block as required.");
			m_assembler.writeSection(m_modelName + "Bank" + bankNumber, "UDATA");
			if(m_statePointer == null)
			{
				m_statePointer = new Variable("_statePtr", Ownership.INTERNAL, bankNumber, m_largeRomModel ? 3 : 2);
				visitCreateVariableDefinition(m_statePointer.getName(), m_statePointer.getSize());
			}
			
			if(!m_hasBankedVariables)
			{
				m_log.log(Level.INFO, "The generated module has banked variables. Please configure the linker.");
				m_hasBankedVariables = true;
			}
		}		
	}
	
	/**
	 * Declare the start of code.
	 * The builder may wish to output any boilerplate code here.
	 */
	@Override
	public void visitStartCode()
	{
		if(m_statePointer == null)
		{
			// Place it in ACCESS
			visitStartAccessVariables(true);
		}
		
		m_assembler.blankLine();
		m_assembler.writeSection(m_modelName + "Code", "CODE");
		writeInitMethod();
		writeProcessStateMethod();
	}

	/** Output the method that initialised the state machine */
	private void writeInitMethod()
	{
		clearBankSel();
		m_assembler.blankLine();
		m_assembler.startBlockComment();
		m_assembler.writeBlockCommentLine("State model initialisation.");
		m_assembler.endBlockComment();
		
		m_assembler.writeLabel(getInitMethodName());
		setPointer(m_statePointer, m_rootStateName);
		m_assembler.opCode("RETURN");
	}

	
	/** Output the method that processess the current state */
	private void writeProcessStateMethod()
	{
		clearBankSel();
		m_assembler.blankLine();
		m_assembler.startBlockComment();
		m_assembler.writeBlockCommentLine("State model entry point.");
		m_assembler.writeBlockCommentLine("This method executes the code for the current state.");
		m_assembler.endBlockComment();
		
		m_assembler.writeLabel(getStepMethodName());
		gotoPointer(m_statePointer);
		
	}
	
	/**
	 * Start a Node.
	 * The Node will be started, all its transitions visited, then the node ended before any other
	 * nodes are visited.
	 * 
	 * The first node to be started is the root node.
	 * @param node
	 */
	public void startNode(Node node)
	{
		clearBankSel();
		m_assembler.blankLine();
		m_assembler.startBlockComment();
		m_assembler.writeBlockCommentLine("Node " + node.getStateName());
		m_assembler.endBlockComment();
		m_assembler.writeLabel(node.getStateName());
		m_lastTransitionCanFallThrough = true;
	}

	/**
	 * Visit a Transition on the current Node
	 * @param rangeTransition
	 */
	public void visitTransition(Transition transition)
	{
		m_assembler.writeComment("-- Start of transition --");
		m_assembler.writeLabel(getNextTransitionLabel());
		
		// At the moment we don't cleverly work out routes through the 
		// conditions, so if more than 1 bank is used we are paranoid and
		// banksel after any branch.
		if(m_numberOfBanksUsed > 1) clearBankSel();
		
		// Advances next transition label
		m_transitionCounter++;
	}

	/** 
	 * Get the lable for the transition referenced by m_transitionCounter.
	 * This will be the next transition.
	 * @return
	 */
	private String getNextTransitionLabel()
	{
		return "trans" + m_transitionCounter;
	}
	
	/** Encode a transition precondition for Greater or Equals. Variable may need substitution */
	public void visitTransitionPreconditionGE(Variable variable, int value)
	{
		m_assembler.writeComment(String.format(" Precondition %s >= %d", variable.getName(), value));
		if(value != 0)
		{
			banksel(variable);
			m_assembler.opCode("MOVLW", formatInt(value - 1));
			m_assembler.opCode("CPFSGT", variable.getName(), access(variable));
			m_assembler.opCode("GOTO", getNextTransitionLabel());
		}
	}

	/** Encode a transition precondition for Equals */
	public void visitTransitionPreconditionEQ(Variable variable, int value)
	{
		m_assembler.writeComment(String.format(" Precondition %s == %d", variable.getName(), value));
		banksel(variable);
		m_assembler.opCode("MOVLW", formatInt(value));
		m_assembler.opCode("CPFSEQ", variable.getName(), access(variable));
		m_assembler.opCode("GOTO", getNextTransitionLabel());
	}

	/** Encode a transition precondition for Less than or Equals */
	public void visitTransitionPreconditionLE(Variable variable, int value)
	{
		m_assembler.writeComment(String.format(" Precondition %s <= %d", variable.getName(), value));
		if(value < 255)
		{
			banksel(variable);
			m_assembler.opCode("MOVLW", formatInt(value + 1));
			m_assembler.opCode("CPFSLT", variable.getName(), access(variable));
			m_assembler.opCode("GOTO", getNextTransitionLabel());
		}
	}

	/** Encode a precondition checking that the given flag has the given value */
	public void visitTransitionPreconditionFlag(Variable flag, int bit, boolean expectedValue)
	{
		m_assembler.writeComment(String.format(" Precondition Flag %s:%d == %b", flag.getName(), bit, expectedValue));
		int offset = bit / 8;
		bit = bit % 8;
		String opCode = expectedValue ? "BTFSS" : "BTFSC";
		banksel(flag);
		m_assembler.opCode(opCode,  offset(flag, offset), Integer.toString(bit), access(flag));
		m_assembler.opCode("GOTO", getNextTransitionLabel());			
	}
		
	/** Encode storing the input at the given variable+indexed offset location. */
	public void visitCommandCopyVariableToIndexedVariable(Variable source, Variable output, Variable indexer)
	{
		if(source.getSize() > 1) m_log.warning("Indexed variable handling code currently only supports 8 bit values. Variable: " + source.getName());

		m_assembler.writeComment(String.format(" Command %s[%s] := %s", output.getName(), indexer.getName(), source.getName()));
		m_assembler.opCode("LFSR0", output.getName());
		banksel(indexer);
		m_assembler.opCode("MOVF", indexer.getName(), "W", access(indexer));
		m_assembler.opCode("MOVFF", source.getName(), "PLUSW0");
	}

	/** Encode copy input to output */
	public void visitCommandCopyVariable(Variable input, Variable output)
	{
		m_assembler.writeComment(String.format(" Command %s := %s", output.getName(), input.getName()));
		for(int i = 0; i < input.getSize() && i < output.getSize(); i++)
		{
			m_assembler.opCode("MOVFF", offset(input, i), offset(output, i));
		}
	}
	
	/** Encode clearing a variable's value. */
	public void visitCommandClearVariable(Variable variable)
	{
		m_assembler.writeComment(String.format(" Command %s := 0", variable.getName()));
		banksel(variable);
		for(int i = 0; i < variable.getSize(); i++)
		{
			m_assembler.opCode("CLRF", offset(variable, i), access(variable));
		}		
	}
	
	/** Encode clearing a variable's value using indexing. */
	public void visitCommandClearIndexedVariable(Variable variable, Variable indexer)
	{
		m_assembler.writeComment(String.format(" Command %s[%s] := 0", variable.getName(), indexer.getName()));
		m_assembler.opCode("LFSR0", variable.getName());
		banksel(indexer);
		m_assembler.opCode("MOVF", indexer.getName(), "W", access(indexer));
		m_assembler.opCode("CLRF", "PLUSW0", "A");

	}
	
	/** Encode incrementing a variable's value */
	public void visitCommandIncrementVariable(Variable variable)
	{
		m_assembler.writeComment(String.format(" Command %s++", variable.getName()));
		banksel(variable);
		m_assembler.opCode("INCF", variable.getName(), "F", access(variable));
		for(int i = 1; i < variable.getSize(); i++)
		{
			m_assembler.opCode("BTFSC", "STATUS", "C", "A");
			m_assembler.opCode("INCF", offset(variable, i), "F", access(variable));
		}
	}

	/** Encode a command to set or clear a flag. If bit is more than 7 then more than one byte is used. */
	public void visitCommandSetFlag(Variable flags, int bit, boolean newValue)
	{
		m_assembler.writeComment(String.format(" Command %s:%d := %b", flags.getName(), bit, newValue));
		int offset = bit / 8;
		bit = bit % 8;
		String opCode = newValue ? "BSF" : "BCF";
		banksel(flags);
		m_assembler.opCode(opCode, offset(flags, offset), Integer.toString(bit), access(flags));
	}

	/** Encode a CALL to the given method */
	public void visitCommandMethodCall(RomLocation method)
	{
		m_assembler.writeComment(" Command CALL " + method.getName());
		m_assembler.opCode("CALL", method.getName());
	}
	
	/** Encode a "Go to named node and return control" in the transition */
	public void visitTransitionGoToNode(String stateName)
	{
		m_assembler.writeComment(" Transition GOTO " + stateName);
		setPointer(m_statePointer, stateName);
		
		if(m_returnCode != null && m_returnCode.size() > 0)
		{
			for(String returnStatement : m_returnCode)
			{
				m_assembler.opCode(returnStatement);
			}
		}
		else
		{
			m_assembler.opCode("RETURN");
		}
	}
	
	/**
	 * End of visiting a Node.
	 * Write the fallthrough to the default state.
	 * @param node
	 */
	public void endNode(Node node)
	{
		if(m_lastTransitionCanFallThrough)
		{
			visitTransitionGoToNode(m_rootStateName);
		}
	}

	/**
	 * End of visiting
	 */
	@Override
	public void finished()
	{
		m_assembler.writeEndMarker();
	}

	/**
	 * Write instructions to go to the location in the given little-endian pointer
	 * @param pointer
	 */
	private void gotoPointer(Variable pointer)
	{
		if(m_largeRomModel)
		{
			m_assembler.opCode("MOVFF", offset(pointer, 2), "PCLATU");
		}
		else
		{
			m_assembler.opCode("CLRF", "PCLATU", "A");
		}
		
		m_assembler.opCode("MOVFF", offset(pointer, 1), "PCLATH");
		m_assembler.opCode("MOVFF", offset(pointer, 0), "PCL");
	}
	
	/**
	 * Write instructions to populate the little-endian pointer with the given label
	 * @param pointer
	 * @param label
	 */
	private void setPointer(Variable pointer, String label)
	{
		banksel(pointer);
		if(m_largeRomModel)
		{
			m_assembler.opCode("MOVLW", "UPPER(" + label + ")");
			m_assembler.opCode("MOVWF", offset(pointer, 2), access(pointer));
		}

		m_assembler.opCode("MOVLW", "HIGH(" + label + ")");
		m_assembler.opCode("MOVWF", offset(pointer, 1), access(pointer));

		m_assembler.opCode("MOVLW", "LOW(" + label + ")");
		m_assembler.opCode("MOVWF", offset(pointer, 0), access(pointer));
	}
	
	/** If the current bank is not that of the variable, and the variable is
	 * not access bank, output a BANKSEL command.
	 * 
	 * @see #clearBankSel
	 */
	private void banksel(Variable pointer)
	{
		int bank = pointer.getBank();
		if(!pointer.isAccess() && bank != m_currentBank)
		{
			m_assembler.opCode("BANKSEL", pointer.getName());
			m_currentBank = bank;
		}
	}
	
	/**
	 * The current bank is unknown - so a banksel will be issued for any
	 * banked access.
	 */
	private void clearBankSel()
	{
		m_currentBank = -1;
	}

	/** Return the location of the variable with the given offset for use in assembler */
	protected String offset(Variable v, int offset)
	{
		if(offset != 0)
		{
			return String.format("(%s + .%d)", v.getName(), offset);
		}
		return v.getName();
	}

	/**
	 * Return the MPASM access/bank flag (A or B) for the variable
	 * @param v
	 * @return
	 */
	private String access(Variable v)
	{
		return v.isAccess() ? "A" : "B";
	}
	
	/**
	 * Format an integer for MPASM
	 * @param x
	 * @return
	 */
	private String formatInt(int x)
	{
		return "." + x;
	}

}
