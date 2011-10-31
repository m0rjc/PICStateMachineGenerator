package uk.me.m0rjc.picstategenerator.model;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

/**
 * Command to return from a subroutine by jumping to the
 * instruction stored on the stack.
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
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
