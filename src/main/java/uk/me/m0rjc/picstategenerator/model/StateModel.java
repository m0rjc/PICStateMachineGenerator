package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import uk.me.m0rjc.picstategenerator.visitor.IModel;
import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

/**
 * The model used to build the state tree.
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class StateModel implements IModel
{
    /** Assumed amount of RAM pages supported. */
    private static final int MAX_RAM_PAGES = 15;

    /** A name for the model. Must be short - will be used for symbols */
    private String m_modelName;

    /** All nodes in this model keyed on node name. */
    private Map<String, Node> m_nodesByName = new HashMap<String, Node>();

    /** All variables known to the model keyed on variable name. */
    private Map<String, Variable> m_variablesByName = new HashMap<String, Variable>();

    /** ROM locations for reference by the model. */
    private List<RomLocation> m_externalRom = new ArrayList<RomLocation>();

    /** Variable which holds a counter used for the "Numbers Nodes". */
    private Variable m_countVariable;

    /** Variable which holds the input value. */
    private Variable m_inputVariable;

    /** Root node of the model. */
    private Node m_rootNode;

    /** Counter for creating unique node names. */
    private int m_stateIndex;

    /**
     * Construct the model.
     * 
     * @param modelName
     *            A short, MPASM compatible symbol name, about 3 characters, used in symbols
     */
    public StateModel(final String modelName)
    {
        m_modelName = modelName;
    }

    /** @return The name given to the model. Must be a short MPASM compatible name */
    public final String getModelName()
    {
        return m_modelName;
    }

    /**
     * @return Create a unique name for a state.
     */
    private String createStateName()
    {
        return String.format("%s_State_%04d", m_modelName, m_stateIndex++);
    }

    /**
     * @return Create a new node with a unique name.
     */
    public final Node createNode()
    {
        return createNamedNode(createStateName());
    }

    /**
     * Create a node with the given name.
     * 
     * @param name name for the node.
     * @return the new node.
     */
    public final Node createNamedNode(final String name)
    {
        Node node = new Node(name, this);
        m_nodesByName.put(name, node);
        return node;
    }

     /**
     * Add a variable to the model.
     * 
     * @param v variable to add
     * @throws IllegalArgumentException is the name is not unique.
     */
    public final void addVariable(final Variable v)
    {
        if (m_variablesByName.containsKey(v.getName()))
        {
            throw new IllegalArgumentException("Variable name " + v.getName()
                    + " already exists");
        }
        m_variablesByName.put(v.getName(), v);
    }

    /**
     * Register an external method.
     * 
     * @param name the ROM symbol.
     */
    public final void registerExternalMethod(final RomLocation name)
    {
        m_externalRom.add(name);
    }

    /**
     * @return the variable used for counters. It will be created on demand when this method is called.
     */
    public Variable getCountVariable()
    {
        if (m_countVariable == null)
        {
            String name = m_modelName + "_storeCount";
            m_countVariable = new Variable(name, SymbolOwnership.INTERNAL, Variable.ACCESS_BANK, 1); 
            addVariable(m_countVariable);
        }
        return m_countVariable;
    }

    /**
     * @param v the variable that will contain input to the state engine.
     * @throws IllegalStateException if the variable has already been set.
     */
    public void setInputVariable(final Variable v)
    {
        if (m_inputVariable != null)
        {
            throw new IllegalStateException("Input variable " + m_inputVariable
                    + " has already been set");
        }
        m_inputVariable = v;
    }

    /**
     * @return the variable that will contain input to the state engine.
     * @throws IllegalStateException if none has been set.
     */
    public Variable getInputVariable()
    {
        if (m_inputVariable == null)
        {
            throw new IllegalStateException(
                    "Input variable has not been set for this model");
        }
        return m_inputVariable;
    }

    /**
     * @param name name to look up.
     * @return the variable with the given name.
     */
    public Variable getVariable(final String name)
    {
        return m_variablesByName.get(name);
    }
    
    /**
     * @return all variables.
     */
    @Override
    public Collection<Variable> getVariables()
    {
        return m_variablesByName.values();
    }

    /**
     * @return the Initial State, creating one if needed.
     */
    @Override
    public Node getInitialState()
    {
        if (m_rootNode == null)
        {
            m_rootNode = createNode();
        }
        return m_rootNode;
    }

    /**
     * @param name name to look up
     * @return the node with that name, or null if none found.
     */
    @Override
    public Node getNode(final String name)
    {
        return m_nodesByName.get(name);
    }

    /**
     * Perform any optimisation of the now complete model.
     */
    public void optimiseModel()
    {
        m_rootNode.setSharedEntryCodeOnMultipleEntryNodes(new HashSet<String>());
    }

    /**
     * Visit the model.
     * 
     * @param visitor
     *            callback to build the final module and header files.
     */
    public void accept(final IModelVisitor visitor)
    {
        visitor.visitStartModel(this);

        // EXTERN section
        for (RomLocation r : m_externalRom)
        {
            if(r.isMustImport())
            {
                visitor.visitDeclareExternalSymbol(r);
            }
        }

        for (Variable v : m_variablesByName.values())
        {
            v.acceptForExtern(visitor);
        }

        // GLOBAL section
        for (Variable v : m_variablesByName.values())
        {
            v.acceptForGlobal(visitor);
        }

        // VARIABLES
        visitor.visitStartAccessVariables(hasVariablesToDeclareInPage(-1));
        for (Variable v : m_variablesByName.values())
        {
            v.acceptForDeclaration(visitor, -1);
        }

        for (int page = 0; page <= MAX_RAM_PAGES; page++)
        {
            visitor.visitStartBankedVariables(page,
                    hasVariablesToDeclareInPage(page));
            for (Variable v : m_variablesByName.values())
            {
                v.acceptForDeclaration(visitor, page);
            }
        }

        visitor.visitStartCode();
        m_rootNode.accept(new HashSet<String>(), visitor);
        visitor.finished();
    }

    /** 
     * @return have I variables to define in the given page?
     * @param page page to look up. -1 ({@link Variable#ACCESS_BANK}) is Access.
     */
    private boolean hasVariablesToDeclareInPage(final int page)
    {
        for (Variable v : m_variablesByName.values())
        {
            if (v.isMustDeclareStorage() && v.isInRamPage(page))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean requiresSubroutineStack()
    {
        for (Node node : m_nodesByName.values())
        {
            if (node.requiresSubroutineStack())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the Initial Node for the model. This is the default node that each
     * node will return to if none of its transitions match.
     * 
     * @param node the Root Node.
     */
    public void setRoot(final Node node)
    {
        m_rootNode = node;
    }

    /**
     * Locate the ROM location with the given name.
     * 
     * @param name
     *            to look for
     * @return location, or null if not registered.
     */
    public RomLocation getRomLocation(final String name)
    {
        for (RomLocation l : m_externalRom)
        {
            if (l.getName().equals(name))
            {
                return l;
            }
        }
        return null;
    }

}
