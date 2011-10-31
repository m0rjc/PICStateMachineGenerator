package uk.me.m0rjc.picstategenerator.model;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

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
	public static VariableValuePrecondition createEQ(Variable variable, int value)
	{
		return new VariableValuePrecondition(Comparison.EQUAL, variable, value);
	}

	/** Create a precondition that requires variable &gt;= value */
	public static VariableValuePrecondition createGE(Variable variable, int value)
	{
		return new VariableValuePrecondition(Comparison.GREATER_THAN_OR_EQUAL,
				variable, value);
	}

	/** Create a precondition that requires variable &lt;= value */
	public static VariableValuePrecondition createLE(Variable variable, int value)
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
	 * @see uk.me.m0rjc.picstategenerator.model.Precondition#accept(uk.me.m0rjc.picstategenerator.visitor.IModelVisitor)
	 */
	@Override
	public void accept(IModelVisitor visitor)
	{
		m_comparison.accept(visitor, m_variable, m_value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_comparison == null) ? 0 : m_comparison.hashCode());
		result = prime * result + m_value;
		result = prime * result
				+ ((m_variable == null) ? 0 : m_variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VariableValuePrecondition other = (VariableValuePrecondition) obj;
		if (m_comparison != other.m_comparison) return false;
		if (m_value != other.m_value) return false;
		if (m_variable == null)
		{
			if (other.m_variable != null) return false;
		}
		else if (!m_variable.equals(other.m_variable)) return false;
		return true;
	}
}
