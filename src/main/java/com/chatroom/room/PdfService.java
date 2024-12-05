package com.chatroom.room;

import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.data.DataHolder;
import org.springframework.stereotype.Service;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

@Service
public class PdfService {
    final private static DataHolder OPTIONS = new MutableDataSet();
    public void parseString(String content) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(content);
        String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        System.out.println(html);
        producePdf(html);
    }

    private void producePdf(String html){
        PdfConverterExtension.exportToPdf("/Users/ganruilin/IdeaProjects/ChatRoom/flexmark-java.pdf", html, "", OPTIONS);
        System.out.println("Output PDF to /Users/ganruilin/IdeaProjects/ChatRoom/flexmark-java.pdf");
    }


}
