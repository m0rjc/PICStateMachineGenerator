package uk.me.m0rjc.picstategenerator.model;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

/**
 * Command to transition to a node as a subroutine, storing the continuation if this
 * transition on a stack.
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
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
	
	@Override
	public String getTargetNode()
	{
		return m_targetStateName;
	}

	private Node getNode(StateModel model)
	{
		return model.getNode(m_targetStateName);
	}
}
