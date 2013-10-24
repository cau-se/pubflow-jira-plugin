
package de.pubflow.components.jiraConnector.wsArtifacts;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.pubflow package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AddAttachment_QNAME = new QName("pubflow.de", "addAttachment");
    private final static QName _AddAttachmentResponse_QNAME = new QName("pubflow.de", "addAttachmentResponse");
    private final static QName _AddAttachmentBArray_QNAME = new QName("", "bArray");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.pubflow
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddAttachment }
     * 
     */
    public AddAttachment createAddAttachment() {
        return new AddAttachment();
    }

    /**
     * Create an instance of {@link AddAttachmentResponse }
     * 
     */
    public AddAttachmentResponse createAddAttachmentResponse() {
        return new AddAttachmentResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddAttachment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "pubflow.de", name = "addAttachment")
    public JAXBElement<AddAttachment> createAddAttachment(AddAttachment value) {
        return new JAXBElement<AddAttachment>(_AddAttachment_QNAME, AddAttachment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddAttachmentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "pubflow.de", name = "addAttachmentResponse")
    public JAXBElement<AddAttachmentResponse> createAddAttachmentResponse(AddAttachmentResponse value) {
        return new JAXBElement<AddAttachmentResponse>(_AddAttachmentResponse_QNAME, AddAttachmentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "bArray", scope = AddAttachment.class)
    public JAXBElement<byte[]> createAddAttachmentBArray(byte[] value) {
        return new JAXBElement<byte[]>(_AddAttachmentBArray_QNAME, byte[].class, AddAttachment.class, ((byte[]) value));
    }

}
