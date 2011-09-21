package m0rjc.ax25.generator.unittest;

import java.io.InputStream;
import java.util.logging.LogManager;

import m0rjc.ax25.generator.GenerateGpsStateModel;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.simulatorBuilder.Simulation;
import m0rjc.ax25.generator.simulatorBuilder.SimulationException;
import m0rjc.ax25.generator.simulatorBuilder.SimulatorBuilder;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestGpsGenerateModel
{
	private Simulation m_simulation;
	
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
		StateModel model = GenerateGpsStateModel.buildmodel();
		SimulatorBuilder builder = new SimulatorBuilder();
		builder.registerSpecialFunctionRegister(GenerateGpsStateModel.SFR_EUSART_RECEIVE);
		model.accept(builder);
		m_simulation = builder.getSimulation();
		m_simulation.setInputVariable(GenerateGpsStateModel.VARIABLE_INPUT);
	}
	
	// TODO: Test variables are set up in the right places.
	
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
