package uk.me.m0rjc.picstategenerator.endToEndTest;

/**
 * Access to the microcontroller running the test code. 
 */
public class MicrocontrollerIO
{
	/**
	 * Send a byte. Ensure that it is echoed back.
	 * @param input the byte to send.
	 * @throws Exception if the byte is not echoed back or another error occurs.
	 */
	public void sendByte(byte input) throws Exception
	{
		send(input);
		if(receive() != input)
		{
			throw new Exception("Microcontroller did not echo back input, or echo not received.");
		}
	}
	
	/**
	 * Send an ASCII string.
	 * @param string string to send
	 * @throws Exception on failure
	 */
	public void sendString(String string) throws Exception
	{
		byte[] bytes = string.getBytes("UTF8");
		for(byte b : bytes)
		{
			sendByte(b);
		}
	}
	
	/**
	 * Read a single value from the microcontroller.
	 * @param value the value to read
	 * @param offset the offset within a multi-byte value.
	 * @return the byte read.
	 * @throws Exception on failure.
	 */
	public byte readByte(TestHarnessValue value, int offset) throws Exception
	{
		if(offset >= value.getLength())
		{
			throw new IndexOutOfBoundsException();
		}
		
		sendString("XXXX!!TEST-SEND:");
		sendByte(value.getId());
		sendByte((byte)':');
		sendByte((byte)offset);
		sendString("!!");

		byte result;
		byte first = receive();
		if(first == (byte)'>')
		{
			if(receive() == (byte)'>')
			{
				result = receive();
				if(receive() == (byte)'<')
				{
					return result;
				}
			}
		}
		else if(first == (byte)'E')
		{
			if(receive() == (byte)'0')
			{
				byte digit = receive();
				if(digit == (byte)'1')
				{
					throw new Exception("Microcontroller reports unrecognised test harness value");
				}
				else if(digit == (byte)'2')
				{
					throw new Exception("Microcontroller reports unrecognised flag");
				}
			}
		}
		throw new Exception("Unexpected response from microcontroller.");
	}
	
	/**
	 * Read all the bytes of the given value as a string.
	 * @param value the value to read from the connected microcontroller.
	 * @return the value as a string
	 * @throws Exception on failure
	 */
	public String readString(TestHarnessValue value) throws Exception
	{
		byte[] values = new byte[value.getLength()];
		for(int i = 0; i < values.length; i++)
		{
			values[i] = readByte(value, i);
		}
		return new String(values);
	}
	
	/**
	 * Send a command to direct set Test9
	 * @param newValue
	 * @throws Exception
	 */
	public void setTestFlag9(boolean newValue) throws Exception
	{
		sendString("XXXX!!SET-TEST-9:" + (newValue ? '1' : '0'));
	}
	
	/**
	 * Transmit a byte.
	 * @param b byte to send
	 * @throws Exception on failure
	 */
	private void send(byte b) throws Exception
	{
		// TODO:
	}
	
	/**
	 * Receive a byte.
	 * @return the byte read
	 * @throws Exception on failure
	 */
	private byte receive() throws Exception
	{
		// TODO:
		return 0;
	}
}
