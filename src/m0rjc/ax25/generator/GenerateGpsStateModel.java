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
	
	/** This can only be read once per input character. It is a FIFO */
	public static final String SFR_EUSART_RECEIVE = "RCREG";
	
	public static final String VARIABLE_INPUT = "gpsInput";
	public static final String VARIABLE_GPS_TIME = "gpsTime";
	public static final String VARIABLE_GPS_QUALITY = "gpsQuality";
	public static final String VARIABLE_GPS_LONGITUDE_HUNDREDTHS = "gpsLongitudeHundredths";
	public static final String VARIABLE_GPS_LONGITUDE_MIN = "gpsLongitudeMin";
	public static final String VARIABLE_GPS_LONGITUDE_DEG = "gpsLongitudeDeg";
	public static final String VARIABLE_GPS_LATITUDE_HUNDREDTHS = "gpsLatitudeHundredths";
	public static final String VARIABLE_GPS_LATITUDE_MIN = "gpsLatitudeMin";
	public static final String VARIABLE_GPS_LATITUDE_DEG = "gpsLatitudeDeg";

	public static final String VARIABLE_GPS_FLAGS = "gpsFlags";
	public static final String GPS_FLAG_GPS_NEW_TIME = "FLAG_GPS_NEW_TIME";
	public static final String GPS_FLAG_GPS_NEW_QUALITY = "FLAG_GPS_NEW_QUALITY";
	public static final String GPS_FLAG_GPS_NEW_POSITION = "FLAG_GPS_NEW_POSITION";
	public static final String GPS_FLAG_GPS_EAST = "FLAG_GPS_EAST";
	public static final String GPS_FLAG_GPS_NORTH = "FLAG_GPS_NORTH";
	
	private static final String STATE_READ_TIME_HHMMSS = "ReadTimeHHMMSS";
	
	public static void main(String[] args) throws Exception
	{
		StateModel model = buildmodel();
		
		writeGraphviz(model);
	}

	public static StateModel buildmodel() throws Exception
	{
		StateModel model = new StateModel("gps");

		model.setInputVariable(model.createGlobalAccessVariable(VARIABLE_INPUT, 1));
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
		buildGpGGA(model, dollar);
		return model;
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
			.addEntryCommand(Command.setFlag(flags, GPS_FLAG_GPS_NEW_TIME, true))
			.skipTo(
				new Transition().whenEqual(input, '$').goTo(dollar),
				new Transition().whenEqual(input, ',').goTo(STATE_GPGGA_LATITUDE));
				
		// Skip over HHMMSS.ss
		model.createNamedNode(STATE_GPGGA_SKIP_TIME)
			.addEntryCondition(new FlagCheckPrecondition(flags, flags.getBit(GPS_FLAG_GPS_NEW_TIME), true))
			.addNumbers(6)
			.skipTo(
				new Transition().whenEqual(input, '$').goTo(dollar),
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
		
		Variable input = model.getInputVariable();
		Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);
		
		entryNode.addEntryCondition(Precondition.checkFlag(flags, GPS_FLAG_GPS_NEW_POSITION, false))
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LATITUDE_DEG))
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LATITUDE_MIN))
			.addString(".")
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LATITUDE_HUNDREDTHS))
			.addNumbers(0,2)
			.addString(",")
			.addChoices(
				new Transition().whenEqual(input,'S')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_NORTH, false))
						.goTo(stateNameReadLongitude),
				new Transition().whenEqual(input, 'N')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_NORTH, true))
						.goTo(stateNameReadLongitude));
		
		model.createNamedNode(stateNameReadLongitude)
			.addString(",")
			.addNumbers(3,3,model.getVariable(VARIABLE_GPS_LONGITUDE_DEG))
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LONGITUDE_MIN))
			.addString(".")
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LONGITUDE_HUNDREDTHS))
			.addNumbers(0,2)
			.addString(",")
			.addChoices(
					new Transition().whenEqual(input,'E')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_EAST, true))
						.goTo(stateNameComplete),
					new Transition().whenEqual(input, 'W')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_EAST, false))
						.goTo(stateNameComplete));
		
		Node afterLatLong = model.createNamedNode(stateNameComplete);
		afterLatLong.addEntryCommand(Command.setFlag(flags, GPS_FLAG_GPS_NEW_POSITION, true));
		return afterLatLong;
	}
	
	/**
	 * Build the segment to read time in the form HHMMSS then stop
	 * @param model
	 * @throws Exception
	 */
	private static void buildReadTimeHHMMSS(StateModel model) throws Exception
	{
		Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);
		
		model.createNamedNode(STATE_READ_TIME_HHMMSS)
			.addEntryCondition(Precondition.checkFlag(flags, GPS_FLAG_GPS_NEW_TIME, false))
			.addNumbers(6,6,model.getVariable(VARIABLE_GPS_TIME))
			.addEntryCommand(Command.setFlag(flags, GPS_FLAG_GPS_NEW_TIME, true));
	}
}
