package com.rbc.payments.wires.iso20022;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Namespace-agnostic XPath helper for asserting on ISO 20022 output. Use local-name paths like {@code /Document/X/Y}. */
public final class XmlAssert {

    private final Document doc;
    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private XmlAssert(Document doc) {
        this.doc = doc;
    }

    public static XmlAssert parse(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return new XmlAssert(f.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new AssertionError("Not well-formed XML", e);
        }
    }

    /** Text of the first node matching a local-name path, or {@code null} when absent. */
    public String text(String path) {
        List<String> all = texts(path);
        return all.isEmpty() ? null : all.get(0);
    }

    public List<String> texts(String path) {
        try {
            NodeList nodes = (NodeList) xpath.evaluate(toLocalNames(path), doc, XPathConstants.NODESET);
            List<String> out = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                out.add(nodes.item(i).getTextContent());
            }
            return out;
        } catch (Exception e) {
            throw new AssertionError("Bad path " + path, e);
        }
    }

    public String namespace() {
        return doc.getDocumentElement().getNamespaceURI();
    }

    static String toLocalNames(String path) {
        StringBuilder sb = new StringBuilder();
        for (String step : path.split("/")) {
            if (step.isEmpty()) {
                continue;
            }
            if (step.startsWith("@")) {
                sb.append('/').append(step);
            } else {
                sb.append("/*[local-name()='").append(step).append("']");
            }
        }
        return sb.toString();
    }
}
