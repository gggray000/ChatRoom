package com.chatroom.room;

import com.spire.barcode.BarCodeGenerator;
import com.spire.barcode.BarCodeType;
import com.spire.barcode.BarcodeSettings;
import com.spire.barcode.QRCodeECL;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class QrCodeService {

    private BarcodeSettings settings;

    public QrCodeService(){
        this.settings = new BarcodeSettings();
    }

    public BufferedImage qrCodeGeneration(String url, String roomName){
        this.settings.setType(BarCodeType.QR_Code);
        settings.setData(url);
        settings.setX(2);
        settings.setQRCodeECL(QRCodeECL.M);
        settings.setTopText("Room: " + roomName);
        settings.setShowText(false);
        settings.setShowTopText(true);
        settings.isShowBottomText(false);
        settings.hasBorder(false);
        BarCodeGenerator barCodeGenerator = new BarCodeGenerator(settings);
        return barCodeGenerator.generateImage();
    }

    public static void main(String []args) throws IOException {
        // Instantiate a BarcodeSettings object
        BarcodeSettings settings = getBarcodeSettings();
        settings.isShowBottomText(true);

        //Set border visibility
        settings.hasBorder(false);

        //Instantiate a BarCodeGenerator object based on the specific settings
        BarCodeGenerator barCodeGenerator = new BarCodeGenerator(settings);
        //Generate QR code image
        BufferedImage bufferedImage = barCodeGenerator.generateImage();
        //save the image to a .png file
        ImageIO.write(bufferedImage,"png",new File("QR_Code_Test.png"));
    }

    private static @NotNull BarcodeSettings getBarcodeSettings() {
        BarcodeSettings settings = new BarcodeSettings();
        // Set barcode type
        settings.setType(BarCodeType.QR_Code);
        //Set barcode data
        String data = "https://zoom.earth/";
        settings.setData(data);
        //Set barcode module width
        settings.setX(2);
        //Set error correction level
        settings.setQRCodeECL(QRCodeECL.M);

        //Set top text
        settings.setTopText("User Name");
        //Set bottom text
        settings.setBottomText("Event Name");

        //Set text visibility
        settings.setShowText(false);
        settings.setShowTopText(true);
        return settings;
    }
}
