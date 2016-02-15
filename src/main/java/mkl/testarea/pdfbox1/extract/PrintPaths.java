package mkl.testarea.pdfbox1.extract;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.TextPosition;

/**
 * A simple class that attempts to print out path drawing operations.
 * 
 * @author mkl
 */
public class PrintPaths extends PageDrawer
{
    //
    // constructor
    //
    public PrintPaths() throws IOException
    {
        super();
    }

    //
    // method overrides for mere path observation
    //
    // ignore text
    @Override
    protected void processTextPosition(TextPosition text) { }

    // ignore bitmaps
    @Override
    public void drawImage(Image awtImage, AffineTransform at) { }

    // ignore shadings
    @Override
    public void shFill(COSName shadingName) throws IOException { }

    @Override
    public void processStream(PDPage aPage, PDResources resources, COSStream cosStream) throws IOException
    {
        PDRectangle cropBox = aPage.getCropBox();
        this.pageSize = cropBox.createDimension();
        super.processStream(aPage, resources, cosStream);
    }

    @Override
    public void fillPath(int windingRule) throws IOException
    {
        System.out.println("Fill; windingrule: " + windingRule);
        printPath();
        getLinePath().reset();
    }

    @Override
    public void strokePath() throws IOException
    {
        System.out.println("Stroke");
        printPath();
        getLinePath().reset();
    }

    void printPath()
    {
        GeneralPath path = getLinePath();
        PathIterator pathIterator = path.getPathIterator(null);

        double coords[] = new double[6];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                System.out.printf("Move to (%s %s)\n", coords[0], fixY(coords[1]));
                break;
            case PathIterator.SEG_LINETO:
                System.out.printf("Line to (%s %s)\n", coords[0], fixY(coords[1]));
                break;
            case PathIterator.SEG_QUADTO:
                System.out.printf("Quad along (%s %s) and (%s %s)\n", coords[0], fixY(coords[1]), coords[2], fixY(coords[3]));
                break;
            case PathIterator.SEG_CUBICTO:
                System.out.printf("Cubic along (%s %s), (%s %s), and (%s %s)\n", coords[0], fixY(coords[1]), coords[2], fixY(coords[3]), coords[4], fixY(coords[5]));
                break;
            case PathIterator.SEG_CLOSE:
                System.out.println("Close path");
            }
            pathIterator.next();
        }
    }
}
