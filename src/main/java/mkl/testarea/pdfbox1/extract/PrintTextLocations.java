package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 * <a href="http://stackoverflow.com/questions/32203705/extract-text-word-by-word-from-pdf-file-using-pdfbox">
 * Extract text word by word from .pdf file using pdfbox
 * </a>
 * <p>
 * This essentially is the text extraction class used by the OP.
 * </p>
 * 
 * @author nayomi (http://stackoverflow.com/users/4018649/nayomi)
 */
public class PrintTextLocations extends PDFTextStripper
{
    public static StringBuilder tWord = new StringBuilder();
    public static String[][] coordTab;
    public static int p = 0;
    public static String seek;
    public static String[] seekA;
    public static List<String> wordList = new ArrayList<String>();
    public static boolean is1stChar = true;
    public static boolean lineMatch;
    public static int pageNo = 1;
    public static double lastYVal;

    public PrintTextLocations() throws IOException
    {
        super.setSortByPosition(true);
    }

    public static void main(String[] args) throws Exception
    {
        PDDocument document = null;
//        PDFTextParser pdftext = new PDFTextParser();
        String file_name = "d:/test.pdf";
//        seekA = pdftext.pdftoText(file_name).split(" ");
//        seek = pdftext.pdftoText(file_name);
        coordTab = new String[seekA.length * 2][6];
        try
        {
            File input = new File(file_name);
            document = PDDocument.load(input);
            if (document.isEncrypted())
            {
                document.decrypt("");
            }
            PrintTextLocations printer = new PrintTextLocations();
            List allPages = document.getDocumentCatalog().getAllPages();

            for (int i = 0; i < allPages.size(); i++)
            {
                PDPage page = (PDPage) allPages.get(i);
                PDStream contents = page.getContents();
                if (contents != null)
                {
                    printer.processStream(page, page.findResources(), page.getContents().getStream());
                }
                pageNo += 1;
            }
        }
        finally
        {
            if (document != null)
            {

                for (int k = 0; k <= p; k++)
                {
                    System.out.println(k + " : " + coordTab[k][0] + " | " + coordTab[k][1] + " | " + coordTab[k][2] + " | " + coordTab[k][3] + " | "
                            + coordTab[k][4] + " | " + coordTab[k][5]);

                }

//                myxls.close();
                document.close();
            }
        }
    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        String tChar = text.getCharacter();
//        String REGEX = "'' ";
        char c = tChar.charAt(0);
        lineMatch = matchCharLine(text);
        if (!Character.isWhitespace(c))
        {
            if ((!is1stChar) && (lineMatch == true))
            {
                appendChar(tChar);
            }
            else if (is1stChar == true)
            {
                setWordCoord(text, tChar);
            }
        }
        else
        {
            endWord();
        }
    }

    protected void appendChar(String tChar)
    {
        tWord.append(tChar);
        coordTab[p][3] = String.valueOf(tWord);
        is1stChar = false;
    }

    protected void setWordCoord(TextPosition text, String tChar)
    {
        tWord.append(tChar);

        coordTab[p][0] = "" + pageNo;
        coordTab[p][1] = "" + roundVal(Float.valueOf(text.getX()));
        coordTab[p][2] = "" + roundVal(Float.valueOf(text.getY()));
        coordTab[p][3] = String.valueOf(tWord);
        coordTab[p][4] = "" + text.getFontSize();
        coordTab[p][5] = "" + text.getFont().getBaseFont();

        is1stChar = false;
    }

    protected void endWord()
    {
        String newWord = tWord.toString().replaceAll("[^\\x00-\\x7F]", "");
        String sWord = newWord.substring(newWord.lastIndexOf(' ') + 1);
        if (!"".equals(sWord))
        {
            if (Arrays.asList(seekA).contains(sWord))
            {
                wordList.add(newWord);
            }
            else
            {
                wordList.add(newWord);
            }
        }
        tWord.delete(0, tWord.length());
        is1stChar = true;
        p++;
    }

    protected boolean matchCharLine(TextPosition text)
    {
        Double yVal = roundVal(Float.valueOf(text.getY()));
        if (yVal.doubleValue() == lastYVal)
        {
            return true;
        }
        lastYVal = yVal.doubleValue();
        endWord();
        return false;
    }

    protected Double roundVal(Float yVal)
    {
        DecimalFormat rounded = new DecimalFormat("###.##");
        String st = rounded.format(yVal);
        Double yValDub = Double.parseDouble(st.replace(",", "."));
        return yValDub;
    }
}