package m0rjc.ax25.generator.model;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * A command we can ask to be built.
 * 
 * Commands are immutable, so instances can be reused.
 */
public abstract class Command
{
	public abstract void accept(IModelVisitor visitor);

	/**
	 * Convenience method to create a Clear Value command
	 * @param value
	 * @return
	 */
	public static Command clearValue(final Variable value)
	{
		return new Command() {
			@Override
			public void accept(IModelVisitor visitor)
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
			public void accept(IModelVisitor visitor)
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
			public void accept(IModelVisitor visitor)
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
			public void accept(IModelVisitor visitor)
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
			public void accept(IModelVisitor visitor)
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
			public void accept(IModelVisitor visitor)
			{
				visitor.visitCommandSetFlag(flags, flags.getBit(flagName), newValue);
			}
		};
	}
}
