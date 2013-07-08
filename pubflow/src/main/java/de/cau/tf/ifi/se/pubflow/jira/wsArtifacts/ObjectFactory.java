
package de.cau.tf.ifi.se.pubflow.jira.wsArtifacts;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.pubflow.jira.webservice package. 
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

    private final static QName _CreateIssueType_QNAME = new QName("http://webservice.jira.pubflow.de/", "createIssueType");
    private final static QName _CreateIssueTypeResponse_QNAME = new QName("http://webservice.jira.pubflow.de/", "createIssueTypeResponse");
    private final static QName _GetStatusNames_QNAME = new QName("http://webservice.jira.pubflow.de/", "getStatusNames");
    private final static QName _AddIssueCommentResponse_QNAME = new QName("http://webservice.jira.pubflow.de/", "addIssueCommentResponse");
    private final static QName _AddIssueComment_QNAME = new QName("http://webservice.jira.pubflow.de/", "addIssueComment");
    private final static QName _ChangeStatus_QNAME = new QName("http://webservice.jira.pubflow.de/", "changeStatus");
    private final static QName _ChangeStatusResponse_QNAME = new QName("http://webservice.jira.pubflow.de/", "changeStatusResponse");
    private final static QName _CreateIssueResponse_QNAME = new QName("http://webservice.jira.pubflow.de/", "createIssueResponse");
    private final static QName _CreateIssue_QNAME = new QName("http://webservice.jira.pubflow.de/", "createIssue");
    private final static QName _GetStatusNamesResponse_QNAME = new QName("http://webservice.jira.pubflow.de/", "getStatusNamesResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.pubflow.jira.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateIssue }
     * 
     */
    public CreateIssue createCreateIssue() {
        return new CreateIssue();
    }

    /**
     * Create an instance of {@link CreateIssue.Params }
     * 
     */
    public CreateIssue.Params createCreateIssueParams() {
        return new CreateIssue.Params();
    }

    /**
     * Create an instance of {@link CreateIssueType }
     * 
     */
    public CreateIssueType createCreateIssueType() {
        return new CreateIssueType();
    }

    /**
     * Create an instance of {@link CreateIssueType.Params }
     * 
     */
    public CreateIssueType.Params createCreateIssueTypeParams() {
        return new CreateIssueType.Params();
    }

    /**
     * Create an instance of {@link GetStatusNames }
     * 
     */
    public GetStatusNames createGetStatusNames() {
        return new GetStatusNames();
    }

    /**
     * Create an instance of {@link CreateIssueTypeResponse }
     * 
     */
    public CreateIssueTypeResponse createCreateIssueTypeResponse() {
        return new CreateIssueTypeResponse();
    }

    /**
     * Create an instance of {@link AddIssueCommentResponse }
     * 
     */
    public AddIssueCommentResponse createAddIssueCommentResponse() {
        return new AddIssueCommentResponse();
    }

    /**
     * Create an instance of {@link AddIssueComment }
     * 
     */
    public AddIssueComment createAddIssueComment() {
        return new AddIssueComment();
    }

    /**
     * Create an instance of {@link ChangeStatus }
     * 
     */
    public ChangeStatus createChangeStatus() {
        return new ChangeStatus();
    }

    /**
     * Create an instance of {@link ChangeStatusResponse }
     * 
     */
    public ChangeStatusResponse createChangeStatusResponse() {
        return new ChangeStatusResponse();
    }

    /**
     * Create an instance of {@link GetStatusNamesResponse }
     * 
     */
    public GetStatusNamesResponse createGetStatusNamesResponse() {
        return new GetStatusNamesResponse();
    }

    /**
     * Create an instance of {@link CreateIssueResponse }
     * 
     */
    public CreateIssueResponse createCreateIssueResponse() {
        return new CreateIssueResponse();
    }

    /**
     * Create an instance of {@link CreateIssue.Params.Entry }
     * 
     */
    public CreateIssue.Params.Entry createCreateIssueParamsEntry() {
        return new CreateIssue.Params.Entry();
    }

    /**
     * Create an instance of {@link CreateIssueType.Params.Entry }
     * 
     */
    public CreateIssueType.Params.Entry createCreateIssueTypeParamsEntry() {
        return new CreateIssueType.Params.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateIssueType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "createIssueType")
    public JAXBElement<CreateIssueType> createCreateIssueType(CreateIssueType value) {
        return new JAXBElement<CreateIssueType>(_CreateIssueType_QNAME, CreateIssueType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateIssueTypeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "createIssueTypeResponse")
    public JAXBElement<CreateIssueTypeResponse> createCreateIssueTypeResponse(CreateIssueTypeResponse value) {
        return new JAXBElement<CreateIssueTypeResponse>(_CreateIssueTypeResponse_QNAME, CreateIssueTypeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusNames }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "getStatusNames")
    public JAXBElement<GetStatusNames> createGetStatusNames(GetStatusNames value) {
        return new JAXBElement<GetStatusNames>(_GetStatusNames_QNAME, GetStatusNames.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddIssueCommentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "addIssueCommentResponse")
    public JAXBElement<AddIssueCommentResponse> createAddIssueCommentResponse(AddIssueCommentResponse value) {
        return new JAXBElement<AddIssueCommentResponse>(_AddIssueCommentResponse_QNAME, AddIssueCommentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddIssueComment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "addIssueComment")
    public JAXBElement<AddIssueComment> createAddIssueComment(AddIssueComment value) {
        return new JAXBElement<AddIssueComment>(_AddIssueComment_QNAME, AddIssueComment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "changeStatus")
    public JAXBElement<ChangeStatus> createChangeStatus(ChangeStatus value) {
        return new JAXBElement<ChangeStatus>(_ChangeStatus_QNAME, ChangeStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "changeStatusResponse")
    public JAXBElement<ChangeStatusResponse> createChangeStatusResponse(ChangeStatusResponse value) {
        return new JAXBElement<ChangeStatusResponse>(_ChangeStatusResponse_QNAME, ChangeStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateIssueResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "createIssueResponse")
    public JAXBElement<CreateIssueResponse> createCreateIssueResponse(CreateIssueResponse value) {
        return new JAXBElement<CreateIssueResponse>(_CreateIssueResponse_QNAME, CreateIssueResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateIssue }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "createIssue")
    public JAXBElement<CreateIssue> createCreateIssue(CreateIssue value) {
        return new JAXBElement<CreateIssue>(_CreateIssue_QNAME, CreateIssue.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusNamesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jira.pubflow.de/", name = "getStatusNamesResponse")
    public JAXBElement<GetStatusNamesResponse> createGetStatusNamesResponse(GetStatusNamesResponse value) {
        return new JAXBElement<GetStatusNamesResponse>(_GetStatusNamesResponse_QNAME, GetStatusNamesResponse.class, null, value);
    }

}
