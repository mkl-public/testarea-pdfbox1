package mkl.testarea.pdfbox1.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FillForm {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54820224/why-is-pdfbox-overwriting-multiple-fields-even-when-they-dont-match-the-fullyq">
     * Why is PDFBox overwriting multiple Fields, even when they don't match the fullyQualifiedName? (Kotlin Android)
     * </a>
     * <br/>
     * <a href="http://www.kylevp.com/csOnePage.pdf">
     * csOnePage.pdf
     * </a>
     * <p>
     * The problem is due to some fields of the form sharing empty
     * appearance XObjects and PDFBox assuming in case of existing
     * appearance XObjects that it can simply update this existing
     * appearance instead of having to create a new one from scratch.
     * </p>
     * <p>
     * A work-around is to remove existing appearances from a field
     * before setting its value, see below.
     * </p>
     */
    @Test
    public void testLikeKyle() throws IOException, COSVisitorException {
        try (   InputStream originalStream = getClass().getResourceAsStream("csOnePage.pdf") )
        {
            PDDocument doc = PDDocument.loadNonSeq(originalStream, null);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            List<String> skillList = Arrays.asList("Athletics","Acrobatics","Sleight of Hand", "Stealth","Acrana", "History","Investigation","Nature", "Religion", "Animal Handling", "Insight", "Medicine", "Perception", "Survival", "Deception", "Intimidation", "Performance", "Persuasion");

            int temp = 0;
            for (String skill : skillList) {
                PDField field = acroForm.getField(skill);
                temp += 1;
                if (field == null) {
                    System.err.printf("(%d) field '%s' is null.\n", temp, skill);
                } else {
                    field.getDictionary().removeItem(COSName.AP);
                    field.setValue(String.valueOf(temp));
                }
            }

            doc.save(new File(RESULT_FOLDER, "csOnePage-filled.pdf"));
            doc.close();
        }
    }

}
