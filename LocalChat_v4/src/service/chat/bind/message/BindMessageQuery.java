package service.chat.bind.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import service.chat.bind.BindQueryHandler;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.impl.protocol.ResolverQuery;

public class BindMessageQuery extends ResolverQuery {
	private static final String documentRoot = ResolverQuery.getAdvertisementType();
	public static final String unicast = "unicast";
	public static final String propagate = "propagate";

	private String serviceName;
        
	private String senderName;
	private String username;
	private String password;
	private String bindType;

	public BindMessageQuery(String toServiceName,String senderName, String username, String password, String bindType) {
		super();
		this.serviceName = toServiceName;
		this.senderName = senderName;
		this.username = username;
		this.password = password;
		this.bindType = bindType;

	}

	public BindMessageQuery(InputStream stream) throws IOException {
		StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory
				.newStructuredDocument(new MimeMediaType("text/xml"), stream);
		Enumeration<?> elements = document.getChildren();
		while (elements.hasMoreElements()) {
			TextElement element = (TextElement) elements.nextElement();
			if (element.getName().equals("serviceName")) {
				serviceName = element.getTextValue();
				continue;
			}
			if (element.getName().equals("senderName")) {
				senderName = element.getTextValue();
				continue;
			}
			if (element.getName().equals("username")) {
				username = element.getTextValue();
				continue;
			}
			if (element.getName().equals("password")) {
				password = element.getTextValue();
				continue;
			}
			if (element.getName().equals("bindType")) {
				bindType = element.getTextValue();
			}
		}
	}

	@Override
	public Document getDocument(MimeMediaType asMimeType) {

		StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory
				.newStructuredDocument(asMimeType, documentRoot);
		TextElement element;
		element = document.createElement("serviceName", serviceName);
		document.appendChild(element);
		
		element = document.createElement("senderName", senderName);
		document.appendChild(element);

		element = document.createElement("bindType", bindType);
		document.appendChild(element);

		element = document.createElement("username", username);
		document.appendChild(element);

		element = document.createElement("password", password);
		document.appendChild(element);

		return document;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getBindType() {
		return bindType;
	}

	public String getsenderName() {
		return senderName;
	}

	public void setBindType(String bindType) {
		this.bindType = bindType;
	}

}

