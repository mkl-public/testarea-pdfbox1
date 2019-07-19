package mkl.testarea.pdfbox1.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author mkl
 */
public class CheckImageFieldFilled {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/56959790/how-do-i-find-which-image-field-in-pdf-has-image-inserted-and-which-one-has-no-i">
     * How do I find which image field in PDF has image inserted and which one has no images attached using PDFbox 1.8.11?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/g2wqm8ipsp8t8l5/GSA%20500%20PDF_v4.pdf?dl=0">
     * GSA 500 PDF_v4.pdf
     * </a>
     * <p>
     * This test shows how to check in the XFA XML whether a given image
     * field is set. 
     * </p>
     * @see #isFieldFilledXfa(Document, String)
     */
    @Test
    public void testCheckXfaGsa500Pdf_v4() throws IOException, ParserConfigurationException, SAXException {
        try (   InputStream resource = getClass().getResourceAsStream("GSA 500 PDF_v4.pdf");
                PDDocument document = PDDocument.load(resource);    ) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            Document xfaDom = acroForm.getXFA().getDocument();

            System.out.println("Filled image fields from ImageField1..ImageField105:");
            for (int i=1; i < 106; i++) {
                if (isFieldFilledXfa(xfaDom, "ImageField" + i)) {
                    System.out.printf("* ImageField%d\n", i);
                }
            }
        }
    }

    /** @see #testCheckXfaGsa500Pdf_v4() */
    boolean isFieldFilledXfa(Document xfaDom, String fieldName) {
        NodeList fieldElements = xfaDom.getElementsByTagName(fieldName);
        for (int i = 0; i < fieldElements.getLength(); i++) {
            Node node = fieldElements.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (element.getAttribute("xfa:contentType").startsWith("image/")) {
                    return element.getTextContent().length() > 0;
                }
            }
        }
        return false;
    }
}
