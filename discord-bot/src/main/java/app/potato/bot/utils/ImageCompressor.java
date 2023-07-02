package app.potato.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public
class ImageCompressor {

    private static final Logger logger
            = LoggerFactory.getLogger( ImageCompressor.class );
    private final        byte[] sourceImageData;
    private final        String fileExtension;
    private              byte[] compressedImageData;

    public
    ImageCompressor( byte[] sourceImageData,
                     String fileExtension )
    {
        this.sourceImageData = sourceImageData;
        this.fileExtension   = fileExtension;
    }

    public
    byte[] getCompressed() throws IOException {

        if ( compressedImageData == null ) {
            ByteArrayInputStream imageDataStream
                    = new ByteArrayInputStream( sourceImageData );

            BufferedImage image = ImageIO.read( imageDataStream );

            ImageWriteParam param
                    = ImageIO.getImageWritersByFormatName( fileExtension )
                             .next()
                             .getDefaultWriteParam();
            param.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
            param.setCompressionQuality( 0.5f );

            ByteArrayOutputStream compressedImageOutputStream
                    = new ByteArrayOutputStream();

            try {
                ImageIO.write( image,
                               fileExtension,
                               compressedImageOutputStream );
            }
            catch ( IOException e ) {
                logger.info( e.getMessage() );
            }

            compressedImageData = compressedImageOutputStream.toByteArray();
        }

        return compressedImageData;
    }

}
