package mkl.testarea.pdfbox1.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/29371129/java-pdfbox-fill-out-pdf-form-append-it-to-pddocument-and-repeat">
 * Java pdfBox: Fill out pdf form, append it to pddocument, and repeat
 * </a>
 * 
 * There are two major issues: No AcroForm generation and identical field names.
 * {@link #test()} fixes this.
 * 
 * @author mkl
 */
public class AppendFormTwice
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void test() throws COSVisitorException, IOException
    {
        byte[] template = generateSimpleTemplate();
        Files.write(new File(RESULT_FOLDER,  "template.pdf").toPath(), template);

        try (   PDDocument finalDoc = new PDDocument(); )
        {
            List<PDField> fields = new ArrayList<PDField>();
            int i = 0;

            for (String value : new String[]{"eins", "zwei"})
            {
                PDDocument doc = new PDDocument().load(new ByteArrayInputStream(template));
                PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
                PDAcroForm acroForm = docCatalog.getAcroForm();
                PDField field = acroForm.getField("SampleField");
                field.setValue(value);
                field.setPartialName("SampleField" + i++);
                List<PDPage> pages = docCatalog.getAllPages();
                finalDoc.addPage(pages.get(0));
                fields.add(field);
            }

            PDAcroForm finalForm = new PDAcroForm(finalDoc);
            finalDoc.getDocumentCatalog().setAcroForm(finalForm);
            finalForm.setFields(fields);

            finalDoc.save(new File(RESULT_FOLDER, "form-two-templates.pdf"));
        }
    }

    /**
     * Generates a sample PDF template with one form field with the name "SampleField"
     */
    byte[] generateSimpleTemplate() throws IOException, COSVisitorException
    {
        try (   PDDocument template = new PDDocument();
                InputStream fontStream = getClass().getResourceAsStream("LiberationSans-Regular.ttf");
                ByteArrayOutputStream resultStream = new ByteArrayOutputStream()    )
        {
            PDPage page = new PDPage();
            template.addPage(page);
            
            PDTrueTypeFont font = PDTrueTypeFont.loadTTF(template, fontStream);
            
            // add a new AcroForm and add that to the document
            PDAcroForm acroForm = new PDAcroForm(template);
            template.getDocumentCatalog().setAcroForm(acroForm);
            
            // Add and set the resources and default appearance
            PDResources res = new PDResources();
            String fontName = res.addFont(font);
            acroForm.setDefaultResources(res);
            String da = "/" + fontName + " 12 Tf 0 g";
            //acroForm.setDefaultAppearance(da);

            COSDictionary cosDict = new COSDictionary();

            COSArray rect = new COSArray();
            rect.add(new COSFloat(250f)); // lower x boundary
            rect.add(new COSFloat(700f)); // lower y boundary
            rect.add(new COSFloat(500f)); // upper x boundary
            rect.add(new COSFloat(750f)); // upper y boundary

            cosDict.setItem(COSName.RECT, rect);
            cosDict.setItem(COSName.FT, COSName.getPDFName("Tx")); // Field Type
            cosDict.setItem(COSName.TYPE, COSName.ANNOT);
            cosDict.setItem(COSName.SUBTYPE, COSName.getPDFName("Widget"));
            cosDict.setItem(COSName.DA, new COSString(da));

            // add a form field to the form
            PDTextbox textBox = new PDTextbox(acroForm, cosDict);
            textBox.setPartialName("SampleField");
            acroForm.getFields().add(textBox);
            
            // specify the annotation associated with the field
            // and add it to the page
            PDAnnotationWidget widget = textBox.getWidget();
            page.getAnnotations().add(widget);
            
            //textBox.setValue("Test");
            
            template.save(resultStream);
            return resultStream.toByteArray();
        }
        
        
    }
}
