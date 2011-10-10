package m0rjc.ax25.generator.model;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * A command we can ask to be built.
 * Commands are immutable, so instances can be reused.
 * 
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public abstract class Command
{
	/**
	 * Render this command using the visitor.
	 * @param model model being rendered
	 * @param visitor that which shall render it.
	 */
	public abstract void accept(StateModel model, IModelVisitor visitor);

	/**
	 * Convenience method to create a Clear Value command
	 * @param value
	 * @return
	 */
	public static Command clearValue(final Variable value)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandClearVariable(value);
			}
		};		
	}

	/**
	 * Convenience method to create a Clear Indexed Value command
	 * @param value
	 * @return
	 */
	public static Command clearIndexedValue(final Variable value, final Variable indexer)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandClearIndexedVariable(value, indexer);
			}
		};		
	}

	
	/**
	 * Convenience method to create a "Store Value" command
	 * @param input
	 * @param output
	 * @return
	 */
	public static Command storeValue(final Variable input, final Variable output)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandCopyVariable(input, output);
			}
		};
	}

	/**
	 * Convenience method to create an "Increment Value" command
	 * @param input
	 * @param output
	 * @return
	 */
	public static Command incrementValue(final Variable value)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandIncrementVariable(value);
			}
		};		
	}
	
	/**
	 * Convenience method to create a "Store Value at indexed location" command
	 * @return
	 */
	public static Command storeValueIndex(final Variable input, final Variable output, final Variable outputIndexer)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandCopyVariableToIndexedVariable(input, output, outputIndexer);
			}
		};		
	}

	/**
	 * Convenience method to create a "Set Flag" command
	 * @param flags variable holding the flags
	 * @param flagName name of the flag within the variable
	 * @param newValue expected value
	 * @return
	 */
	public static Command setFlag(final Variable flags, final String flagName, final boolean newValue)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandSetFlag(flags, flags.getBit(flagName), newValue);
			}
		};
	}
	
	/**
	 * Create a command to call a method.
	 * The method name must have been registered with the model as an external symbol.
	 * @param methodName
	 *
	 * @return
	 */
	public static Command call(final RomLocation method)
	{
		return new Command() {
			@Override
			public void accept(StateModel model, IModelVisitor visitor)
			{
				visitor.visitCommandMethodCall(method);
			}		
		};
	}
	
	
	/**
	 * Create a command to make the state machine enter a subroutine.
	 * The instruction after this command will be stored as the return address.
	 * A command to go to the named node will be rendered.
	 * 
	 * @param stateName Node to call as a subroutine.
	 * @return the Command instance giving that command.
	 */
	public static Command subroutine(String stateName)
	{
		return null;
	}

	/**
	 * Does this command require the subroutine stack?
	 * The default implementation returns false.
	 */
	public boolean requiresSubroutineStack()
	{
		return false;
	}

	/**
	 * If this command calls a node then return it here.
	 * Otherwise return null.
	 */
	public String getTargetNode()
	{
		return null;
	}
}
