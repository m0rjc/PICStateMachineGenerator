package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import m0rjc.ax25.generator.visitor.IModel;
import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * The model used to build the state tree
 * 
 * @author Richard Corfield
 */
public class StateModel implements IModel
{
	/** A name for the model. Must be short - will be used for symbols */
	private String m_modelName;
	
	/** Code needed to return from the state machine */
	private String[] m_returnCode;
	
	/** All nodes in this model */
	private Map<String,Node> m_nodesByName = new HashMap<String, Node>();
	
	/** All variables known to the model */
	private Map<String,Variable> m_variablesByName = new HashMap<String, Variable>();
	
	/** Variables I need to register in the access bank */
	private List<Variable> m_accessBankVariables = new ArrayList<Variable>();
	
	/** Variables I need to register in paged memory */
	private List<Variable> m_pagedVariables = new ArrayList<Variable>();
	
	/** Variable which holds a counter */
	private Variable m_countVariable;
	
	/** Variable which holds the input value */
	private Variable m_inputVariable;
	
	/** Symbols I export */
	private List<String> m_globalSymbols = new ArrayList<String>();
	
	/** Symbols I import */
	private List<Variable> m_externVariables = new ArrayList<Variable>();
	private List<RomLocation> m_externRom = new ArrayList<RomLocation>();
	
	/** Root node of the model. */
	private Node m_rootNode;
	
	/** Counter for creating unique node names */
	private int m_stateIndex;
	
	/**
	 * Construct the model
	 * @param name A short name, about 3 characters, used in symbols
	 */
	public StateModel(String name)
	{
		m_modelName = name;
		m_rootNode = createNode();
	}
	
	/**
	 * Create a name for a state
	 */
	private String createStateName()
	{
		return String.format("%s_State_%04d", m_modelName, m_stateIndex++);
	}
	
	/**
	 * Create a node with a generated name.
	 * @return
	 */
	public Node createNode()
	{
		return createNamedNode(createStateName());
	}

	/**
	 * Create a node with the given name
	 * @param string
	 * @return
	 */
	public Node createNamedNode(String name)
	{
		Node node = new Node(name, this);
		m_nodesByName.put(name, node);
		return node;
	}
	
	/**
	 * Create a variable in the access bank.
 	 * The storage will be declared in the generated code.
	 * @param name name for the variable
	 * @param size size in bytes
	 * @return
	 */
	public Variable createAccessVariable(String name, int size)
	{
		if(m_variablesByName.containsKey(name))
		{
			throw new IllegalArgumentException("Variable name " + name + " already exists");
		}
		Variable v = Variable.accessVariable(name, size);
		m_variablesByName.put(name,  v);
		m_accessBankVariables.add(v);
		return v;
	}

	/**
	 * Create a global variable in the Access area
	 * @param name
	 * @param size
	 * @return
	 */
	public Variable createGlobalAccessVariable(String name, int size)
	{
		Variable v = createAccessVariable(name, size);
		makeVariableGlobal(v);
		return v;
	}

	
	/**
	 * Create a variable in paged memory.
	 * The storage will be declared in the generated code and made GLOBAL.
	 * @param page
	 * @param name
	 * @param size
	 * @return
	 */
	public Variable createGlobalPagedVariable(int page, String name, int size)
	{
		Variable v = createPagedVariable(page, name, size);
		makeVariableGlobal(v);
		return v;
	}
	
	/**
	 * Create a variable in paged memory.
	 * The storage will be declared in the generated code.
	 * @param name name for the variable
	 * @param size size in bytes
	 * @return
	 */
	public Variable createPagedVariable(int page, String name, int size)
	{
		if(m_variablesByName.containsKey(name))
		{
			throw new IllegalArgumentException("Variable name " + name + " already exists");
		}
		Variable v = Variable.pagedVariable(page, name, size);
		m_variablesByName.put(name,  v);
		m_pagedVariables.add(v);
		return v;
	}
	
	/**
	 * Make a variable global, so it can be seen by external modules.
	 * @param v
	 */
	public void makeVariableGlobal(Variable v)
	{
		m_globalSymbols.add(v.getName());
	}
	
	/**
	 * Register the existence of an externally defined variable.
	 * @param requiresExtern false if it's declared in an included header, for example and SFR
	 */
	public void registerExternalVariable(Variable v, boolean requiresExtern)
	{
		String name = v.getName();
		if(m_variablesByName.containsKey(name))
		{
			throw new IllegalArgumentException("Variable name " + name + " already exists");			
		}
		m_variablesByName.put(name, v);
		if(requiresExtern)
		{
			m_externVariables.add(v);
		}
	}
	
	/**
	 * Register an external method
	 * @param name
	 */
	public void registerExternalMethod(RomLocation name, boolean requiresExtern)
	{
		if(requiresExtern)
		{
			m_externRom.add(name);
		}
	}

	/**
	 * Get the variable used for counters
	 * @return
	 */
	public Variable getCountVariable()
	{
		if(m_countVariable == null)
		{
			m_countVariable = createAccessVariable(m_modelName + "_storeCount", 1);
		}
		return m_countVariable;
	}
	
	/**
	 * Set the variable that will contain input to the state engine.
	 */
	public void setInputVariable(Variable v)
	{
		if(m_inputVariable != null)
		{
			throw new IllegalStateException("Input variable " + m_inputVariable + " has already been set");
		}
		m_inputVariable = v;
	}
	
	/** The variable that will contain input to the state engine */
	public Variable getInputVariable()
	{
		if(m_inputVariable == null)
		{
			throw new IllegalStateException("Input variable has not been set for this model");
		}
		return m_inputVariable;		
	}
	
	/** Return the variable with the given name */
	public Variable getVariable(String name)
	{
		return m_variablesByName.get(name);
	}
	
	/**
	 * Set the code that will be used to return from a state execution.
	 * For example <code>RETLW 0</code>
	 * @param lines  lines of assembler to use to return once a state has finished.
	 */
	public void setReturnCode(String ...  lines)
	{
		m_returnCode = lines;
	}

	/**
	 * Return the code segment to be used to return from state operations.
	 * @return
	 */
	@Override
	public String[] getReturnCode()
	{
		return m_returnCode;
	}
	
	/**
	 * Return the Intial State
	 * @return
	 */
	@Override
	public Node getInitialState()
	{
		return m_rootNode;
	}
	
	/**
	 * Return a node by name
	 * @param name
	 * @return
	 */
	@Override
	public Node getNode(String name)
	{
		return m_nodesByName.get(name);
	}
	
	/**
	 * Visit the model
	 * @param visitor
	 */
	public void accept(IModelVisitor visitor)
	{
		for(Variable v : m_externVariables)
		{
			visitor.visitDeclareExternalSymbol(v);
		}

		for(RomLocation r : m_externRom)
		{
			visitor.visitDeclareExternalSymbol(r);
		}

		
		for(String name : m_globalSymbols)
		{
			visitor.visitDeclareGlobalSymbol(name);
		}

		visitor.visitStartAccessVariables(!m_accessBankVariables.isEmpty());
		for(Variable v : m_accessBankVariables)
		{
			v.accept(visitor);
		}
		
		// Deliberately very wrong so it will fail a test until I fix it
		visitor.visitStartBankedVariables(10, !m_pagedVariables.isEmpty());
		for(Variable v : m_pagedVariables)
		{
			v.accept(visitor);
		}
		
		visitor.visitStartCode();
		
		// TODO: I think I'm going to have to encode variables specially
		m_rootNode.accept(new HashSet<String>(), visitor);
		visitor.finished();
	}

}
