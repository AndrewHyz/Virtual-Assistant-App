package assistant.app;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import javafx.scene.control.Label;

public class MarkdownResponse implements Displayable {

    private String htmlContent;

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    public MarkdownResponse(String markdown) {
        Node document = PARSER.parse(markdown);
        this.htmlContent = RENDERER.render(document);
    }

    @Override
    public void update(Label label) {
        // MarkdownResponse is rendered via WebView in the cell factory
        label.setText("");
    }

    public String getHtmlContent() {
        return htmlContent;
    }
}
