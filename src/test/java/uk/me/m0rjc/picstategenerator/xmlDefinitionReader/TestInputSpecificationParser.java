package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

import uk.me.m0rjc.picstategenerator.model.CompositePrecondition;
import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.Variable;
import uk.me.m0rjc.picstategenerator.model.VariableValuePrecondition;
import uk.me.m0rjc.picstategenerator.model.Variable.Ownership;
import uk.me.m0rjc.picstategenerator.xmlDefinitionReader.InputSpecificationParser;

@RunWith(JUnit4.class)
public class TestInputSpecificationParser
{
	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void testParseValue_Number() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Assert.assertEquals(123, parseValue(parser, "123"));
		Assert.assertEquals(34, parseValue(parser, "34"));		
		Assert.assertEquals(0, parseValue(parser, "0"));		
	}

	@Test
	public void testParseValue_Hex() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Assert.assertEquals(0x1A, parseValue(parser, "0x1A"));
	}

	@Test
	public void testParseValue_Character() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Assert.assertEquals('c', parseValue(parser, "'c'"));
		Assert.assertEquals('\'', parseValue(parser, "'''")); // The parser is quite crude
	}

	@Test
	public void testParseValue_Wildcard() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Assert.assertEquals(InputSpecificationParser.WILDCARD, parseValue(parser, "*"));
	}
	
	@Test
	public void testParseSpec_SingleNumber() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Variable v = new Variable("test", Ownership.GLOBAL, -1, 1);
		VariableValuePrecondition expected = VariableValuePrecondition.createEQ(v, 123);
		Assert.assertEquals(expected, (VariableValuePrecondition)parseSpec(parser, v, "123"));		
	}

	@Test
	public void testParseSpec_SingleCharacter() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Variable v = new Variable("test", Ownership.GLOBAL, -1, 1);
		VariableValuePrecondition expected = VariableValuePrecondition.createEQ(v, '$');
		Assert.assertEquals(expected, (VariableValuePrecondition)parseSpec(parser, v, "'$'"));		
	}

	@Test
	public void testParseSpec_Wildcard() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Variable v = new Variable("test", Ownership.GLOBAL, -1, 1);
		Assert.assertNull(parseSpec(parser, v, "*"));		
	}

	@Test
	public void testParseSpec_CharacterRange() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Variable v = new Variable("test", Ownership.GLOBAL, -1, 1);
		CompositePrecondition expected = (CompositePrecondition) Precondition.range(v, '0', '9');
		Assert.assertEquals(expected, (CompositePrecondition)parseSpec(parser, v, "'0'-'9'"));		
	}

	@Test
	public void testParseSpec_NumberRange() throws SAXException
	{
		InputSpecificationParser parser = new InputSpecificationParser();
		Variable v = new Variable("test", Ownership.GLOBAL, -1, 1);
		CompositePrecondition expected = (CompositePrecondition) Precondition.range(v, 0, 9);
		Assert.assertEquals(expected, (CompositePrecondition)parseSpec(parser, v, "0-9"));		
	}

	
	private Precondition parseSpec(InputSpecificationParser parser, Variable v, String string) throws SAXException
	{
		char[] array = string.toCharArray();
		return parser.parseCondition(v, array, 0, array.length);		
	}
	
	private int parseValue(InputSpecificationParser parser, String string) throws SAXException
	{
		char[] array = string.toCharArray();
		return parser.parseValue(array, 0, array.length);
	}
}
