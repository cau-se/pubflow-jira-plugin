package de.pubflow.service.ocn.entity.abstractClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.pubflow.service.ocn.entity.Bottle;
import de.pubflow.service.ocn.entity.Leg;
import de.pubflow.service.ocn.entity.Parameter;
import de.pubflow.service.ocn.entity.Sample;
import de.pubflow.service.ocn.exceptions.PubJectException;
import de.pubflow.service.ocn.mapping.MyHashMapListAdapter;



@XmlSeeAlso({Leg.class, Bottle.class, Parameter.class, Sample.class})
public abstract class LBPSContainer{

	public HashMap <String, String> attribs = new HashMap<String, String>();
	
	@XmlJavaTypeAdapter(MyHashMapListAdapter.class)
	public HashMap <String, ArrayList<LBPSContainer>> lists = new HashMap<String, ArrayList<LBPSContainer>>();

	public String toString(){
		String out = ""; 

		for(String s : attribs.keySet())
			out += s + " " + attribs.get(s).toString() + "\n";		

		for(String s : lists.keySet())
			out += s + " " + lists.get(s).toString() + "\n";		
		return out;
	}

	@SuppressWarnings("unchecked")
	public static <T extends LBPSContainer> T createFromResultSet(Class <T> clazz, String[] definition, ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException{
		LBPSContainer pj = clazz.newInstance();

		for(String s : definition)
			pj.add(s, rs.getString(s));

		return (T)pj;
	}

	@SuppressWarnings("unchecked")
	public static <T extends LBPSContainer> T createFromResultSet(Class<T> clazz, String[] definition, String[] types, ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException, PubJectException {
		LBPSContainer pj = clazz.newInstance();

		if(definition.length != types.length) throw new PubJectException("Improper type or field defintions!");

		for(int i = 0; i < definition.length; i++){
			if(types[i].equals("String")){
				pj.add(definition[i], rs.getString(definition[i]));
			}else if(types[i].equals("Double")){
				pj.add(definition[i], Double.toString(rs.getDouble(definition[i])));
			}else{
				throw new PubJectException("Unknown type in " + clazz.toString());
			}

		}

		return (T)pj;
	}

	public String getString(String s){
		Object o = attribs.get(s);
		if (o != null){

			if(o instanceof Integer) 
				return o.toString();

			if(o instanceof Double) 
				return o.toString();

			else 
				return o.toString();
		}else{
			return "";
		}
	}

	public ArrayList<LBPSContainer> getList(String s) throws PubJectException{
		return lists.get(s);
	}

	public void add(String s, String o){
		attribs.put(s, o);
	}

	public void addToList(String s, LBPSContainer o) throws PubJectException{

		if(lists.get(s) != null)
			(lists.get(s)).add(o);

		else{
			ArrayList<LBPSContainer> l = new ArrayList<LBPSContainer>();
			l.add(o);
			lists.put(s, l);
		}
	}

}
