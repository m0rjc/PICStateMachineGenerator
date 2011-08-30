package m0rjc.ax25.generator.model;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * A precondition based on the value of a variable
 */
public class VariableValuePrecondition extends Precondition
{
	private enum Comparison
	{
		LESS_THAN_OR_EQUAL
		{
			@Override
			void accept(IModelVisitor visitor, Variable variable, int value)
			{
				visitor.visitTransitionPreconditionLE(variable, value);
			}

			@Override
			boolean accepts(int myValue, int queryValue)
			{
				return queryValue <= myValue;
			}
		},
		EQUAL
		{
			@Override
			void accept(IModelVisitor visitor, Variable variable, int value)
			{
				visitor.visitTransitionPreconditionEQ(variable, value);
			}
			
			@Override
			boolean accepts(int myValue, int queryValue)
			{
				return queryValue == myValue;
			}
		},
		GREATER_THAN_OR_EQUAL
		{
			@Override
			void accept(IModelVisitor visitor, Variable variable, int value)
			{
				visitor.visitTransitionPreconditionGE(variable, value);
			}
			
			@Override
			boolean accepts(int myValue, int queryValue)
			{
				return queryValue >= myValue;
			}
		};

		/** Visitor/Builder pattern accept method. */
		abstract void accept(IModelVisitor visitor, Variable variable, int value);
		
		/** True if this the values satisfy this condition.
		 * @param myValue   value stored in the precondition
		 * @param quetyValue value being tested
		 */
		abstract boolean accepts(int myValue, int quetyValue);
	}

	/** Create a precondition that requires variable = value */
	public static Precondition createEQ(Variable variable, int value)
	{
		return new VariableValuePrecondition(Comparison.EQUAL, variable, value);
	}

	/** Create a precondition that requires variable &gt;= value */
	public static Precondition createGE(Variable variable, int value)
	{
		return new VariableValuePrecondition(Comparison.GREATER_THAN_OR_EQUAL,
				variable, value);
	}

	/** Create a precondition that requires variable &lt;= value */
	public static Precondition createLE(Variable variable, int value)
	{
		return new VariableValuePrecondition(Comparison.LESS_THAN_OR_EQUAL,
				variable, value);
	}

	@Override
	public boolean accepts(Variable variable, int value)
	{
		return m_variable.equals(variable) && m_comparison.accepts(m_value, value);
	}

	private final Comparison m_comparison;
	private final Variable m_variable;
	private final int m_value;

	private VariableValuePrecondition(Comparison comparison, Variable variable,
			int value)
	{
		m_comparison = comparison;
		m_variable = variable;
		m_value = value;
	}

	/**
	 * @see m0rjc.ax25.generator.model.Precondition#accept(m0rjc.ax25.generator.visitor.IModelVisitor)
	 */
	@Override
	public void accept(IModelVisitor visitor)
	{
		m_comparison.accept(visitor, m_variable, m_value);
	}

}
