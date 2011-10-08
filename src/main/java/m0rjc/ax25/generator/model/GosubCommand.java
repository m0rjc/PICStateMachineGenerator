package m0rjc.ax25.generator.model;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Command to transition to a node as a subroutine, storing the continuation if this
 * transition on a stack.
 *
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
 */
public class GosubCommand extends Command
{
	private String m_targetStateName;
	
	public GosubCommand(String targetStateName)
	{
		m_targetStateName = targetStateName;
	}

	@Override
	public void accept(StateModel model, IModelVisitor visitor)
	{
		visitor.push();
		visitor.saveReturnOnSubroutineStack();
		getNode(model).renderGoToNode(visitor, false);
		visitor.pop();
	}
	
	/**
	 * This command does require the subroutine stack.
	 */
	@Override
	public boolean requiresSubroutineStack()
	{
		return true;
	}
	
	private Node getNode(StateModel model)
	{
		return model.getNode(m_targetStateName);
	}
}
