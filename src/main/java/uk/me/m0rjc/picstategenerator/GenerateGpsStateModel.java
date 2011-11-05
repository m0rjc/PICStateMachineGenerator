package uk.me.m0rjc.picstategenerator;

import uk.me.m0rjc.picstategenerator.model.Command;
import uk.me.m0rjc.picstategenerator.model.GosubCommand;
import uk.me.m0rjc.picstategenerator.model.Node;
import uk.me.m0rjc.picstategenerator.model.Precondition;
import uk.me.m0rjc.picstategenerator.model.ReturnFromSubroutineCommand;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.SymbolOwnership;
import uk.me.m0rjc.picstategenerator.model.Transition;
import uk.me.m0rjc.picstategenerator.model.Variable;

/**
 * Example of generating the model programatically. Has since been replaced by
 * the XML reader, but still used for testing.
 */
public final class GenerateGpsStateModel
{
    /** Characters in the 100ths part of the GPS data. */
    private static final int SIZE_HUNDREDTHS = 2;
    /** Characters in the Latitude degrees and minutes part of the GPS data. */
    private static final int SIZE_LATITUDE_DEGMIN = 4;
    /** Characters in the Longitude degrees and minutes part of the GPS data. */
    private static final int SIZE_LONGITUDE_DEGMIN = 5;
    /** Characters int he time part of the GPS data. */
    private static final int SIZE_GPS_TIME = 6;

    /** Subroutine name to read the latitude longitude string. */
    private static final String SUBROUTINE_READ_LAT_LONG = "readLatLong";
    /** Subroutine name to read the time string. */
    private static final String SUBROUTINE_READ_TIME = "readTime";

    /** Page for state variables. */
    private static final int STATE_VARIABLE_PAGE = 2;

    /** Input variable name. */
    public static final String VARIABLE_INPUT = "gpsInput";
    /** Time - 6 characters, eg 180423 for 18:04 and 23 seconds. */
    public static final String VARIABLE_GPS_TIME = "gpsTime";
    /** GPS quality indicator character. */
    public static final String VARIABLE_GPS_QUALITY = "gpsQuality";
    public static final String VARIABLE_GPS_LONGITUDE_HUNDREDTHS = "gpsLongitudeHundredths";
    public static final String VARIABLE_GPS_LONGITUDE_DEGMIN = "gpsLongitudeDeg";
    public static final String VARIABLE_GPS_LATITUDE_HUNDREDTHS = "gpsLatitudeHundredths";
    public static final String VARIABLE_GPS_LATITUDE_DEGMIN = "gpsLatitudeDeg";

    public static final String VARIABLE_GPS_FLAGS = "gpsFlags";
    public static final String GPS_FLAG_GPS_NEW_QUALITY = "FLAG_GPS_NEW_QUALITY";
    public static final String GPS_FLAG_GPS_NEW_POSITION = "FLAG_GPS_NEW_POSITION";
    public static final String GPS_FLAG_GPS_EAST = "FLAG_GPS_EAST";
    public static final String GPS_FLAG_GPS_NORTH = "FLAG_GPS_NORTH";

    /** Inhibit construction. */
    private GenerateGpsStateModel()
    {
    }
    
