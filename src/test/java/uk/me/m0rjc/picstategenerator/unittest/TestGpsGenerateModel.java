package uk.me.m0rjc.picstategenerator.unittest;

import java.io.InputStream;
import java.util.logging.LogManager;

import junit.framework.Assert;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.me.m0rjc.picstategenerator.GenerateGpsStateModel;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.Simulation;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulationException;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulatorBuilder;

@RunWith(JUnit4.class)
public class TestGpsGenerateModel
{
	private Simulation m_simulation;
	private StateModel m_model;
	
	@BeforeClass
	public static void systemSetup() throws Exception
	{
		// Set up logging
		InputStream in = TestGpsGenerateModel.class.getResourceAsStream("logging.properties");
		if(in != null)
		{
			LogManager.getLogManager().readConfiguration(in);
		}
	}
	
	@Before
	public void testSetup() throws Exception
	{
		m_model = GenerateGpsStateModel.buildmodel();
		m_model.optimiseModel();
		SimulatorBuilder builder = new SimulatorBuilder();
		builder.registerSpecialFunctionRegister(GenerateGpsStateModel.SFR_EUSART_RECEIVE);
		m_model.accept(builder);
		m_simulation = builder.getSimulation();
		m_simulation.setInputVariable(GenerateGpsStateModel.VARIABLE_INPUT);
	}
	
	/**
	 * The root node has many ways in, so should use the shared entry optimisation.
	 * @throws SimulationException
	 */
	@Test
	public void testOptimiser_rootNodeHasSharedEntry() throws SimulationException
	{
		Assert.assertTrue(m_model.getInitialState().isUseSharedEntryCode());
	}
	
	/**
	 * The root node has many ways in, so should use the shared entry optimisation.
	 * @throws SimulationException
	 */
	@Test
	public void testOptimiser_dollarNodeHasSharedEntry() throws SimulationException
	{
		Assert.assertTrue(m_simulation.getNode("dollar").isSharedEntryCodeDeclared());
	}
	
	@Test
	public void testGpGGA_fix_storesFix() throws SimulationException
	{
		// Example taken from the datasheet for my module
		m_simulation.acceptInput("$GPGGA,060932.448,2447.0959,N,12100.5204,E,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_TIME, "060932");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_DEGMIN, "2447");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_HUNDREDTHS, "09");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NORTH, true);
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_DEGMIN, "12100");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_HUNDREDTHS, "52");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_EAST, true);
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, true);
	}

	@Test
	public void testGpRMC_fix_storesFix() throws SimulationException
	{
		// Example taken from http://aprs.gids.nl/nmea/#rmc
		m_simulation.acceptInput("$GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68\n\r");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_TIME, "225446");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_DEGMIN, "4916");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_HUNDREDTHS, "45");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NORTH, true);
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_DEGMIN, "12311");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_HUNDREDTHS, "12");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_EAST, false);
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, true);
	}

	
	@Test
	public void testGpGGA_fixButFixAlreadyStored_doesNotStoreFix() throws SimulationException
	{
		// Example taken from the datasheet for my module
		m_simulation.acceptInput("$GPGGA,060932.448,2447.0959,S,12100.5204,W,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		// This checksum is wrong. At time of writing the checksum was not checked.
		m_simulation.acceptInput("$GPGGA,184512.448,1234.5678,N,06012.9682,E,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_TIME, "060932");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_DEGMIN, "2447");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_HUNDREDTHS, "09");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NORTH, false);
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_DEGMIN, "12100");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_HUNDREDTHS, "52");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_EAST, false);
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, true);		
	}

	@Test
	public void testGpGGA_modelWillAcceptNewFixAfterPreviousFixRead_storesFix() throws SimulationException
	{
		// Example taken from the datasheet for my module
		m_simulation.acceptInput("$GPGGA,060932.448,2447.0959,S,12100.5204,W,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		m_simulation.setFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, false);
		// This checksum is wrong. At time of writing the checksum was not checked.
		m_simulation.acceptInput("$GPGGA,184512.448,1234.5678,N,06012.9682,E,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_TIME, "184512");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_DEGMIN, "1234");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_HUNDREDTHS, "56");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NORTH, true);
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_DEGMIN, "06012");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_HUNDREDTHS, "96");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_EAST, true);
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, true);		
	}

	
	@Test
	public void testGpGGA_garbledFollowedByCorrectInput_storesFix() throws SimulationException
	{
		// Example taken from the datasheet for my module
		m_simulation.acceptInput("$GPGGA,060932.448,2447.0959garbledgarbledgarbled");
		m_simulation.acceptInput("$GPGGA,060934.448,2447.0959,S,12100.5204,W,1,08,1.1,108.7,M,,,,0000*0E\n\r");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_TIME, "060934");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_DEGMIN, "2447");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LATITUDE_HUNDREDTHS, "09");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NORTH, false);
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_DEGMIN, "12100");
		m_simulation.assertChars(GenerateGpsStateModel.VARIABLE_GPS_LONGITUDE_HUNDREDTHS, "52");
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_EAST, false);
		m_simulation.assertFlag(GenerateGpsStateModel.VARIABLE_GPS_FLAGS, GenerateGpsStateModel.GPS_FLAG_GPS_NEW_POSITION, true);				
	}

}
