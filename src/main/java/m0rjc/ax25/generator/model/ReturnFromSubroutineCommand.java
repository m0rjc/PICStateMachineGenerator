package m0rjc.ax25.generator.model;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Command to return from a subroutine by jumping to the
 * instruction stored on the stack.
 *
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public class ReturnFromSubroutineCommand extends Command
{
	@Override
	public void accept(StateModel model, IModelVisitor visitor)
	{
		visitor.visitTransitionReturnFromSubroutineStack();
	}

	/**
	 * This command does require the subroutine stack.
	 */
	@Override
	public boolean requiresSubroutineStack()
	{
		return true;
	}
}
