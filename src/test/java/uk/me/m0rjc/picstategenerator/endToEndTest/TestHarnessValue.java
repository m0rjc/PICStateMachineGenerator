package uk.me.m0rjc.picstategenerator.endToEndTest;

/**
 * A value known to the C test harness code running on the test microcontroller.
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public enum TestHarnessValue
{
	/** Echo back the Send Index */
	TEST_SEND_INDEX(1,256),
	/** Read the GPS flags. Indexes are
	 * <ul>
	 * <li>0: New Position ('0' or '1')
	 * <li>1: East ('E') or West ('W')
	 * <li>2: North ('N') or South ('S')
	 * <li>3: Test Flag 9 ('0' or '1')
	 * </ul>
	 *  */
	GPS_FLAGS(2,4),
	
	GPS_LATITUDE_DEGMIN(3,4),
	GPS_LATITUDE_HUNDREDTHS(4,2),
	GPS_LONGITUDE_DEGMIN(5,5),
	GPS_LONGITUDE_HUNDREDTHS(6,2),
	GPS_TIME(7,6),
	GPS_QUALITY(8,1);
	
	TestHarnessValue(int id, int length)
	{
		m_id = (byte)id;
		m_length = length;
	}
	
	private byte m_id;
	private int m_length;
	
	/**
	 * @return the ID known to main.c on the microcontroller.
	 */
	public byte getId()
	{
		return m_id;
	}
	
	/**
	 * @return the length of data for this value.
	 */
	public int getLength()
	{
		return m_length;
	}
}
