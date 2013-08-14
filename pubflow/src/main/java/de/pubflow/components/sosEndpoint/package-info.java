/**
 * 
 */
/**
 * @author pcb
 * 
 * The Sensor Observation Service aggregates readings from live, in-situ and remote sensors. The service provides an interface to make sensors and sensor data archives accessible via an interoperable web based interface. Four profiles are defined within the SOS specification: core, transactional, enhanced, and entire. The current release (52N-SOS-3.2.0) implements the core profile comprising the mandatory operations:

 GetCapabilities, for requesting a self-description of the service.
 GetObservation, for requesting the pure sensor data encoded in Observations & Measurements (O&M)
 DescribeSensor, for requesting information about the sensor itself, encoded in a Sensor Model Language (SensorML) instance document.
 The transactional profile comprising of the following operations is implemented, too:

 RegisterSensor, for signing up new sensors.
 InsertObservation, for inserting new observations for registered sensors.
 Additionally, the following operations are implemted:

 GetFeatureOfInterest, for requesting the GML encoded representation of the feature that is the target of the observation.
 GetResult, for periodically polling of sensor data
 The current release implements the latest schema version (1.0.0).
 (source:http://52north.org/communities/sensorweb/sos/ )
 *
 */
package de.pubflow.components.sosEndpoint;