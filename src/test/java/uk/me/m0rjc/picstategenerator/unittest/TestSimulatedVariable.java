package uk.me.m0rjc.picstategenerator.unittest;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulatedVariable;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulationException;

@RunWith(JUnit4.class)
public class TestSimulatedVariable
{
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Test
	public void testInitiallyZero() throws SimulationException
	{
		SimulatedVariable variable = new SimulatedVariable("Test", 5);
		for(int i = 0; i < 5; i++)
		{
			Assert.assertEquals(0, variable.getValue(i));
		}
	}
	
	@Test
	public void testZeroSizeFails() throws SimulationException
	{
		expected.expect(IllegalArgumentException.class);
		new SimulatedVariable("Test", 0);
	}

	@Test
	public void testTooBigFails() throws SimulationException
	{
		expected.expect(IllegalArgumentException.class);
		new SimulatedVariable("Test", 257);
	}

	@Test
	public void testGetSet() throws SimulationException
	{
		SimulatedVariable variable = new SimulatedVariable("Test", 5);
		byte value = 34;
		variable.setValue(value);
		Assert.assertEquals(value, variable.getValue());
		Assert.assertEquals(value, variable.getValue(0));
	}
	
	@Test
	public void testGetSetIndexed() throws SimulationException
	{
		SimulatedVariable variable = new SimulatedVariable("Test", 5);
		byte[] values = {34,22,-35,0,127};

		for(int i = 0; i < 5; i++)
		{
			variable.setValue(i,values[i]);
		}

		for(int i = 0; i < 5; i++)
		{
			Assert.assertEquals(values[i], variable.getValue(i));
		}
	}

	@Test
	public void testGetSetIndexedTooBigFails() throws SimulationException
	{
		SimulatedVariable variable = new SimulatedVariable("Test", 5);
		expected.expect(SimulationException.class);
		variable.getValue(5);
	}
	
	@Test
	public void testSetGetBit() throws SimulationException
	{
		SimulatedVariable variable = new SimulatedVariable("Test", 5);
		
		Assert.assertFalse(variable.getBit(0));
		variable.setBit(0, true);
		Assert.assertEquals(1, variable.getValue());
		Assert.assertTrue(variable.getBit(0));
		
		variable.setBit(4, true);
		Assert.assertEquals(17, variable.getValue());
		Assert.assertTrue(variable.getBit(4));
		Assert.assertTrue(variable.getBit(0));

		variable.setBit(0, false);
		Assert.assertEquals(16, variable.getValue());
		Assert.assertTrue(variable.getBit(4));
		Assert.assertFalse(variable.getBit(0));

		Assert.assertFalse(variable.getBit(7));
		variable.setBit(7, true);
		Assert.assertEquals(-112, variable.getValue());
		Assert.assertTrue(variable.getBit(7));

		Assert.assertFalse(variable.getBit(8));
		variable.setBit(8, true);
		Assert.assertEquals(1, variable.getValue(1));
		Assert.assertTrue(variable.getBit(8));
	}

}