    /** @return Build the model programatically. */
    public static StateModel buildmodel()
    {
        final StateModel model = new StateModel("gps");

        final Variable input = createGlobalAccessVariable(model,
                VARIABLE_INPUT, 1);
        model.setInputVariable(input);

        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_TIME, SIZE_GPS_TIME);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_QUALITY, 1);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_LONGITUDE_DEGMIN, SIZE_LONGITUDE_DEGMIN);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_LONGITUDE_HUNDREDTHS, SIZE_HUNDREDTHS);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_LATITUDE_DEGMIN, SIZE_LATITUDE_DEGMIN);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_LATITUDE_HUNDREDTHS, SIZE_HUNDREDTHS);
        createGlobalPagedVariable(model, STATE_VARIABLE_PAGE,
                VARIABLE_GPS_FLAGS, 1).addFlag(GPS_FLAG_GPS_NEW_QUALITY)
                .addFlag(GPS_FLAG_GPS_NEW_POSITION).addFlag(GPS_FLAG_GPS_NORTH)
                .addFlag(GPS_FLAG_GPS_EAST);

        final Node initial = model.getInitialState();
        final Node dollar = model.createNamedNode("dollar");
        initial.addTransition(new Transition().when(
                Precondition.equals(input, '$')).goTo(dollar));

        buildSubroutineForLatLong(model, dollar);
        buildSubroutineForTime(model, dollar);
        buildGpGGA(model, dollar);
        buildGpRMC(model, dollar);
        return model;
    }

    /**
     * Create a GLOBAL paged variable in the given model.
     * 
     * @param model
     *            model to add the variable to
     * @param page
     *            page for the variable
     * @param name
     *            name of the variable
     * @param size
     *            size to reserve for the variable
     * @return the created variable.
     */
    private static Variable createGlobalPagedVariable(final StateModel model,
            final int page, final String name, final int size)
    {
        final Variable v = new Variable(name, SymbolOwnership.GLOBAL, page,
                size);
        model.addVariable(v);
        return v;
    }

    /**
     * Create a GLOBAL ACCESS variable in the given model.
     * 
     * @param model
     *            model to add the variable to
     * @param name
     *            name of the variable
     * @param size
     *            size to reserve for the variable
     * @return the created variable.
     */
    private static Variable createGlobalAccessVariable(final StateModel model,
            final String name, final int size)
    {
        final Variable v = new Variable(name, SymbolOwnership.GLOBAL,
                Variable.ACCESS_BANK, size);
        model.addVariable(v);
        return v;
    }

    /**
     * Define the state model for GPGGA - System fix data.
     * 
     * @param model
     *            model to build in to.
     * @param dollar
     *            node that the machine is in after reading the initial '$'
     */
    private static void buildGpGGA(final StateModel model, final Node dollar)
    {
        final Variable input = model.getInputVariable();
        final Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);

        dollar.addString("GPGGA,")
                .addEntryCondition(
                        Precondition.checkFlag(flags,
                                GPS_FLAG_GPS_NEW_POSITION, false))
                .addEntryCommand(new GosubCommand(SUBROUTINE_READ_TIME))
                .addEntryCommand(new GosubCommand(SUBROUTINE_READ_LAT_LONG))
                .addString(",")
                .addChoices(
                        new Transition()
                                .whenInRange(input, '0', '2')
                                .doCommand(
                                        Command.storeValue(
                                                input,
                                                model.getVariable(VARIABLE_GPS_QUALITY)),
                                        Command.setFlag(flags,
                                                GPS_FLAG_GPS_NEW_QUALITY, true))
                                .goTo(model.getInitialState()));
    }

    /**
     * Build just enough of GPRMC to read time and position.
     * 
     * @param model
     *            model to build in to.
     * @param dollar
     *            node that the machine is in after reading the initial '$'
     */
    private static void buildGpRMC(final StateModel model, final Node dollar)
    {
        final Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);

        dollar.addString("GPRMC,")
                .addEntryCondition(
                        Precondition.checkFlag(flags,
                                GPS_FLAG_GPS_NEW_POSITION, false))
                .addEntryCommand(new GosubCommand(SUBROUTINE_READ_TIME))
                .addString("A,")
                .addEntryCommand(new GosubCommand(SUBROUTINE_READ_LAT_LONG));
    }

    /**
     * The read time subroutine reads 6 digits, then skips to the comma. The
     * subroutine ends as the comma is read.
     * 
     * @param model
     *            model to build in to
     * @param dollar
     *            node to go to id an unexpected $ is seen in the input.
     */
    private static void buildSubroutineForTime(final StateModel model,
            final Node dollar)
    {
        final Variable input = model.getInputVariable();

        model.createNamedNode(SUBROUTINE_READ_TIME)
                .addNumbers(SIZE_GPS_TIME, model.getVariable(VARIABLE_GPS_TIME))
                .skipToCommaElse(
                        new Transition().when(Precondition.equals(input, '$'))
                                .goTo(dollar))
                .addEntryCommand(new ReturnFromSubroutineCommand());
    }

    /**
     * Create a state machine to read Lat,Long.
     * 
     * @param model
     *            model to build in to.
     * @param dollar
     *            node to go to if the '$' symbol is seen.
     */
    private static void buildSubroutineForLatLong(final StateModel model,
            final Node dollar)
    {

        final Variable input = model.getInputVariable();
        final Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);

        final Node entryNode = model.createNamedNode(SUBROUTINE_READ_LAT_LONG);
        entryNode
                .addNumbers(SIZE_LATITUDE_DEGMIN,
                        model.getVariable(VARIABLE_GPS_LATITUDE_DEGMIN))
                .addString(".")
                .addNumbers(SIZE_HUNDREDTHS, SIZE_HUNDREDTHS,
                        model.getVariable(VARIABLE_GPS_LATITUDE_HUNDREDTHS))
                .skipToCommaElse(
                        new Transition().when(Precondition.equals(input, '$'))
                                .goTo(dollar))
                .addChoices(
                        new Transition()
                                .whenEqual(input, 'S')
                                .doCommand(
                                        Command.setFlag(flags,
                                                GPS_FLAG_GPS_NORTH, false))
                                .goTo("readLongitude"),
                        new Transition()
                                .whenEqual(input, 'N')
                                .doCommand(
                                        Command.setFlag(flags,
                                                GPS_FLAG_GPS_NORTH, true))
                                .goTo("readLongitude"));

        model.createNamedNode("readLongitude")
                .addString(",")
                .addNumbers(SIZE_LONGITUDE_DEGMIN,
                        model.getVariable(VARIABLE_GPS_LONGITUDE_DEGMIN))
                .addString(".")
                .addNumbers(2, 2,
                        model.getVariable(VARIABLE_GPS_LONGITUDE_HUNDREDTHS))
                .skipToCommaElse(
                        new Transition().when(Precondition.equals(input, '$'))
                                .goTo(dollar))
                .addChoices(
                        new Transition()
                                .whenEqual(input, 'E')
                                .doCommand(
                                        Command.setFlag(flags,
                                                GPS_FLAG_GPS_EAST, true))
                                .goTo("latLongComplete"),
                        new Transition()
                                .whenEqual(input, 'W')
                                .doCommand(
                                        Command.setFlag(flags,
                                                GPS_FLAG_GPS_EAST, false))
                                .goTo("latLongComplete"));

        final Node afterLatLong = model.createNamedNode("latLongComplete");
        afterLatLong.addEntryCommand(Command.setFlag(flags,
                GPS_FLAG_GPS_NEW_POSITION, true));
        afterLatLong.addEntryCommand(new ReturnFromSubroutineCommand());
    }
}
