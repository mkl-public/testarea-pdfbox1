package mkl.testarea.pdfbox1.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.examples.util.PrintImageLocations;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * <a href="http://stackoverflow.com/questions/31009949/replacing-images-with-same-resource-in-pdfbox">
 * Replacing images with same resource in PDFBox
 * </a>
 * <p>
 * Helper class extracting image positions. This class is based on the PDFBox sample
 * {@link PrintImageLocations}.
 * </p>
 * 
 * @author mkl
 */
public class ImageLocator extends PDFStreamEngine
{
    private static final String INVOKE_OPERATOR = "Do";

    public ImageLocator() throws IOException
    {
        super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PDFTextStripper.properties", true));
    }

    public List<ImageLocation> getLocations()
    {
        return new ArrayList<ImageLocation>(locations);
    }

    /**
     * This is used to handle an operation.
     *
     * @param operator
     *            The operation to perform.
     * @param arguments
     *            The list of arguments.
     *
     * @throws IOException
     *             If there is an error processing the operation.
     */
    protected void processOperator(PDFOperator operator, List<COSBase> arguments) throws IOException
    {
        String operation = operator.getOperation();
        if (INVOKE_OPERATOR.equals(operation))
        {
            COSName objectName = (COSName) arguments.get(0);
            Map<String, PDXObject> xobjects = getResources().getXObjects();
            PDXObject xobject = (PDXObject) xobjects.get(objectName.getName());
            if (xobject instanceof PDXObjectImage)
            {
                PDXObjectImage image = (PDXObjectImage) xobject;
                PDPage page = getCurrentPage();
                Matrix matrix = getGraphicsState().getCurrentTransformationMatrix();

                locations.add(new ImageLocation(page, matrix, image));
            }
            else if (xobject instanceof PDXObjectForm)
            {
                // save the graphics state
                getGraphicsStack().push((PDGraphicsState) getGraphicsState().clone());
                PDPage page = getCurrentPage();

                PDXObjectForm form = (PDXObjectForm) xobject;
                COSStream invoke = (COSStream) form.getCOSObject();
                PDResources pdResources = form.getResources();
                if (pdResources == null)
                {
                    pdResources = page.findResources();
                }
                // if there is an optional form matrix, we have to
                // map the form space to the user space
                Matrix matrix = form.getMatrix();
                if (matrix != null)
                {
                    Matrix xobjectCTM = matrix.multiply(getGraphicsState().getCurrentTransformationMatrix());
                    getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                processSubStream(page, pdResources, invoke);

                // restore the graphics state
                setGraphicsState((PDGraphicsState) getGraphicsStack().pop());
            }
        }
        else
        {
            super.processOperator(operator, arguments);
        }
    }

    public class ImageLocation
    {
        public ImageLocation(PDPage page, Matrix matrix, PDXObjectImage image)
        {
            this.page = page;
            this.matrix = matrix;
            this.image = image;
        }

        public PDPage getPage()
        {
            return page;
        }

        public Matrix getMatrix()
        {
            return matrix;
        }

        public PDXObjectImage getImage()
        {
            return image;
        }

        final PDPage page;
        final Matrix matrix;
        final PDXObjectImage image;
    }
    
    final List<ImageLocation> locations = new ArrayList<ImageLocation>();
}
