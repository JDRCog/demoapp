package com.rbc.payments.wires.iso20022;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/** Minimal fluent wrapper over StAX for writing ISO 20022 XML. Escaping is handled by StAX. */
public final class IsoXml {

    private final StringWriter out = new StringWriter();
    private final XMLStreamWriter w;

    private IsoXml() {
        try {
            w = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            w.writeStartDocument("UTF-8", "1.0");
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    public static IsoXml create() {
        return new IsoXml();
    }

    /** Opens an element that declares {@code namespace} as its default namespace. */
    public IsoXml startRoot(String name, String namespace) {
        return run(() -> {
            w.writeStartElement("", name, namespace);
            w.writeDefaultNamespace(namespace);
        });
    }

    public IsoXml start(String name) {
        return run(() -> w.writeStartElement(name));
    }

    public IsoXml end() {
        return run(w::writeEndElement);
    }

    public IsoXml leaf(String name, String text) {
        return run(() -> {
            w.writeStartElement(name);
            w.writeCharacters(text);
            w.writeEndElement();
        });
    }

    public IsoXml leafIfPresent(String name, String text) {
        return text == null || text.isBlank() ? this : leaf(name, text);
    }

    public IsoXml leaf(String name, String text, String attribute, String attributeValue) {
        return run(() -> {
            w.writeStartElement(name);
            w.writeAttribute(attribute, attributeValue);
            w.writeCharacters(text);
            w.writeEndElement();
        });
    }

    public String toXml() {
        run(w::writeEndDocument);
        run(w::close);
        return out.toString();
    }

    private interface XmlAction {
        void run() throws XMLStreamException;
    }

    private IsoXml run(XmlAction action) {
        try {
            action.run();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to write ISO 20022 XML", e);
        }
        return this;
    }
}
