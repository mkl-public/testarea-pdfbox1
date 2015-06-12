package mkl.testarea.pdfbox1.content;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

/**
 * <a href="http://stackoverflow.com/questions/30767643/pdfbox-issue-while-changing-page">
 * PdfBox issue while changing page
 * </a>
 * <p>
 * The original PdfRenderingEndorsement class (which is the class where actually the data is painted)
 * with minute changes to achieve compilability.
 * </p>
 * @author Frakcool <http://stackoverflow.com/users/2180785/frakcool>
 */
public class PdfRenderingEndorsement {

    //private static final Logger LOGGER = Logger.getLogger(PdfRenderingEndorsement.class);

    private static final float BOTTOM_MARGIN = 60;

    private static final int DESC_WIDTH = 269; //For description fields

    private static final int FIELD_WIDTH = 70;

    private static final int FIELD_WIDTH2 = 60;

    private static final int FIELD_WIDTH3 = 60;

    private static final int FIELD1 = 112;

    private static final int VALUE1 = 112;

    private static final int FIELD2 = 80;

    private static final int VALUE2 = 80;

    private static final int VALUE_WIDTH = 80;

    private static final int VALUE_WIDTH2 = 60;

    private static final int VALUE_WIDTH3 = 80;

    private static final int HALF_WIDTH = 325;

    private static final int TEXT_WIDTH = 410; //For text fields

    //private final RwaConstants constants = ConstantsGetter.getInstance();

    private final PDDocument doc;

    private final String logoPath;

    private final String[] header;

    private int count = 0;

    private boolean newPage;

    private PDPageContentStream content;

    /**
     * Empty constructor. Used only to initialize the rendering class and call
     * it's methods.
     */
    public PdfRenderingEndorsement(PDDocument doc, String logoPath, 
                   String[] header) {
    this.doc = doc;
    this.logoPath = logoPath;
    this.header = header;
    }

    public float checkContentStream2(float y, int lines, int space) 
    throws Exception {
    float newY = checkYCoord2(y, lines, space);
    if (newY == 700) {
        if (content != null) {
        content.close();
        }

        File file = new File(logoPath);
        PDJpeg logoImg = new PDJpeg(doc, new FileInputStream(file));
        PDPage page = new PDPage(PDPage.PAGE_SIZE_LETTER);
        doc.addPage(page);
        content = new PDPageContentStream(doc, page);
        content.drawImage(logoImg, 50, 720);
        rHeader();
    }
    return newY;
    }

    private float checkYCoord2(float y, int lines, int space) {
    float newY = y;
    for (int i = 0; i < lines; i++) {
        if ((newY - space) <= BOTTOM_MARGIN) {
        newY = 700f;
        return newY;
        } else {
        newY = newY - space;
        }
    }
    return y;
    }

    public boolean getNewPage() {
    return newPage;
    }

    public float checkContentStream(float y) throws Exception {
    float newY = checkYCoord(y, 1, 10);
    if (newY == 700) {
        if (content != null) {
        content.close();
        }
        File file = new File(logoPath);
        PDJpeg logoImg = new PDJpeg(doc, new FileInputStream(file));
        PDPage page = new PDPage(PDPage.PAGE_SIZE_LETTER);
        doc.addPage(page);
        content = new PDPageContentStream(doc, page);
        content.drawImage(logoImg, 50, 720);
        rHeader();
    }
    return newY;
    }

    public float checkYCoord(float y, int lines, int space) {
    float newY = y;
    for (int i = 0; i < lines; i++) {
        if ((newY - space) <= BOTTOM_MARGIN) {
        newY = 700f;
        return newY;
        } else {
        newY = newY - space;
        }
    }
    return y;
    }

    public float checkContentStream(float y, int lines, int space) 
    throws Exception {
    float newY = checkYCoord(y, lines, space);
    if (newY == 700) {
        if (content != null) {
        content.close();
        }
        File file = new File(logoPath);
        PDJpeg logoImg = new PDJpeg(doc, new FileInputStream(file));
        PDPage page = new PDPage(PDPage.PAGE_SIZE_LETTER);
        doc.addPage(page);
        content = new PDPageContentStream(doc, page);
        content.drawImage(logoImg, 50, 720);
        rHeader();
    }
    return newY;
    }

    public void closeContentStream() throws Exception {
    if (content != null) {
        content.close();
    }
    }

    /**
     * Renders the header for slip documents.
     */
    public void rHeader() throws Exception {
    float y = 760f;
    content.setLineWidth(.5f);
    content.setFont(PDType1Font.TIMES_ROMAN, 9);
    content.setNonStrokingColor(Color.GRAY);
    content.drawLine(50, 710, 562, 710);

    y = rText(150, y + 19, 10, "ENDOSO" /*constants.endorsement()*/, null,
          TEXT_WIDTH, 0);
    y = rText(150, y + 9, 10, header[0], null, TEXT_WIDTH, 0);
    y = rText(150, y + 9, 10, header[1], null, TEXT_WIDTH, 0);
    y = rText(150, y + 9, 10, header[2], null, TEXT_WIDTH, 0);
    y = rText(150, y + 9, 10, header[3], null, TEXT_WIDTH, 0);
    content.setNonStrokingColor(Color.BLACK);
    content.setFont(PDType1Font.TIMES_ROMAN, 9);
    }

    public float rText(float x, float y, int space, String labelField,
               String value) 
    throws Exception {
    return rText(x, y, space, labelField, value, FIELD_WIDTH, 
             HALF_WIDTH - 2 * FIELD_WIDTH - 10);    
    }

