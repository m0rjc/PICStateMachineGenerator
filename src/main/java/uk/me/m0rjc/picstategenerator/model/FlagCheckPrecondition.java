package uk.me.m0rjc.picstategenerator.model;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

/**
 * A precondition that checks the value of a flag.
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class FlagCheckPrecondition extends Precondition
{
	private final Variable m_variable;
	private final int m_bit;
	private final boolean m_expectedValue;
	
	public FlagCheckPrecondition(Variable variable, int bit, boolean expectedValue)
	{
		m_variable = variable;
		m_bit = bit;
		m_expectedValue = expectedValue;
	}

	/**
	 * @see uk.me.m0rjc.picstategenerator.model.Precondition#accept(uk.me.m0rjc.picstategenerator.visitor.IModelVisitor)
	 */
	@Override
	public void accept(IModelVisitor visitor)
	{
		visitor.visitTransitionPreconditionFlag(m_variable, m_bit, m_expectedValue);
	}

	/**
	 * A flags check can never accept a single value, so this method returns false.
	 */
	@Override
	public boolean accepts(Variable variable, int value)
	{
		return false;
	}
	
	
}
