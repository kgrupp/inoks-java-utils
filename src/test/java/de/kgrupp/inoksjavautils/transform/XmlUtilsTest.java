package de.kgrupp.inoksjavautils.transform;

import de.kgrupp.inoksjavautils.exception.UnCheckedException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlUtilsTest {

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class MyClass {
        @XmlElement
        private int anInt;
        @XmlAttribute
        private double someDouble;
    }

    private static final MyClass EXAMPLE1 = new MyClass(1, 1.0);
    private static final String EXAMPLE1_XML = "<my someDouble=\"1.0\"><anInt>1</anInt></my>";
    private static final MyClass EXAMPLE2 = new MyClass(2, 2.0);
    private static final String EXAMPLE2_XML = "<my someDouble=\"2.0\"><anInt>2</anInt></my>";
    private static final String EXAMPLE_STRING = "<test>" + EXAMPLE1_XML + EXAMPLE2_XML + "</test>";

    @Test
    void unmarshalFirst() {
        Optional<MyClass> result = XmlUtils.unmarshalFirst("my", EXAMPLE_STRING, MyClass.class);
        assertTrue(result.isPresent());
        assertEquals(EXAMPLE1, result.get());
    }

    @Test
    void unmarshalFirstNotFound() {
        Optional<MyClass> result = XmlUtils.unmarshalFirst("missing", EXAMPLE_STRING, MyClass.class);
        assertFalse(result.isPresent());
    }

    @Test
    void unmarshalFirstInvalid() {
        assertThrows(UnCheckedException.class, () -> XmlUtils.unmarshalFirst("my", EXAMPLE_STRING + "INVALID", MyClass.class));
    }

    @Test
    void unmarshalFirstInvalidType() {
        assertThrows(UnCheckedException.class, () -> XmlUtils.unmarshalFirst("my", EXAMPLE_STRING.replace("1.0", "TEST"), MyClass.class));
    }

    @Test
    void createEmptyDocument() {
        final Document emptyDocument = XmlUtils.createEmptyDocument();
        assertEquals(0, emptyDocument.getChildNodes().getLength());
    }

    @Test
    void unmarshalEach() {
        final List<MyClass> result = XmlUtils.unmarshalEach("my", EXAMPLE_STRING, MyClass.class);
        assertEquals(2, result.size());
        assertEquals(EXAMPLE1, result.get(0));
        assertEquals(EXAMPLE2, result.get(1));
    }

    @Test
    void toIterable() {
        final Document document = XmlUtils.parse(IOUtils.stringToInputStream(EXAMPLE_STRING));
        final Iterable<Node> nodes = XmlUtils.toIterable(document.getElementsByTagName("my"));
        final List<Node> list = StreamSupport.stream(nodes.spliterator(), false).collect(Collectors.toList());
        assertEquals(2, list.size());
        assertEquals("1", list.get(0).getTextContent());
        assertEquals("2", list.get(1).getTextContent());
    }

    @Test
    void toIterableBreakIterator() {
        final Document document = XmlUtils.parse(IOUtils.stringToInputStream(EXAMPLE_STRING));
        final Iterable<Node> nodes = XmlUtils.toIterable(document.getElementsByTagName("my"));

        final Iterator<Node> iterator = nodes.iterator();
        iterator.next();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}