    public float rTextLR(float x, float y, int space, String labelField,
               String value) 
    throws Exception {
    return rText(x, y, space, labelField, value, 0, 
             HALF_WIDTH - 2 * FIELD_WIDTH - 10);    
    }

    public float rText(float x, float y, int space, String labelField,
               String value, int fieldWidth) 
    throws Exception {
    if (fieldWidth == 0) {
        return rText(x, y, space, labelField, value, FIELD_WIDTH2, 
             VALUE_WIDTH2);
    } else if (fieldWidth == 1) {
        return rText(x, y, space, labelField, value, FIELD_WIDTH3,
             VALUE_WIDTH3);
    } else if (fieldWidth == 2) {
        return rText(x, y, space, labelField, value, TEXT_WIDTH,
             TEXT_WIDTH);
    }
    return y;
    }


    public float getFieldSize(int fs) {
    switch(fs) {
    case 1:
        return (FIELD_WIDTH + VALUE_WIDTH);
    case 2:
        return (FIELD_WIDTH + DESC_WIDTH);
    case 3:
        return (FIELD_WIDTH + TEXT_WIDTH);
    case 4:
        return (HALF_WIDTH - FIELD_WIDTH);
    case 5:
        return (FIELD_WIDTH + TEXT_WIDTH);
    case 6:
        return (FIELD_WIDTH + TEXT_WIDTH);
    case 7:
        return (FIELD1 + 19) / 2;
    case 8:
        return (FIELD2 + 19) / 2;
    default:
        return 0;
    }
    }

    public void paintLinesH(float y) throws Exception {
    content.drawLine(49, y - 6, 327, y - 6);
    content.drawLine(335, y - 6, 563, y - 6);
    }

    public void paintLinesV(float x, float yMax, float yMin)
    throws Exception {
    content.drawLine(x - 1, yMax - 6, x - 1, yMin - 6);
    }

    public float rText(float x, float y, int space, String labelField,
               String value, int fieldWidth, int valueWidth) 
    throws Exception {
    PDFont font = PDType1Font.TIMES_BOLD;
    content.setFont(font, 9);
    float y1 = 0f;
    float y2 = 0f;
    if (value == null) {
        return rText(labelField, fieldWidth, x, y - 19, space, font, false);
    } else {
        if (labelField == null) {
        font = PDType1Font.TIMES_ROMAN;
        content.setFont(font, 9);
        return rText(value, valueWidth, x, y - 19, space, font, true);
        } else {
        y1 = rText(labelField, fieldWidth, x, y - 30, space, font, 
               false);
        font = PDType1Font.TIMES_ROMAN;
        content.setFont(font, 9);
        float y3 = y;
        y2 = rText(value, valueWidth, x + fieldWidth + 10, y - 30,
               space, font, true);
        if (y3 < y2) {
            return y2;
        } else {
            if (y1 >= y2) {
            return y2;
            } else {
            return y1;
            }
        }
        }
    }
    }

    private ArrayList<String> getRows(String text, int width, PDFont font)
    throws Exception {
    float textWidth = font.getStringWidth(text) / 1000f * 9f;
    ArrayList<String> result = new ArrayList<String>();// Lists.newArrayList();
    if (textWidth < width) {
        result.add(text);
        return result;
    }

    float spaceWidth = font.getStringWidth(" ") / 1000f * 9f;
    String[] paragraphs = text.split("\n|\r\n|\r");
    for (String paragraph : paragraphs) {
        float pWidth = font.getStringWidth(paragraph) / 1000f * 9f;
        if (pWidth < width) {
        result.add(paragraph);
        continue;
        }

        float widthCount = 0f;
        String[] words = paragraph.trim().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < words.length; j++) {
        if (words[j].trim().length() == 0) {
            continue;
        }

        float wWidth = font.getStringWidth(words[j]) / 1000f * 9f;
        float totalWidth = widthCount + wWidth + spaceWidth;
        if (totalWidth < width + spaceWidth) {
            sb.append(words[j]);
            sb.append(" ");
            widthCount = totalWidth;
        } else {
            result.add(sb.toString().trim());
            sb = new StringBuilder();
            sb.append(words[j]);
            sb.append(" ");
            widthCount = totalWidth - widthCount;
        }
        }
        result.add(sb.toString().trim());
    }
    return result;
    }

    private float rText(String text, int width, float x, float y, int space,
            PDFont font, boolean isValue) throws Exception {
    float newY = y;
    int rowHeight = 0;
    newPage = false;
    ArrayList<String> rowList = getRows(text, width, font);
    if (isValue) {
        for (String row : rowList) {
        if (rowHeight >= 10) {
            newY = checkContentStream(newY - 10);
            newY = newY == 700 ? 680 : newY;
            if (newY <= 700 && !newPage) {
            newPage = true;
            }
            rowHeight = newY == 680 ? 0 : rowHeight;
        }
        content.beginText();
        content.moveTextPositionByAmount(x, newY);
        content.drawString(row);
        content.endText();
        rowHeight = rowHeight + 10;
        }
    } else {
        for (String row : rowList) {
        content.beginText();
        content.moveTextPositionByAmount(x, newY - rowHeight);
        content.drawString(row);
        content.endText();
        rowHeight = rowHeight + 10;
        }
        newY -= (rowHeight - 10);
    }
    return newY;
    }
}