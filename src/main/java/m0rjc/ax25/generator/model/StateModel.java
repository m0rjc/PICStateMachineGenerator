package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import m0rjc.ax25.generator.model.Variable.Ownership;
import m0rjc.ax25.generator.visitor.IModel;
import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * The model used to build the state tree
 * 
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public class StateModel implements IModel
{
	/** A name for the model. Must be short - will be used for symbols */
	private String m_modelName;
	
	/** All nodes in this model */
	private Map<String,Node> m_nodesByName = new HashMap<String, Node>();
	
	/** All variables known to the model */
	private Map<String,Variable> m_variablesByName = new HashMap<String, Variable>();
	
	private List<RomLocation> m_externalRom = new ArrayList<RomLocation>();
	
	/** Variable which holds a counter */
	private Variable m_countVariable;
	
	/** Variable which holds the input value */
	private Variable m_inputVariable;
		
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
	}
	
	/** The name given to the model. Must be a short MPASM compatible name */
	public String getModelName()
	{
		return m_modelName;
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
	public Variable createInternalAccessVariable(String name, int size)
	{
		Variable v = new Variable(name, Ownership.INTERNAL, Variable.ACCESS_BANK, size);
		addVariable(v);
		return v;
	}

	/**
	 * Create a variable based on the given prototype
	 * @param v
	 * @param global
	 */
	public void addVariable(Variable v)
	{
		if(m_variablesByName.containsKey(v.getName()))
		{
			throw new IllegalArgumentException("Variable name " + v.getName() + " already exists");
		}
		m_variablesByName.put(v.getName(),  v);
	}


	/**
	 * Create a global variable in the Access area
	 * @param name
	 * @param size
	 * @return
	 */
	public Variable createGlobalAccessVariable(String name, int size)
	{
		Variable v = new Variable(name, Ownership.GLOBAL, Variable.ACCESS_BANK, size);
		addVariable(v);
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
		Variable v = new Variable(name, Ownership.GLOBAL, page, size);
		addVariable(v);
		return v;
	}
	
	/**
	 * Create a variable in paged memory.
	 * The storage will be declared in the generated code.
	 * @param name name for the variable
	 * @param size size in bytes
	 * @return
	 */
	public Variable createInternalPagedVariable(int page, String name, int size)
	{
		Variable v = new Variable(name, Ownership.INTERNAL, -1, size);
		addVariable(v);
		return v;
	}
		
	/**
	 * Register an external method
	 * @param name
	 */
	public void registerExternalMethod(RomLocation name, boolean requiresExtern)
	{
		if(requiresExtern)
		{
			m_externalRom.add(name);
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
			m_countVariable = createInternalAccessVariable(m_modelName + "_storeCount", 1);
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
	 * Return the Initial State, creating one if needed.
	 * @return
	 */
	@Override
	public Node getInitialState()
	{
		if(m_rootNode == null)
		{
			m_rootNode = createNode();
		}
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
	 * Perform any optimisation of the now complete model.
	 */
	public void optimiseModel()
	{
		// Work out which nodes should use shared entry code
		m_rootNode.setSharedEntryCodeOnMultipleEntryNodes(new HashSet<String>());
	}
	
	/**
	 * Visit the model
	 * @param visitor
	 */
	public void accept(IModelVisitor visitor)
	{
		visitor.visitStartModel(this);
		
		// EXTERN section
		for(RomLocation r : m_externalRom)
		{
			visitor.visitDeclareExternalSymbol(r);
		}
		
		for(Variable v : m_variablesByName.values())
		{
			v.acceptForExtern(visitor);
		}

		// GLOBAL section
		for(Variable v : m_variablesByName.values())
		{
			v.acceptForGlobal(visitor);
		}
		
		// VARIABLES
		visitor.visitStartAccessVariables(hasVariablesToDeclareInPage(-1));
		for(Variable v : m_variablesByName.values())
		{
			v.acceptForDeclaration(visitor, -1);
		}
		
		for(int page = 0; page <= 15; page++)
		{
			visitor.visitStartBankedVariables(page, hasVariablesToDeclareInPage(page));
			for(Variable v : m_variablesByName.values())
			{
				v.acceptForDeclaration(visitor, page);
			}
		}
		
		visitor.visitStartCode();
		m_rootNode.accept(new HashSet<String>(), visitor);
		visitor.finished();
	}

	/** Have I variables to define in the given page? -1 is ACCESS */
	private boolean hasVariablesToDeclareInPage(int page)
	{
		for(Variable v : m_variablesByName.values())
		{
			if(v.isMustDeclareStorage() && v.isInRamPage(page)) return true;
		}
		return false;
	}

	
	@Override
	public boolean requiresSubroutineStack()
	{
		for (Node node : m_nodesByName.values())
		{
			if(node.requiresSubroutineStack()) return true;
		}
		return false;
	}

	/**
	 * Set the Initial Node for the model. This is the default node that each
	 * node will return to if none of its transitions match.
	 */
	public void setRoot(Node node)
	{
		m_rootNode = node;
	}

	/**
	 * Locate the ROM location with the given name.
	 * @param name to look for
	 * @return location, or null if not registered.
	 */
	public RomLocation getRomLocation(String name)
	{
		for(RomLocation l : m_externalRom)
		{
			if(l.getName().equals(name)) return l;
		}
		return null;
	}

}
