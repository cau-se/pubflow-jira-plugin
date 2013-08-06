package de.pubflow.common.entity.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class WFParameter {
	
	public WFParameter(){}
	
	private String key;
	private String payloadClazz;
	private int intValue;
	private String stringValue;
	private long longValue;
	private double doubleValue;

	/**
	 * @return the payloadClazz
	 */
	public String getPayloadClazz() {
		return payloadClazz;
	}
	/**
	 * @param payloadClazz the payloadClazz to set
	 */
	public void setPayloadClazz(String payloadClazz) {
		this.payloadClazz = payloadClazz;
	}
	/**
	 * @return the stringValue
	 */
	public String getStringValue() {
		return stringValue;
	}
	/**
	 * @param stringValue the stringValue to set
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	/**
	 * @return the longValue
	 */
	public long getLongValue() {
		return longValue;
	}
	/**
	 * @param longValue the longValue to set
	 */
	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
	/**
	 * @return the doubleValue
	 */
	public double getDoubleValue() {
		return doubleValue;
	}
	/**
	 * @param doubleValue the doubleValue to set
	 */
	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}
	/**
	 * @return the intValue
	 */
	public int getIntValue() {
		return intValue;
	}
	/**
	 * @param intValue the intValue to set
	 */
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	@XmlElement(name="key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

}
