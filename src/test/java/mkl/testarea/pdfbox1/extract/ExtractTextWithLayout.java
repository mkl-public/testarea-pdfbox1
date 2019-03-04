package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractTextWithLayout {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54956720/how-to-replace-a-space-with-a-word-while-extract-the-data-from-pdf-using-pdfbox">
     * How to replace a space with a word while extract the data from PDF using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=10ZkdPlGWzMJeahwnQPzE6V7s09d1nvwq">
     * test.pdf
     * </a> as "testWPhromma.pdf"
     * <p>
     * The {@link LayoutTextStripper} can be used out of the box here.
     * </p>
     */
    @Test
    public void testExtractTestWPhromma() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testWPhromma.pdf")) {
            PDDocument document = PDDocument.load(resource);
            LayoutTextStripper stripper = new LayoutTextStripper();
            String text = stripper.getText(document);
            System.out.printf("testWPhromma.pdf\n------\n%s------\n", text);
        }
    }

}
