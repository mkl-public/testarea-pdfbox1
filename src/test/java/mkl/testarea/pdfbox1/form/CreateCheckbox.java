package mkl.testarea.pdfbox1.form;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateCheckbox
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43604973/creating-a-checkbox-and-printing-it-to-pdf-file-is-not-working-using-pdfbox-1-8">
     * Creating a checkbox and printing it to pdf file is not working using pdfbox 1.8.9 api
     * </a>
     * <p>
     * This test executes the OP's method for checkbox creation. As it turns out
     * it has some errors: It treats a checkbox field as a text field for which
     * a PDF reader should create an appearance based on the default appearance
     * (DA) value of the field in particular if NeedAppearances is true.
     * Checkboxes are different, though: one does have to supply an appearance
     * stream at least for the on state.
     * </p>
     * 
     * @see #writeInputFieldToPDFPage(PDPage, PDDocument, Float, Float, Boolean)
     */
    @Test
    public void testCheckboxLikeSureshGoud() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        
        writeInputFieldToPDFPage(page, document, 100f, 500f, true);
        
        document.save(new File(RESULT_FOLDER, "CheckboxLikeSureshGoud.pdf"));
    }

    /**
     * <a href="http://stackoverflow.com/questions/43604973/creating-a-checkbox-and-printing-it-to-pdf-file-is-not-working-using-pdfbox-1-8">
     * Creating a checkbox and printing it to pdf file is not working using pdfbox 1.8.9 api
     * </a>
     * <p>
     * The OP's method for checkbox creation.
     * </p>
     * @see #testCheckboxLikeSureshGoud()
     */
    public static void writeInputFieldToPDFPage( PDPage pdPage, PDDocument document, Float x, Float y, Boolean ticked) throws IOException {
        PDFont font = PDType1Font.HELVETICA;
        PDResources res = new PDResources();
        String fontName = res.addFont(font);
        String da = ticked?"/" + fontName + " 10 Tf 0 0.4 0 rg":"";

        COSDictionary acroFormDict = new COSDictionary();
        acroFormDict.setBoolean(COSName.getPDFName("NeedAppearances"), true);
        acroFormDict.setItem(COSName.FIELDS, new COSArray());
        acroFormDict.setItem(COSName.DA, new COSString(da));

        PDAcroForm acroForm =  new PDAcroForm(document, acroFormDict);
        acroForm.setDefaultResources(res);
        document.getDocumentCatalog().setAcroForm(acroForm);

        PDGamma colourBlack = new PDGamma();
        PDAppearanceCharacteristicsDictionary fieldAppearance =
            new PDAppearanceCharacteristicsDictionary(new COSDictionary());
        fieldAppearance.setBorderColour(colourBlack);
        if(ticked) {
            COSArray arr = new COSArray();
            arr.add(new COSFloat(0.89f));
            arr.add(new COSFloat(0.937f));
            arr.add(new COSFloat(1f));
            fieldAppearance.setBackground(new PDGamma(arr));
        }

        COSDictionary cosDict = new COSDictionary();
        COSArray rect = new COSArray();

        rect.add(new COSFloat(x));
        rect.add(new COSFloat(new Float(y-5)));
        rect.add(new COSFloat(new Float(x+10)));
        rect.add(new COSFloat(new Float(y+5)));

        cosDict.setItem(COSName.RECT, rect);
        cosDict.setItem(COSName.FT, COSName.getPDFName("Btn")); // Field Type
        cosDict.setItem(COSName.TYPE, COSName.ANNOT);
        cosDict.setItem(COSName.SUBTYPE, COSName.getPDFName("Widget"));
        if(ticked) {
            cosDict.setItem(COSName.TU, new COSString("Checkbox with PDFBox"));
        }
        cosDict.setItem(COSName.T, new COSString("Chk"));
        //Tick mark color and size of the mark
        cosDict.setItem(COSName.DA, new COSString(ticked?"/F0 10 Tf 0 0.4 0 rg":"/FF 1 Tf 0 0 g"));
        cosDict.setInt(COSName.F, 4);

        PDCheckbox checkbox = new PDCheckbox(acroForm, cosDict);
        checkbox.setFieldFlags(PDCheckbox.FLAG_READ_ONLY);
        checkbox.setValue("Yes");

        checkbox.getWidget().setAppearanceCharacteristics(fieldAppearance);

        pdPage.getAnnotations().add(checkbox.getWidget());
        acroForm.getFields().add(checkbox);
    }
}
