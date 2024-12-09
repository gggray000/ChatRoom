package com.chatroom.room;

import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.data.DataHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
        String html = renderer.render(document);

        String filename = roomId + ".pdf";
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
