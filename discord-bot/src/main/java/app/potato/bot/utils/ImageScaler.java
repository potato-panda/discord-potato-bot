package app.potato.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public
class ImageScaler {
    private static final Logger logger
            = LoggerFactory.getLogger( ImageCompressor.class );
    private final        byte[] sourceImageData;
    private final        String fileExtension;
    private              byte[] scaledImageData;

    public
    ImageScaler( byte[] sourceImageData,
                 String fileExtension )
    {
        this.sourceImageData = sourceImageData;
        this.fileExtension   = fileExtension;
    }

    public
    byte[] getScaled() throws IOException {
        if ( scaledImageData == null ) {
//            long methodStart = System.nanoTime();
            ByteArrayInputStream imageDataStream
                    = new ByteArrayInputStream( sourceImageData );

            BufferedImage image = ImageIO.read( imageDataStream );

            int height = image.getHeight();
            int width  = image.getWidth();

            float scale = 0.500f;

            int targetHeight = (int) ( height * scale );
            int targetWidth  = (int) ( width * scale );

            Image scaledImage = image.getScaledInstance( targetWidth,
                                                         targetHeight,
                                                         Image.SCALE_FAST );

            BufferedImage bufferedImage = new BufferedImage( targetWidth,
                                                             targetHeight,
                                                             BufferedImage.TYPE_INT_RGB );

            bufferedImage.getGraphics()
                         .drawImage( scaledImage,
                                     0,
                                     0,
                                     null );

            ByteArrayOutputStream scaledImageOutputStream
                    = new ByteArrayOutputStream();

            try {
                ImageIO.write( bufferedImage,
                               fileExtension,
                               scaledImageOutputStream );
            }
            catch ( IOException e ) {
                logger.info( e.getMessage() );
            }

//            long methodEnd = System.nanoTime();
//            logger.info( "Time to Scale Image: {}ms",
//                         ( methodEnd - methodStart ) / 1_000_000 );
            scaledImageData = scaledImageOutputStream.toByteArray();
        }

        return scaledImageData;
    }
}
