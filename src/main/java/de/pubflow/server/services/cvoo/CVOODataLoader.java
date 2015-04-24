package de.pubflow.server.services.cvoo;

import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.properties.PropLoader;
import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.services.ocn.entity.Bottle;
import de.pubflow.server.services.ocn.entity.Leg;
import de.pubflow.server.services.ocn.entity.Parameter;
import de.pubflow.server.services.ocn.entity.Sample;
import de.pubflow.server.services.ocn.entity.abstractClass.PubJect;

/**
 * @author arl
 *
 */
public class CVOODataLoader {

	static Logger myLogger = LoggerFactory.getLogger(CVOODataLoader.class);

	public final String DEFAULT_DBURL = "jdbc:postgresql://localhost:5433/bottleocn?schema=bottleocn";
	public final String DEFAULT_DBUSER = "secret";
	public final String DEFAULT_DBPASSWORD = "secret";
	
	public ComMap getData(ComMap data, int instanceId) throws Exception {
		String legId = data.get("de.pubflow.services.cvoo.PluginAllocator.getData.legid");	
		PropLoader props = PropLoader.getInstance();
		long millis = System.currentTimeMillis();

		try{
			String connectionURL = props.getProperty("DBURL", this.getClass(), DEFAULT_DBURL);  //$NON-NLS-2$
			String user = PropLoader.getInstance().getProperty("DBUSER", this.getClass(), DEFAULT_DBUSER);  //$NON-NLS-2$
			String password = PropLoader.getInstance().getProperty("DBPASSWORD", this.getClass(), DEFAULT_DBPASSWORD);  //$NON-NLS-2$

			StringBuilder log = new StringBuilder();

			String queryString;
			Connection connection;
			Statement statement;
			ResultSet rs;

			Leg leg = null;

			long time_start = System.currentTimeMillis();
			Set<String> parameters = new HashSet<String>();
			boolean verbose = false;

			//Datenbankzeugs...
			Class.forName("org.postgresql.Driver").newInstance(); 
			
			Enumeration<Driver> enu = DriverManager.getDrivers();
			while(enu.hasMoreElements()){
				Driver d;
				if((d = enu.nextElement()).getClass().getCanonicalName().equals("org.hsqldb.jdbc.JDBCDriver")){
					DriverManager.deregisterDriver(d);
				}
			}
			
			
			connection = DriverManager.getConnection(connectionURL, user, password);
			statement = connection.createStatement();

			log.append(String.format("=================================================== START JOB ==================================================\n" +  
					"Fetching data for leg_id \t\t %s \n" + 
					"LEG...\t\t\t\t\t", legId));

			// Query für die leg-Daten
			// ONLY POSTGRES VESION 9.0 ==>

//			queryString = 
//					"WITH part as (SELECT * from cvoo.leg)" + 
//							"SELECT " + 
//							"name AS leg_name, expocode AS leg_expocode, id AS leg_id " + 
//							"FROM part WHERE id = " + legId; 

			 queryString = "select name as leg_name, expocode as leg_expocode, id as leg_id from bottleocn.leg where id = " + legId;

			rs = statement.executeQuery(queryString);
			rs.next();

			try{
				// Generiere leg-Objekt aus Tabellendaten
				leg = Leg.createFromResultSet(Leg.class, Leg.LEGTABLE, rs);

			}catch(SQLException e){
				e.printStackTrace();
				throw new Exception(String.format("There is no data for legid %s in the cvoo database or the view 'leg' has been changed. \n", legId)); 
			}

			log.append("OK\n"); 
			log.append("SAMPLES/BOTTLES...\t\t\t"); 

			// Query für bottle- und sample-Daten
			// ONLY POSTGRES VESION 9.0 ==>

//			queryString = 
//					"WITH part AS " + 
//							"(WITH part AS " + 
//							"(SELECT latitude AS bottle_latitude, longitude AS bottle_longitude, leg_id, " + 
//							"waterdepth AS bottle_waterdepth, time AS bottle_time, station AS bottle_station, " + 
//							"label AS bottle_label, id AS bottle_id " + 
//							"FROM cvoo.bottle) " + 
//							"SELECT * " + 
//							"FROM part " + 
//							"WHERE leg_id = " + legId + ") " +  //$NON-NLS-2$
//							"SELECT part.*, sample.id AS sample_id, sample.val AS sample_val, " + 
//							"parameter_id AS sample_parameter_unit_id, flag AS sample_flag " + 
//							"FROM part, cvoo.sample " + 
//							"WHERE part.bottle_id = sample.bottle_id " + 
//							"ORDER BY sample.bottle_id"; 

				queryString = "SELECT bottle.latitude AS bottle_latitude, bottle.longitude AS bottle_longitude, bottle.leg_id, " +
								"bottle.waterdepth AS bottle_waterdepth, bottle.time AS bottle_time, bottle.station AS bottle_station, " +
								"bottle.label AS bottle_label, bottle.id AS bottle_id, sample.id AS sample_id, sample.val AS sample_val, " +
								"parameter_id AS sample_parameter_unit_id, flag AS sample_flag " +
								"FROM bottleocn.bottle, bottleocn.sample " +
								"WHERE bottle.id = sample.bottle_id and bottle.leg_id = " + legId +
								"ORDER BY sample.bottle_id;";

			rs = statement.executeQuery(queryString);

			PubJect bottle = null;
			PubJect sample = null;
			String before = ""; 

			//Erstelle bottle- und sample-Objekte
			while(rs.next()){
				//Prüfe ob Datensatz zu einer neuen bottle gehört
				if(!before.equals(rs.getString(Bottle.ID))){
					if(bottle != null){
						if (verbose) log.append(String.format("Collected %d samples of bottle no. %s / %s \n", bottle.getList(Bottle.SAMPLELIST).size(), bottle.getString(Bottle.LABEL), bottle.getString(Bottle.STATION))); 
						leg.addToList(Leg.BOTTLELIST, bottle);
					}
					//Generiere bottle-Objekt aus Tabellendaten
					bottle = PubJect.createFromResultSet(Bottle.class, Bottle.c_BOTTLETABLE, rs);
				}

				//Generiere sample-Objekt aus Tabellendaten
				sample = PubJect.createFromResultSet(Sample.class, Sample.c_SAMPLETABLE, Sample.c_SAMPLETABLETYPE, rs);

				//Ergänze Parameterliste
				parameters.add(sample.getString(Sample.PARAMETERUNITID));
				bottle.addToList(Bottle.SAMPLELIST, sample);

				before = rs.getString(Bottle.ID);
			}

			log.append("OK\n"); 
			log.append("PARAMETER...\t\t\t\t"); 

			//Hole Parameterinformationen
			for(String s : parameters) {
				queryString = 
						"SELECT p.shortname as parameter_description, p.shortname as parameter_abbreviation, '' as parameter_comment, " + 
								"p.pangaeaid as parameter_unit_pangaea_id, p.id as parameter_unit_id, " + 
								"p.unit as parameter_unit_mathematical, '' as parameter_method_id, '' as parameter_pi_id " + 
								"FROM bottleocn.parameter p " + 
								"WHERE " + 
								" p.id = " + s; 

				rs = statement.executeQuery(queryString);

				while(rs.next()){
					PubJect parameter;

					//Generiere parameter-Objekt aus Tabellendaten
					parameter = PubJect.createFromResultSet(Parameter.class, Parameter.c_PARAMETERTABLE, rs);
					StringTokenizer st1 = new StringTokenizer(parameter.getString(Parameter.DESCRIPTION), "-"); 

					String parameterName = st1.nextToken();
					String parameterAbbr;
					if(st1.countTokens() > 1){
						//Teile nicht-atomare Parameternamen auf. Muster: "Name - Abkürzung"
						if(st1.hasMoreTokens()){
							parameterAbbr = st1.nextToken().substring(1);		
							parameterName = parameterName.substring(0, parameterName.length() - 2);

							parameter.add(Parameter.ABBREVIATION, parameterAbbr);
						}
					}

					parameter.add(Parameter.NAME, parameterName);
					leg.addToList(Leg.PARAMETERLIST, parameter);

				}
			}

			log.append("OK\n"); 

			long time_samples = System.currentTimeMillis();

			JAXBContext ctx = JAXBContext.newInstance(Leg.class);
			
			Marshaller m = ctx.createMarshaller();
			StringWriter legSw = new StringWriter();
			log.append(String.format("Fetched data in %d ms.\n\n", time_samples - time_start));  //$NON-NLS-2$

			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(leg, new File("/home/arl/cvoo.txt"));
			
			m.marshal(leg, legSw);

			data.put("de.pubflow.services.ocn.PluginAllocator.getData.log", log.toString()); 
			data.put("de.pubflow.services.ocn.PluginAllocator.getData.leg", legSw.toString()); 
			data.newJiraAttachment("debug_" + "de.pubflow.services.ocn.PluginAllocator.getData.leg", legSw.toString().getBytes());

			data.newJiraComment(String.format("CVOODataLoader: exited normally after %f s.", (System.currentTimeMillis() - millis)/1000.0)); 
			return data;

		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("CVOODataLoader: " + e.getMessage()); 
		}
	}
}
