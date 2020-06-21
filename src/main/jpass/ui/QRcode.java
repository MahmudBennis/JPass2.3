package main.jpass.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * QR code class for Generating and Reading QR codes.
 *
 * @author Mahmud Ibr Bennis.
 *
 */
public class QRcode
{
    /**
     * Generating the QR code.
     * @param qrCodeData - The data that need to be hidden into the QR code.
     * @param filePath - The file path where the generated QR code will be saved.
     */
    public static void generateQRcode(String qrCodeData, String filePath)
    {

        try {
            Map < EncodeHintType, ErrorCorrectionLevel > hintMap = new HashMap < EncodeHintType, ErrorCorrectionLevel > ();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            final var contents = new String (qrCodeData.getBytes (Charset.defaultCharset ()), Charset.defaultCharset ());
            BitMatrix matrix = new MultiFormatWriter().encode (contents, BarcodeFormat.QR_CODE, 500, 500, hintMap);
            MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
                                                                               .lastIndexOf('.') + 1), new File(filePath));
            int dec = 10;
            while (!qrCodeValid (filePath) || Objects.requireNonNull (readQRcode (filePath)).length () < 20)
            {
                matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, 500-dec, 500-dec, hintMap);
                MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
                                                                                   .lastIndexOf('.') + 1), new File(filePath));
                dec+=10;
            }


        } catch (Exception e) {
            System.err.println(e.getMessage ());
        }
    }

    /**
     * Reading the QR code.
     * @param filePath - The file path where the QR code is stored.
     * @return - returns the text of the QR code.
     */
    public static String readQRcode (String filePath)
    {
        Result qrCodeResult = null;
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
            qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
        } catch (Exception e) {
            MessageDialog.showWarningMessage (JPassFrame.getInstance (), "No QR Code found in the image.");
        }
        return (qrCodeResult != null ? qrCodeResult.getText() : null);
    }

    /**
     * Checking whether the generated QR code is valid or not.
     * @param filePath - The file path.
     * @return - returns True it is valid, and False if it is not.
     */
    public static boolean qrCodeValid (String filePath)
    {
        Result qrCodeResult = null;
        try
        {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
            qrCodeResult = new MultiFormatReader ().decode (binaryBitmap);
        } catch (NotFoundException | FileNotFoundException e)
        {
            return false;
        } catch (IOException e)
        {
            e.printStackTrace ();
        }
        return qrCodeResult != null;
    }
}
