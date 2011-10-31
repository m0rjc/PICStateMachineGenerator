package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;


import org.xml.sax.SAXException;

import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.Variable;

/**
 * Class to parse input specifications. This class currently will parse one
 * specification based on the following tokens.
 * 
 * <ul>
 * <li><code>123</code> A decimal number
 * <li><code>0x1A</code> A hex number
 * <li><code>'a'</code> A character
 * <li><code>*</code> Anything
 * </ul>
 * 
 * <strong>This code is not re-entrant</strong>
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class InputSpecificationParser
{
	private char[] m_input;
	private int m_startPosition;
	private int m_currentPosition;
	private int m_endPosition;

	private int m_valueAccumulator;
	private boolean m_hasValue;

	public static final int WILDCARD = -1;

	public static final InputSpecificationParser INSTANCE = new InputSpecificationParser();
	
	/**
	 * Parse a condition made up of a single value or a range of values.
	 * 
	 * The following pattern is supported
	 * 
	 * <ul>
	 * <li><code><i>token</i>-<i>token</i></code> A range, example
	 * <code>'0'-'9'</code>
	 * </ul>
	 * 
	 * If a wildcard is read then null will be returned.
	 * 
	 * @param v
	 * @param input
	 * @param start
	 * @param length
	 * @return
	 * @throws SAXException
	 */
	public Precondition parseCondition(Variable v, String input) throws SAXException
	{
		return parseCondition(v, input.toCharArray(), 0, input.length());
	}
	
	/**
	 * Parse a condition made up of a single value or a range of values.
	 * 
	 * The following pattern is supported
	 * 
	 * <ul>
	 * <li><code><i>token</i>-<i>token</i></code> A range, example
	 * <code>'0'-'9'</code>
	 * </ul>
	 * 
	 * If a wildcard is read then null will be returned.
	 * 
	 * @param v
	 * @param input
	 * @param start
	 * @param length
	 * @return
	 * @throws SAXException
	 */
	public Precondition parseCondition(Variable v, char[] input, int start,
			int length) throws SAXException
	{
		m_startPosition = start;
		m_input = input;
		m_currentPosition = start;
		m_endPosition = start + length - 1;

		try
		{
			Precondition result = doParseCondition(v);
			if (isNotAtEnd())
			{
				throwException("Extra information after end");
			}
			return result;
		}
		finally
		{
			m_input = null;
		}
	}

	private Precondition doParseCondition(Variable v) throws SAXException
	{
		if (!isNotAtEnd())
		{
			throwException("Nothing entered");
		}

		int firstValue = doParseValue();

		if (firstValue == WILDCARD)
		{
			return null;
		}

		if (isNotAtEnd())
		{
			if (readChar() != '-')
			{
				throwException("Expected - sign to indicate a range");
			}
			int secondValue = doParseValue();
			if (isNotAtEnd())
			{
				throwException("Extra information after end");
			}
			return Precondition.range(v, firstValue, secondValue);
		}

		return Precondition.equals(v, firstValue);
	}

	/**
	 * Parse a single value.
	 * 
	 * <ul>
	 * <li><code>123</code> A decimal number
	 * <li><code>0x1A</code> A hex number
	 * <li><code>'a'</code> A character
	 * <li><code>*</code> Anything
	 * </ul>
	 * 
	 * @param input
	 * @param start
	 * @param length
	 * @return
	 * @throws SAXException
	 */
	public int parseValue(String input)
			throws SAXException
	{
		return parseValue(input.toCharArray(), 0, input.length());
	}
	
	/**
	 * Parse a single value.
	 * 
	 * <ul>
	 * <li><code>123</code> A decimal number
	 * <li><code>0x1A</code> A hex number
	 * <li><code>'a'</code> A character
	 * <li><code>*</code> Anything
	 * </ul>
	 * 
	 * @param input
	 * @param start
	 * @param length
	 * @return
	 * @throws SAXException
	 */
	public int parseValue(char[] input, int start, int length)
			throws SAXException
	{
		m_startPosition = start;
		m_input = input;
		m_currentPosition = start;
		m_endPosition = start + length - 1;

		try
		{
			return doParseValue();
		}
		finally
		{
			m_input = null;
		}
	}

	/**
	 * Parse a value, then leave the state machine set up for more input
	 * 
	 * @throws SAXException
	 */
	private int doParseValue() throws SAXException
	{
		m_hasValue = false;
		m_valueAccumulator = 0;
		
		ValueParserState state = ValueParserState.INITIAL;
		while (isNotAtEnd() && state != ValueParserState.FINAL)
		{
			state = state.nextChar(readChar(), this);
		}

		if (!m_hasValue) throwException("Did not find a Value");
		return m_valueAccumulator;
	}

	/**
	 * Read a character, then advance the position.
	 * 
	 * @throws SAXException
	 */
	private char readChar() throws SAXException
	{
		if (!isNotAtEnd())
			throw new SAXException(
					"Input specification parser tried to read beyond end");
		return m_input[m_currentPosition++];
	}

	/** True if there are still characters to read */
	private boolean isNotAtEnd()
	{
		return m_currentPosition <= m_endPosition;
	}

	/**
	 * Report an error. The character reported was the last character read with
	 * readChar()
	 */
	private void throwException(String message) throws SAXException
	{
		// The index in the main array of the character that was read, if any
		int errorPtr = m_currentPosition - 1;
		// Index for reporting is 1 based
		int errorIndex = errorPtr - m_startPosition + 1;
		// The character that was read
		String currentCharacter = errorPtr >= m_startPosition ? new String(
				m_input, errorPtr, 1) : "none";

		String inputText = new String(m_input, m_startPosition, m_endPosition
				- m_startPosition + 1);
		throw new SAXException(
				String.format(
						"Parse error reading input value spec '%s'. Position %d (%s): %s",
						inputText, errorIndex, currentCharacter, message));
	}

	/** The state machine has found the wildcard specifier */
	protected void onWildcard()
	{
		m_valueAccumulator = WILDCARD;
		m_hasValue = true;
	}

	/**
	 * The parser is finding hex digits.
	 * 
	 * @param i
	 *            The value 0 to 15 represented by the digit
	 */
	protected void onHexDigit(int i)
	{
		m_hasValue = true;
		m_valueAccumulator = m_valueAccumulator * 16 + i;
	}

	/**
	 * The parser is finding decimal digits.
	 * 
	 * @param i
	 *            The value 0 to 9 represented by the digit
	 */
	protected void onDecimalDigit(int i)
	{
		m_hasValue = true;
		m_valueAccumulator = m_valueAccumulator * 10 + i;
	}

	/** Undo the fetching of the character */
	protected void rewindOne()
	{
		m_currentPosition--;
	}

	/** The state machine has found a character specification */
	protected void onCharacter(char ch)
	{
		m_valueAccumulator = ch;
		m_hasValue = true;
	}

	/** State machine to read a value */
	private enum ValueParserState
	{
		/** The initial state */
		INITIAL
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				if (ch == '\'') return READ_CHAR;
				if (ch == '*')
				{
					callback.onWildcard();
					return FINAL;
				}
				if (ch == '0')
				{
					callback.onDecimalDigit(0);
					return INITIAL_ZERO;
				}
				else if (ch > '0' && ch <= '9')
				{
					callback.onDecimalDigit(ch - '0');
					return READ_DECIMAL;
				}
				else
					throw new SAXException("Unexpected character '" + ch
							+ "' at start of Value");
			}
		},
		/** Inside the single quotes, the character. */
		READ_CHAR
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				callback.onCharacter(ch);
				return AFTER_CHAR;
			}
		},
		/** After the character has been read there must be a single quote */
		AFTER_CHAR
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				if (ch == '\'') return FINAL;
				throw new SAXException(
						"Cannot parse value. Expecting ' after character.");
			}
		},

		/** If the first character is zero we don't know if it's hex or decimal */
		INITIAL_ZERO
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				if (ch == 'x' || ch == 'X') return READ_HEX;
				if (ch >= '0' && ch <= '9')
				{
					callback.onDecimalDigit(ch - '0');
					return READ_DECIMAL;
				}
				else
				{
					callback.rewindOne();
					return FINAL;
				}
			}
		},

		/** Read hex digits until they run out */
		READ_HEX
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				ch = Character.toLowerCase(ch);
				if (ch >= '0' && ch <= '9')
				{
					callback.onHexDigit(ch - '0');
					return READ_HEX;
				}
				if (ch >= 'a' && ch <= 'f')
				{
					int value = ch - 'a' + 10;
					callback.onHexDigit(value);
					return READ_HEX;
				}
				else
				{
					callback.rewindOne();
					return FINAL;
				}
			}
		},

		/** Read decimal digits until they run out */
		READ_DECIMAL
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				ch = Character.toLowerCase(ch);
				if (ch >= '0' && ch <= '9')
				{
					callback.onDecimalDigit(ch - '0');
					return READ_DECIMAL;
				}
				else
				{
					callback.rewindOne();
					return FINAL;
				}
			}
		},

		/** End state */
		FINAL
		{
			@Override
			ValueParserState nextChar(char ch, InputSpecificationParser callback)
					throws SAXException
			{
				return FINAL;
			}
		};

		/**
		 * Respond to the next character
		 * 
		 * @param ch
		 * @param callback
		 * @return the state to transition to.
		 * @throws SAXException
		 */
		abstract ValueParserState nextChar(char ch,
				InputSpecificationParser callback) throws SAXException;
	}
}
