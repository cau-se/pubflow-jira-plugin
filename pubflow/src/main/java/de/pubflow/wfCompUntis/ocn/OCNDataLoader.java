package de.pubflow.wfCompUntis.ocn;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.sql.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import de.pubflow.common.properties.PropLoader;
import de.pubflow.wfCompUntis.ocn.entity.Bottle;
import de.pubflow.wfCompUntis.ocn.entity.Leg;
import de.pubflow.wfCompUntis.ocn.entity.Parameter;
import de.pubflow.wfCompUntis.ocn.entity.Sample;
import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;

public class OCNDataLoader {
	
	public String getData(int id, int instanceId) throws Exception {

		
		
		try{
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
			String connectionURL = PropLoader.getInstance().getProperty("dbUrl", this.getClass().getCanonicalName(), "jdbc:postgresql://192.168.48.70:5432/ocn?schema=ocn");
			try {
				Class.forName("org.postgresql.Driver").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}		
			
			
			String user = PropLoader.getInstance().getProperty("user", this.getClass().getCanonicalName(), "arl");
			String password = PropLoader.getInstance().getProperty("pw", this.getClass().getCanonicalName(), "dWmWidW");
			connection = DriverManager.getConnection(connectionURL, user, password);
			statement = connection.createStatement();

			log.append("LEG...\t\t\t\t\t");

			//Query für die leg-Daten
			queryString = 
					"WITH part as (SELECT * from ocn.leg)" +
							"SELECT name AS leg_name, expocode AS leg_expocode, id AS leg_id FROM part WHERE id = " + id;

			rs = statement.executeQuery(queryString);
			rs.next();

			//Generiere leg-Objekt aus Tabellendaten
			leg = Leg.createFromResultSet(Leg.class, Leg.c_LEGTABLE, rs);

			log.append("OK\n");

			log.append("SAMPLES/BOTTLES...\t\t\t");


			//Query für bottle- und sample-Daten
			queryString = 
					"WITH part AS " +
							"(WITH part AS " +
							"(SELECT latitude AS bottle_latitude, longitude AS bottle_longitude, leg_id, waterdepth AS bottle_waterdepth, time AS bottle_time, station AS bottle_station, label AS bottle_label, id AS bottle_id " +
							"FROM ocn.bottle) " +
							"SELECT * " +
							"FROM part " +
							"WHERE leg_id = " + id + ") " +
							"SELECT part.*, sample.id AS sample_id, sample.val AS sample_val, parameter_id AS sample_parameter_unit_id, flag AS sample_flag " +
							"FROM part, ocn.sample " +
							"WHERE part.bottle_id = sample.bottle_id " +
							"ORDER BY sample.bottle_id;";

			rs = statement.executeQuery(queryString);

			PubJect bottle = null;
			PubJect sample = null;
			String before = "";

			//Erstelle bottle- und sample-Objekte
			while(rs.next()){
				//Prüfe ob Datensatz zu einer neuen bottle gehört
				if(!before.equals(rs.getString(Bottle.c_id))){
					if(bottle != null){
						if (verbose) log.append("Collected " + bottle.getList(Bottle.c_sampleList).size() + " samples of bottle no. " + bottle.getString(Bottle.c_label) + "/" + bottle.getString(Bottle.c_station) + "\n");
						leg.addToList(Leg.c_bottleList, bottle);
					}
					//Generiere bottle-Objekt aus Tabellendaten
					bottle = PubJect.createFromResultSet(Bottle.class, Bottle.c_BOTTLETABLE, rs);
				}

				//Generiere sample-Objekt aus Tabellendaten
				sample = PubJect.createFromResultSet(Sample.class, Sample.c_SAMPLETABLE, Sample.c_SAMPLETABLETYPE, rs);

				//Ergänze Parameterliste
				parameters.add(sample.getString(Sample.c_parameterUnitId));
				bottle.addToList(Bottle.c_sampleList, sample);

				before = rs.getString(Bottle.c_id);
			}

			log.append("OK\n");

			log.append("PARAMETER...\t\t\t\t");

			//Hole Parameterinformationen
			for(String s : parameters) {
				queryString = 
						"SELECT p.parameter_description, p.parameter_comment, pu.parameter_unit_pangaea_id, pu.parameter_unit_id, u.unit_mathematical as parameter_unit_mathematical, '' as parameter_method_id, '' as parameter_pi_id " +
								"FROM ocn.parameter p, ocn.parameter_unit pu, ocn.unit u " +
								"WHERE " +
								"p.parameter_id = pu.parameter_id AND pu.unit_id = u.unit_id AND pu.parameter_unit_id = " + s;

				rs = statement.executeQuery(queryString);

				while(rs.next()){
					PubJect parameter;

					//Generiere parameter-Objekt aus Tabellendaten
					parameter = PubJect.createFromResultSet(Parameter.class, Parameter.c_PARAMETERTABLE, rs);
					StringTokenizer st1 = new StringTokenizer(parameter.getString(Parameter.c_description), "-");

					String parameterName = st1.nextToken();
					String parameterAbbr;

					//Teile nicht-atomare Parameternamen auf. Muster: "Name - Abkürzung"
					if(st1.hasMoreTokens()){
						parameterAbbr = st1.nextToken().substring(1);		
						parameterName = parameterName.substring(0, parameterName.length() - 2);

						parameter.add(Parameter.c_abbreviation, parameterAbbr);
					}

					parameter.add(Parameter.c_name, parameterName);

					leg.addToList(Leg.c_parameterList, parameter);

				}
			}


			log.append("OK\n");

			long time_samples = System.currentTimeMillis();

			JAXBContext ctx = JAXBContext.newInstance(Leg.class);
			Marshaller m = ctx.createMarshaller();
			StringWriter sw = new StringWriter();
			log.append("Fetched data in " + (time_samples - time_start) + "ms.\n");

			leg.add(Leg.c_logString, log.toString());

			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//m.marshal(leg, new File("/home/arl/dto.txt"));
			m.marshal(leg, sw);


			return sw.toString();

		}catch(Exception e){

			try {
				PrintStream fw = new PrintStream(PropLoader.getInstance().getProperty("DBConnectorExceptionFile", this.getClass().getCanonicalName(), "exceptionDBConnector.txt"));
				fw.append(e.toString());
				fw.append(e.getMessage());
				fw.append(e.getLocalizedMessage());
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			return "";
		}

	}
	
	public static void main(String[] args) {
		OCNDataLoader loader = new OCNDataLoader();
		OCNToPangaeaMapper mapper = new OCNToPangaeaMapper();
		FileCreator4D fc4d = new FileCreator4D();
		String data="";
		String result = "";
		try {
			data = loader.getData(3, 0);
			result = mapper.replaceArtefacts(data, 0);
			fc4d.toCSV(result, "3", "arl", "0", "0", "0", "0", "0", "etc/4d.4d", "0", "4d.4d", "0", 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
