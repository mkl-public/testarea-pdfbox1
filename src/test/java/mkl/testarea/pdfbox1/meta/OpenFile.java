package mkl.testarea.pdfbox1.meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.Test;

/**
 * @author mkl
 */
public class OpenFile
{
    /**
     * <a href="http://stackoverflow.com/questions/44042244/pdf-failing-to-be-loaded-because-it-cant-be-read-by-pddocument">
     * PDF failing to be loaded because it can't be read by PDDocument
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B57cf1nqGbC4SEYtemxwQjFQNkU">
     * CG_PARTANCE_701018_0415.pdf
     * </a>
     * <p>
     * The PDF could properly be loaded. The code then choked on <code>acroForm.getFields()</code>
     * which threw a {@link NullPointerException}, and it did so for the simple reason that the PDF
     * contains no form definition and, therefore, acroForm was <code>null</code>.
     * </p>
     */
    @Test
    public void testOpenCG_PARTANCE_701018_0415() throws IOException
    {
        try (   InputStream file = getClass().getResourceAsStream("CG_PARTANCE_701018_0415.pdf")    )
        {
            PDDocument pdfTemplate = PDDocument.load(file);
            PDDocumentCatalog docCatalog = pdfTemplate.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            // Get field names
            List<PDField> fieldList = acroForm.getFields();
            List<PDPage> pages = pdfTemplate.getDocumentCatalog().getAllPages();

            //process.processPage(company, templateFile, pdfTemplate, acroForm, fieldList, pages, args);

            //COSDictionary acroFormDict = acroForm.getDictionary();
            //((COSArray) acroFormDict.getDictionaryObject("Fields")).clear();
        }
    }
}
