package org.matsim.nuremberg;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GenerateRandomDemand7 {
    //Specify all input files
    private static final String NETWORKFILE = "input7/NurembergNetwork7c.xml";      //Network for traffic assignment
    private static final String COUNTIES = "input7/lkr_ex.shp";            //Polygon shapefile for demand generation
    private static final String PLANSFILEOUTPUT = "input7/NurembergPlans7.xml";    //The output file of demand generation
    //Define objects and parameters
    private Scenario scenario;
    private Map<String,Geometry> shapeMap;
    //Due to the huge number of commuters, it's preferable to decrease the size of simulating agents.
    //+ This scale factor should be the same as the flow capacity factor. Try different value to evaluate the performance.
    private static double SCALEFACTOR = 0.01;
    //Define the coordinate transformation function
    private final CoordinateTransformation transformation =
            TransformationFactory.getCoordinateTransformation("EPSG:31468", TransformationFactory.DHDN_GK4);


    // Entering point of the class ""Generate Random Demand
    public static void main(String[] args) {
        GenerateRandomDemand7 grd = new GenerateRandomDemand7();
        grd.run();
    }

    //A constructor for this class, which is to set up the scenario container.
    GenerateRandomDemand7 (){
        this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
    }
    //Generate randomly sampling demand
    private void run() {
        this.shapeMap = readShapeFile(COUNTIES, "SCH");

        //write a new method to create OD relations
        createOD(11158,0.6,"09564","09563","Nürnberg City-Fürth City");
        createOD(10690,0.4,"09564","09562","Nürnberg City-Erlangen City");
        createOD(7151,0.4,"09564","09574","Nürnberg City-Nürnberger Land");
        createOD(6032,0.5,"09564","09572","Nürnberg City-Erlangen-Höchstadt");
        createOD(4154,0.5,"09564","09573","Nürnberg City-Fürth");
        createOD(3194,0.6,"09564","09576","Nürnberg City-Roth");
        createOD(2426,0.8,"09564","09565","Nürnberg City-Schwabach");
        createOD(1061,0.6,"09564","09373","Nürnberg City-Neumarkt");
        createOD(1018,0.7,"09564","09474","Nürnberg City-Forchheim");
        createOD(735,0.5,"09564","09571","Nürnberg City-Ansbach");

        createOD(22809,0.4,"09574","09564","Nürnberger Land-Nürnberg City");
        createOD(22513,0.6,"09563","09564","Fürth City-Nürnberg City");
        createOD(19326,0.5,"09573","09564","Fürth-Nürnberg City");
        createOD(12745,0.6,"09576","09564","Roth-Nürnberg City");
        createOD(9171,0.5,"09572","09564","Erlangen-Höchstadt-Nürnberg City");
        createOD(6467,0.4,"09562","09564","Erlangen City-Nürnberg City");
        createOD(6127,0.7,"09474","09564","Forchheim-Nürnberg City");
        createOD(5750,0.8,"09565","09564","Schwabach City-Nürnberg City");
        createOD(5356,0.6,"09373","09564","Neumarkt-Nürnberg City");
        createOD(4064,0.5,"09571","09564","Ansbach-Nürnberg City");

        createOD(143929,0.4,"09564","09564","Nürnberg City-Nürnberg City");

        //Write the population file to specified folder
        PopulationWriter pw = new PopulationWriter(scenario.getPopulation(),scenario.getNetwork());
        pw.write(PLANSFILEOUTPUT);
    }

    //Read in shapefile
    public Map<String,Geometry> readShapeFile(String filename, String attrString){
        Map<String,Geometry> shapeMap = new HashMap<String, Geometry>();
        for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {
            GeometryFactory geometryFactory= new GeometryFactory();
            WKTReader wktReader = new WKTReader(geometryFactory);
            Geometry geometry;
            try {
                geometry = wktReader.read((ft.getAttribute("the_geom")).toString());
                shapeMap.put(ft.getAttribute(attrString).toString(),geometry);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return shapeMap;
    }
    //Create random coordinates within a given polygon
    private  Coord drawRandomPointFromGeometry(Geometry g) {
        Random rnd = MatsimRandom.getLocalInstance();
        Point p;
        double x, y;
        do {
            x = g.getEnvelopeInternal().getMinX() +  rnd.nextDouble() * (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal().getMinX());
            y = g.getEnvelopeInternal().getMinY() + rnd.nextDouble() * (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal().getMinY());
            p = MGC.xy2Point(x,y);
        } while (!g.contains(p));
        Coord coord = new Coord(p.getX(), p.getY());
        return coord;
    }


    //Create od relations for each counties
    private void createOD(int pop, double shareOfcar, String origin, String destination, String toFromPrefix) {

        //Specify the number of commuters and the modal split of this relation
        double commuters = pop*SCALEFACTOR;
        double carcommuters = shareOfcar * commuters;
        //Specify the ID of these two cities, which is the SCH attribute.
        Geometry home = this.shapeMap.get(origin);
        Geometry work = this.shapeMap.get(destination);
        //Randomly creating the home and work location of each commuters
        for (int i = 0; i<=commuters;i++) {
            //Specify the transportation mode
            String mode = "car";
            if (i > carcommuters) mode = "pt"; // Change to pt cuz teleportation is used
            //Specify the home location randomly and transform the coordinate
            Coord homec = drawRandomPointFromGeometry(home);
            homec = transformation.transform(homec);
            //Specify the working location randomly and transform the coordinate
            Coord workc = drawRandomPointFromGeometry(work);
            workc = transformation.transform(workc);
            //Create plan for each commuter
            createOnePerson(i, homec, workc, mode, toFromPrefix);
        }
    }



    //Create plan for each commuters
    private void createOnePerson(int i, Coord coord, Coord coordWork, String mode, String toFromPrefix) {

        double variance = Math.random()*60*2;

        Id<Person> personId = Id.createPersonId(toFromPrefix+i);
        Person person = scenario.getPopulation().getFactory().createPerson(personId);

        Plan plan = scenario.getPopulation().getFactory().createPlan();

        Activity home = scenario.getPopulation().getFactory().createActivityFromCoord("home", coord);
        home.setEndTime(9*60*60+variance*60);
        plan.addActivity(home);

        Leg departureLeg = scenario.getPopulation().getFactory().createLeg(mode);
        plan.addLeg(departureLeg);

        Activity work = scenario.getPopulation().getFactory().createActivityFromCoord("work", coordWork);
        work.setEndTime(17*60*60+variance*60);
        plan.addActivity(work);

        Leg returnLeg = scenario.getPopulation().getFactory().createLeg(mode);
        plan.addLeg(returnLeg);

        Activity home2 = scenario.getPopulation().getFactory().createActivityFromCoord("home", coord);
        plan.addActivity(home2);

        person.addPlan(plan);
        scenario.getPopulation().addPerson(person);
    }

}