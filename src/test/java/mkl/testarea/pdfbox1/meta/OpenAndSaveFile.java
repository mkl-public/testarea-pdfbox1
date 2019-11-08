package mkl.testarea.pdfbox1.meta;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class OpenAndSaveFile {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58761272/pdf-is-getting-change-after-loading-using-pdfbox-jar">
     * PDF is getting change after loading using PDFBOX jar
     * </a>
     * <br/>
     * <a href="https://gofile.io/?c=lLPpQz">
     * AsposeOutput_temp.pdf
     * </a>
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testOpenAndSaveAsposeOutputTemp() throws IOException, COSVisitorException {
        File originalPdfFile = new File("src/test/resources/mkl/testarea/pdfbox1/meta/AsposeOutput_temp.pdf");
        PDDocument originalDocument = PDDocument.load(originalPdfFile);
        originalDocument.save(new File(RESULT_FOLDER, "pdfBoxGen.pdf"));
    }

}
