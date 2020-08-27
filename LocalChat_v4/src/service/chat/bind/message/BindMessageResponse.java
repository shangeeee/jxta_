package service.chat.bind.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;
import service.chat.bind.BindQueryHandler;

public class BindMessageResponse extends ResolverResponse {
	private static final String documentRoot = ResolverResponse.getAdvertisementType();

	private String serviceName;
	private PipeID pipeID;
	private String pipeType;
	private String pipeName;
	private String pipeDesc;
	private String srcPeerID;
	private String srcPeerName;
	private String bindType;

	public BindMessageResponse(String serviceName, PipeAdvertisement pipeAdv, String bindType) {
		super();
		this.serviceName = serviceName;
		this.pipeID = (PipeID) pipeAdv.getPipeID();
		pipeType = pipeAdv.getType();
		pipeName = pipeAdv.getName();
		pipeDesc = pipeAdv.getDescription();
		if (!bindType.equals(BindMessageQuery.propagate) && !bindType.equals(BindMessageQuery.unicast)) {
			throw new IllegalArgumentException(
					"   The bind type must be " + BindMessageQuery.propagate + " or " + BindMessageQuery.unicast);
		}
		this.bindType = bindType;

		setHandlerName(BindQueryHandler.DEFAULT_HANDLER_NAME);
		setResponse(toString());
	}

	public BindMessageResponse(InputStream stream) throws IOException, URISyntaxException {
		StructuredTextDocument document = (StructuredTextDocument)StructuredDocumentFactory
				.newStructuredDocument(new MimeMediaType("text/xml"), stream);
		Enumeration<?> elements = document.getChildren();
		while(elements.hasMoreElements()) {
			TextElement element = (TextElement) elements.nextElement();
			if (element.getName().equals("serviceName")) {
				serviceName = String.valueOf(element.getTextValue());
				continue;
			}
			if(element.getName().equals("srcPeerID")) {
				srcPeerID = String.valueOf(element.getTextValue());
				continue;
			}

			if (element.getName().equals("srcPeerName")) {
				srcPeerName = String.valueOf(element.getTextValue());
				continue;
			}
			if (element.getName().equals("pipeDesc")) {
				pipeDesc = String.valueOf(element.getTextValue());
				continue;
			}
			if (element.getName().equals("pipeName")) {
				pipeName = String.valueOf(element.getTextValue());
				continue;
			}
			if (element.getName().equals("pipeType")) {
				pipeType = String.valueOf(element.getTextValue());
				continue;
			}
			if (element.getName().equals("pipeID")) {
				pipeID = (PipeID) IDFactory.fromURI(new URI(String.valueOf(element.getTextValue())));
				continue;
			}
			if (element.getName().equals("bindType")) {
				bindType = String.valueOf(element.getTextValue());
				continue;
			}
			

		}
	}

	public Document getDocument(MimeMediaType asmimeMedia) {
		StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory
				.newStructuredDocument(asmimeMedia, documentRoot);
		TextElement element;

		element = document.createElement("serviceName", String.valueOf(serviceName));
		document.appendChild(element);

		element = document.createElement("srcPeerID", String.valueOf(srcPeerID));
		document.appendChild(element);

		element = document.createElement("srcPeerName", String.valueOf(srcPeerName));
		document.appendChild(element);

		element = document.createElement("bindType", String.valueOf(bindType));
		document.appendChild(element);

		element = document.createElement("pipeID", pipeID.toString());
		document.appendChild(element);

		element = document.createElement("pipeType", pipeType);
		document.appendChild(element);

		element = document.createElement("pipeName", pipeName);
		document.appendChild(element);

		element = document.createElement("pipeDesc", pipeDesc);
		document.appendChild(element);

		return document;
	}

	@Override
	public String toString() {
		StringWriter out = new StringWriter();
		StructuredTextDocument<?> doc = (StructuredTextDocument<?>) getDocument(new MimeMediaType("text/xml"));
		try {
			doc.sendToWriter(out);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return out.toString();
	}

	public void setSrcPeerID(String srcPeerID) {
		this.srcPeerID = srcPeerID;
	}

	public String getSrcPeerID() {
		return srcPeerID;
	}

	public String getSrcPeerName() {
		return srcPeerName;
	}

	public void setSrcPeerName(String peerName) {
		this.srcPeerName = peerName;
	}

	public Advertisement getPipeAdv() {
		PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		pipeAdv.setPipeID(pipeID);
		pipeAdv.setType(pipeType);
		pipeAdv.setName(pipeName);
		pipeAdv.setDescription(pipeDesc);

		return pipeAdv;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getBindType() {
		return bindType;
	}

}
