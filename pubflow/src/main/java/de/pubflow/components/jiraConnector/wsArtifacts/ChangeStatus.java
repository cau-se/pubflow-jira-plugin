
package de.pubflow.components.jiraConnector.wsArtifacts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changeStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="changeStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="issueId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="statusId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeStatus", propOrder = {
    "issueId",
    "statusId"
})
public class ChangeStatus {

    protected long issueId;
    protected String statusId;

    /**
     * Gets the value of the issueId property.
     * 
     */
    public long getIssueId() {
        return issueId;
    }

    /**
     * Sets the value of the issueId property.
     * 
     */
    public void setIssueId(long value) {
        this.issueId = value;
    }

    /**
     * Gets the value of the statusId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusId() {
        return statusId;
    }

    /**
     * Sets the value of the statusId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusId(String value) {
        this.statusId = value;
    }

}
