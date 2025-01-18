package com.chatroom.room;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfService {
    final private static DataHolder OPTIONS = new MutableDataSet();
    private final Path pdfStorageLocation;

    public PdfService() {
        this.pdfStorageLocation = Paths.get("/Users/ganruilin/IdeaProjects/ChatRoom/pdf-storage");
        // Create directory if it doesn't exist
        this.pdfStorageLocation.toFile().mkdirs();
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
