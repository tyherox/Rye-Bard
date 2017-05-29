import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Theme {

    private static ArrayList<JTextComponent> textComponent = new ArrayList<JTextComponent>();
    private static ArrayList<JComponent> graphicComponent = new ArrayList<JComponent>();

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static Map<String,Color> positiveTheme = new HashMap<String, Color>();
    private static Map<String,Color> NegativeTheme = new HashMap<String, Color>();

    static private boolean inverse = false;
    static private boolean story = false;

    public static void initialize(){

        positiveTheme.put("defaultColor", new Color(28, 28, 28));
        positiveTheme.put("nullColor", new Color(155, 155, 155));
        positiveTheme.put("paneColor", new Color(255, 255, 255));
        positiveTheme.put("backgroundColor", new Color(215, 215, 215));
        positiveTheme.put("secondaryColor", new Color(174, 174, 174));
        positiveTheme.put("borderColor", new Color(156, 156, 156));
        positiveTheme.put("bracketColor", new Color(50, 128, 53));
        positiveTheme.put("blockColor", new Color(210, 129, 55));
        positiveTheme.put("syntaxColor", new Color(61, 122, 186));
        positiveTheme.put("variableColor", new Color(186, 161, 56));
        positiveTheme.put("tertiaryColor", new Color(132, 39, 140));
        positiveTheme.put("errorColor", new Color(172, 13, 23));
        positiveTheme.put("highlightColor", new Color(154, 147, 144, 100));
        positiveTheme.put("gentleHighlightColor", new Color(200, 191, 187, 100));


        NegativeTheme.put("defaultColor", new Color(196, 196, 196));
        NegativeTheme.put("nullColor", new Color(128, 128, 128));
        NegativeTheme.put("paneColor", new Color(43, 43, 43));
        NegativeTheme.put("backgroundColor", new Color(69, 69, 69));
        NegativeTheme.put("secondaryColor", new Color(52, 52, 52));
        NegativeTheme.put("borderColor", new Color(25, 25, 25));
        NegativeTheme.put("bracketColor", new Color(73, 181, 77));
        NegativeTheme.put("blockColor", new Color(227, 148, 35));
        NegativeTheme.put("syntaxColor", new Color(62, 124, 189));
        NegativeTheme.put("variableColor", new Color(186, 161, 56));
        NegativeTheme.put("tertiaryColor", new Color(188, 86, 181));
        NegativeTheme.put("errorColor", new Color(235, 18, 31));
        NegativeTheme.put("highlightColor", new Color(172, 13, 23, 100));
        NegativeTheme.put("gentleHighlightColor", new Color(200, 191, 187, 45));
        
    }

    public static void setInverse(boolean k){
        inverse = k;
    }

    public static void setFocus(boolean k){
        story = k;
    }

    public static Color getColor(String k){
        if(inverse) return NegativeTheme.get(k);
        else return positiveTheme.get(k);
    }

    public static void register(JComponent component){
        graphicComponent.add(component);
    }

    public static boolean drawTheme() {

        long start = System.nanoTime();

        for(JComponent temp : graphicComponent){
            if(temp instanceof TextEditor) ((TextEditor) temp).redraw();
            if(temp instanceof FileWalker) ((FileWalker) temp).redraw();
            if(temp instanceof IconButtons) ((IconButtons) temp).redraw();
            else {
                temp.setFont(getDefaultFont());
                temp.setForeground(getColor("defaultColor"));
                temp.repaint();
            }
        }

        long finish = System.nanoTime();

        System.out.println((finish - start) / 100000000.00000000 + ": rendering time ");

        return true;
    }
    
    public static Font getDefaultFont(){
        Font font = new Font("Dialog", Font.PLAIN, 14);
        return font;
    }

}