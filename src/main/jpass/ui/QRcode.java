package main.jpass.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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

public class QRcode
{

    public static void generateQRcode(String qrCodeData, String filePath, String charset)
    {

        try {
//            String qrCodeData = "www.chillyfacts.com";
//            String filePath = "D:\\QRCODE\\chillyfacts.png";
////            String charset = "UTF-8"; // or "ISO-8859-1"

//            Map < EncodeHintType, ErrorCorrectionLevel > hintMap = new HashMap < EncodeHintType, ErrorCorrectionLevel > ();
//            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
//            BitMatrix matrix = new MultiFormatWriter().encode(
//                    new String(qrCodeData.getBytes(charset), charset),
//                    BarcodeFormat.QR_CODE, 500, 500, hintMap);
//            MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
//                                                                               .lastIndexOf('.') + 1), new File(filePath));
//
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 500, 500);
            Path path = FileSystems.getDefault ().getPath (filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static String readQRcode (String filePath)
    {
        Result qrCodeResult = null;
        try {
//            String filePath = "D:\\QRCODE\\chillyfacts.png";
//            String charset = "UTF-8";
//            Map < EncodeHintType, ErrorCorrectionLevel > hintMap = new HashMap < EncodeHintType, ErrorCorrectionLevel > ();
//            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
//            System.out.println("Data read from QR Code: " + readQRCode(filePath, charset, hintMap));
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
            qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
        } catch (Exception e) {
            MessageDialog.showWarningMessage (JPassFrame.getInstance (), "No QR Code found in the image.");
        }
        return (qrCodeResult != null ? qrCodeResult.getText() : null);
    }

    private boolean qrCodeValid (BinaryBitmap binaryBitmap)
    {
        Result qrCodeResult = null;
        try
        {
            qrCodeResult = new MultiFormatReader ().decode (binaryBitmap);
        } catch (NotFoundException e)
        {
            return false;
        }
        return qrCodeResult != null;
    }
}
