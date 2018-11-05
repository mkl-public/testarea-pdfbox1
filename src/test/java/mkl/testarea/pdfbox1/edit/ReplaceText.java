package mkl.testarea.pdfbox1.edit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ReplaceText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "edit");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/53114578/why-squares-shown-instead-of-symbols-in-output-file-using-pdfbox">
     * Why squares shown instead of symbols in output file using pdfbox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/18cT0tTLWSpPdzubxXH5E8ZGTvN6vY3q-?usp=sharing">
     * doc_test_pdf_replace_input.pdf
     * </a>
     * <p>
     * The font in question contains widths only for the actually used glyphs.
     * Thus, the replacement glyphs which originally were not used all have a
     * width of 0 and consequentially are printed at the same origin.
     * </p>
     * @see #testReplaceLikeBorHunterDocTestPdfReplaceInput1()
     */
    @Test
    public void testReplaceLikeBorHunterDocTestPdfReplaceInput() throws IOException, COSVisitorException {
        try (   InputStream resource = getClass().getResourceAsStream("doc_test_pdf_replace_input.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "doc_test_pdf_replace_input-replaced.pdf"))  ) {
            ReplaceTextInPdf(resource, result);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53114578/why-squares-shown-instead-of-symbols-in-output-file-using-pdfbox">
     * Why squares shown instead of symbols in output file using pdfbox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/18cT0tTLWSpPdzubxXH5E8ZGTvN6vY3q-?usp=sharing">
     * doc_test_pdf_replace_input_1.pdf
     * </a>
     * <p>
     * The font in question contains widths only for the actually used glyphs.
     * Thus, the replacement glyphs which originally were not used all have a
     * width of 0 and consequentially are printed at the same origin.
     * </p>
     * @see #testReplaceLikeBorHunterDocTestPdfReplaceInput()
     */
    @Test
    public void testReplaceLikeBorHunterDocTestPdfReplaceInput1() throws IOException, COSVisitorException {
        try (   InputStream resource = getClass().getResourceAsStream("doc_test_pdf_replace_input_1.pdf ");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "doc_test_pdf_replace_input_1-replaced.pdf"))  ) {
            ReplaceTextInPdf(resource, result);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53114578/why-squares-shown-instead-of-symbols-in-output-file-using-pdfbox">
     * Why squares shown instead of symbols in output file using pdfbox
     * </a>
     * @see #testReplaceLikeBorHunterDocTestPdfReplaceInput()
     * @see #testReplaceLikeBorHunterDocTestPdfReplaceInput1()
     */
    @SuppressWarnings("rawtypes")
    void ReplaceTextInPdf(InputStream input, OutputStream output) throws IOException, COSVisitorException {
        PDDocument doc = null;
        try {
            doc = PDDocument.loadNonSeq(input, null);
            List pages = doc.getDocumentCatalog().getAllPages();

            for (int i = 0; i < pages.size(); i++) {
                PDPage page = (PDPage)pages.get(i);
                PDStream contents = page.getContents();
                PDFStreamParser parser = new PDFStreamParser(contents.getStream());
                parser.parse();
                List tokens = parser.getTokens();

                for (int j = 0; j < tokens.size(); j++) {
                    Object next = tokens.get(j);
                    if (next instanceof PDFOperator) {
                        PDFOperator op = (PDFOperator)next;
                        //Tj and TJ are the two operators that display
                        //strings in a PDF
                        if ("Tj".equals(op.getOperation())) {
                            //Tj takes one operator and that is the string
                            //to display so lets update that operator
                            COSString previous = (COSString)tokens.get(j - 1);
                            String tempString = previous.getString();

                            tempString = tempString.replace("@test", "123456");

                            previous.reset();
                            previous.append(tempString.getBytes());
                        } else if ("TJ".equals(op.getOperation())) {
                            String tempString = "";
                            COSString cosString = null;
                            COSArray previous = (COSArray)tokens.get(j - 1);
                            for (int k = 0; k < previous.size(); k++) {
                                Object arrElement = previous.getObject(k);
                                if (arrElement instanceof COSString) {
                                    cosString = (COSString)arrElement;
                                    tempString += cosString.getString();
                                    cosString.reset();
                                }
                            }

                            if (tempString != null && tempString.trim().length() > 0) {

                                tempString = tempString.replace("@test", "123456");

                                for (int k = 0; k < previous.size(); k++) {
                                    Object arrElement = previous.getObject(k);
                                    if (arrElement instanceof COSString) {
                                        cosString.reset();
                                        cosString.append(tempString.getBytes("ISO-8859-1"));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                //now that the tokens are updated we will replace the
                //page content stream.
                PDStream updatedStream = new PDStream(doc);
                OutputStream out1 = updatedStream.createOutputStream();
                ContentStreamWriter tokenWriter = new ContentStreamWriter(out1);
                tokenWriter.writeTokens(tokens);
                page.setContents(updatedStream);
            }

            doc.save(output);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }
}
