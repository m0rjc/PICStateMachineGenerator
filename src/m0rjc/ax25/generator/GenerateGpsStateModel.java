package m0rjc.ax25.generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import m0rjc.ax25.generator.diagramBuilder.DiagramBuilder;
import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.FlagCheckPrecondition;
import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.model.Variable;

/**
 * Main code to generate and output the state model.
 */
public class GenerateGpsStateModel
{
	private static final int STATE_VARIABLE_PAGE = 2;
	
	private static final String VARIABLE_GPS_TIME = "gpsTime";
	private static final String VARIABLE_GPS_QUALITY = "gpsQuality";
	private static final String VARIABLE_GPS_LONGITUDE_HUNDREDTHS = "gpsLongitudeHundredths";
	private static final String VARIABLE_GPS_LONGITUDE_MIN = "gpsLongitudeMin";
	private static final String VARIABLE_GPS_LONGITUDE_DEG = "gpsLongitudeDeg";
	private static final String VARIABLE_GPS_LATITUDE_HUNDREDTHS = "gpsLatitudeHundredths";
	private static final String VARIABLE_GPS_LATITUDE_MIN = "gpsLatitudeMin";
	private static final String VARIABLE_GPS_LATITUDE_DEG = "gpsLatitudeDeg";

	private static final String VARIABLE_GPS_FLAGS = "gpsFlags";
	private static final String GPS_FLAG_GPS_NEW_TIME = "FLAG_GPS_NEW_TIME";
	private static final String GPS_FLAG_GPS_NEW_QUALITY = "FLAG_GPS_NEW_QUALITY";
	private static final String GPS_FLAG_GPS_NEW_POSITION = "FLAG_GPS_NEW_POSITION";
	private static final String GPS_FLAG_GPS_EAST = "FLAG_GPS_EAST";
	private static final String GPS_FLAG_GPS_NORTH = "FLAG_GPS_NORTH";
	
	private static final String STATE_READ_TIME_HHMMSS = "ReadTimeHHMMSS";
	
