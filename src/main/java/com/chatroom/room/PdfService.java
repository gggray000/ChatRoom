package com.chatroom.room;

import com.chatroom.chat.WebSocketMessageService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    private static final DataHolder OPTIONS = new MutableDataSet();
    private final Path pdfStorageLocation;
    private final WebSocketMessageService webSocketMessageService;

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public PdfService(
            @Value("${chatroom.pdf.storage:pdf-storage}") String storageDir,
            WebSocketMessageService webSocketMessageService
    ) {
        this.pdfStorageLocation = Paths.get(storageDir).toAbsolutePath().normalize();
        this.pdfStorageLocation.toFile().mkdirs();
        this.webSocketMessageService = webSocketMessageService;
    }

    public String makePdf(String roomId, String content) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(content);
        String htmlContent = renderer.render(document);

        String html = String.format("""
                 <!DOCTYPE html>
                 <html>
                 <head>
                     <meta charset="UTF-8">
                     <style>
                         body {
                             font-family: 'Arial', sans-serif;
                             font-size: 12pt;
                             line-height: 1.6;
                             margin: 40px;
                         }
                        \s
                         h1 {
                             font-size: 24pt;
                             font-weight: bold;
                             color: #2c3e50;
                         }
                        \s
                         h2 {
                             font-size: 18pt;
                             font-weight: bold;
                             color: #34495e;
                         }
                        \s
                         em {
                             font-style: italic;
                         }
                        \s
                         strong {
                             font-weight: bold;
                         }
                        \s
                         p {
                             margin-bottom: 10px;
                         }
                        \s
                         .markdown-content {
                             font-family: 'Arial', sans-serif;
                         }
                     </style>
                 </head>
                 <body>
                     %s
                 </body>
                 </html>
                \s""", htmlContent);

        String filename = "Summary_Room_" + roomId + ".pdf";
        Path pdfPath = pdfStorageLocation.resolve(filename);

        makePdfFromHtml(html, pdfPath.toString());
        return filename;
    }

    private void makePdfFromHtml(String html, String outputPath){
        PdfConverterExtension.exportToPdf(outputPath, html, "", OPTIONS);
        System.out.println("Outputted to: " + outputPath);
    }

    public Resource getUrlResourceOfPdf(String filename) {
        try {
            Path filePath = pdfStorageLocation.resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
        return null;
    }


}