	public static void main(String[] args) throws Exception
	{
		StateModel model = new StateModel("gps");
		model.setInputVariable(Variable.accessVariable("USART"));

		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_TIME, 6);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_QUALITY, 1);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LONGITUDE_DEG, 3);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LONGITUDE_MIN, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LONGITUDE_HUNDREDTHS, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LATITUDE_DEG, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LATITUDE_MIN, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LATITUDE_HUNDREDTHS, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_FLAGS, 1)
			.addFlag(GPS_FLAG_GPS_NEW_QUALITY)
			.addFlag(GPS_FLAG_GPS_NEW_TIME)
			.addFlag(GPS_FLAG_GPS_NEW_POSITION)
			.addFlag(GPS_FLAG_GPS_NORTH)
			.addFlag(GPS_FLAG_GPS_EAST);
		
		model.setReturnCode("RETFIE");

		Node initial = model.getInitialState();
		Node dollar = initial.addString("$");
		
		buildReadTimeHHMMSS(model);
		buildGpGLL(model, dollar);
		buildGpZDA(model, dollar);
		buildGpGGA(model, dollar);
		
		writeGraphviz(model);
		
	}

	/**
	 * Generate the graphviz file for the model
	 * @param model
	 * @throws FileNotFoundException
	 */
	private static void writeGraphviz(StateModel model)
		throws FileNotFoundException
	{
		PrintWriter writer = new PrintWriter("graph.dot");
		model.accept(new DiagramBuilder(writer));
		writer.close();
	}

	/**
	 * Define the state model for GPGGA - System fix data
	 * @param model
	 * @param dollar
	 * @throws Exception 
	 */
	private static void buildGpGGA(StateModel model, Node dollar) throws Exception
	{
		final String STATE_GPGGA_READ_TIME = "GpGaaReadTime";
		final String STATE_GPGGA_SKIP_TIME = "GpGaaSkipTime";
		final String STATE_GPGGA_SKIP_CENTISECONDS = "GpGaaSkipCentiSec";
		final String STATE_GPGGA_LATITUDE = "GpGaaLatitude";
		
		Variable input = model.getInputVariable();
		Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);
		
		dollar
			.addString("GPGGA")
			.addChoices(
				new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_READ_TIME),  // Will use state's entry condition
				new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_SKIP_TIME)); // Will use state's entry condition
		
		// Read HHMMSS.ss
		model.createNamedNode(STATE_GPGGA_READ_TIME)
			.addEntryCondition(Precondition.checkFlag(flags, GPS_FLAG_GPS_NEW_TIME, false))
			.addNumbers(6, 6, model.getVariable(VARIABLE_GPS_TIME))
			.addChoices(
				new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_LATITUDE), 
				new Transition().whenEqual(input, '.').goTo(STATE_GPGGA_SKIP_CENTISECONDS));
		
		// Skip over HHMMSS.ss
		model.createNamedNode(STATE_GPGGA_SKIP_TIME)
			.addEntryCondition(new FlagCheckPrecondition(flags, flags.getBit(GPS_FLAG_GPS_NEW_TIME), true))
			.addNumbers(6)
			.addChoices(
					new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_LATITUDE), 
					new Transition().whenEqual(input, '.').goTo(STATE_GPGGA_SKIP_CENTISECONDS));
			
		// Skip over ss,
		model.createNamedNode(STATE_GPGGA_SKIP_CENTISECONDS)
			.addNumbers(2)
			.addChoices(
				new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_LATITUDE)); 
		
		// Read Latitude and longitude
		Node latLong = model.createNamedNode(STATE_GPGGA_LATITUDE);
		Node afterLatLong = createMachineForLatLong(model, latLong, "GpGaa");
		
		// Read fix quality
		afterLatLong
			.addString(",")
			.addChoices(
					new Transition()
						.whenInRange(input, '0', '2')
						.doCommand(Command.storeValue(input, model.getVariable(VARIABLE_GPS_QUALITY)),
								   Command.setFlag(flags, GPS_FLAG_GPS_NEW_QUALITY, true))
						.goTo(model.getInitialState()));
		}
	
	/**
	 * Define the state model for GPZDA - Date and time
	 * @param model
	 * @param dollar
	 * @throws Exception 
	 */
	private static void buildGpZDA(StateModel model, Node dollar) throws Exception
	{
		Variable input = model.getInputVariable();
		
		dollar
			.addString("GPZDA")
			.addChoices(new Transition().whenEqual(input, ',').goTo(STATE_READ_TIME_HHMMSS));
	}

	/**
	 * Define the state model for GPGLL - Position
	 * @param model
	 * @param dollar
	 * @throws Exception
	 */
	private static void buildGpGLL(StateModel model, Node dollar)
		throws Exception
	{	
		Node gpgll = dollar.addString("GPGLL,");
		Node afterLatLong = createMachineForLatLong(model, gpgll, "GpGll");
		
		afterLatLong
			.addChoices(
				new SingleValueTransition(',', STATE_READ_TIME_HHMMSS),
				new SingleValueTransition('$', dollar));
	}

	/**
	 * Create a state machine to read Lat,Long
	 * @param model
	 * @param entryNode
	 * @param namePrefix
	 * @return the node at the end of the machine
	 * @throws Exception
	 */
	private static Node createMachineForLatLong(StateModel model, Node entryNode, final String namePrefix)
		throws Exception
	{
		String stateNameReadLongitude = namePrefix + "ReadLong";
		String stateNameComplete = namePrefix + "DoneLatLong";
		
		entryNode.addGuardCheckFlag(GPS_FLAG_GPS_NEW_POSITION, false)
			.addNumbers(2,VARIABLE_GPS_LATITUDE_DEG)
			.addNumbers(2,VARIABLE_GPS_LATITUDE_MIN)
			.addString(".")
			.addNumbers(2,VARIABLE_GPS_LATITUDE_HUNDREDTHS)
			.addString(",")
			.addChoices(
				new SingleValueTransition('S', 
					new Asm("BCF " + GPS_FLAG_GPS_NORTH),
					stateNameReadLongitude),
				new SingleValueTransition('N', 
					new Asm("BSF " + GPS_FLAG_GPS_NORTH),
					stateNameReadLongitude));
		
		model.createNamedNode(stateNameReadLongitude)
			.addNumbers(3,VARIABLE_GPS_LONGITUDE_DEG)
			.addNumbers(2,VARIABLE_GPS_LONGITUDE_MIN)
			.addString(".")
			.addNumbers(2,VARIABLE_GPS_LONGITUDE_HUNDREDTHS)
			.addString(",")
			.addChoices(
				new SingleValueTransition('E', 
					new Asm("BSF " + GPS_FLAG_GPS_EAST),
					stateNameComplete),
				new SingleValueTransition('W', 
					new Asm("BCF " + GPS_FLAG_GPS_EAST),
					stateNameComplete)
					);
		
		Node afterLatLong = model.createNamedNode(stateNameComplete);
		afterLatLong.addEntryCode("BSF " + GPS_FLAG_GPS_NEW_POSITION);
		return afterLatLong;
	}
	
	/**
	 * Build the segment to read time in the form HHMMSS then stop
	 * @param model
	 * @throws Exception
	 */
	private static void buildReadTimeHHMMSS(StateModel model) throws Exception
	{
		model.createNamedNode(STATE_READ_TIME_HHMMSS)
			.addGuardCheckFlag(GPS_FLAG_GPS_NEW_TIME, false)
			.addNumbers(6,VARIABLE_GPS_TIME)
			.addEntryCode("BSF " + GPS_FLAG_GPS_NEW_TIME);
	}
}
