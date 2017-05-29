import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ScriptWriter extends JTextPane {

    private SmartText texts = new SmartText();

    private ScriptWriter selfPane;
    private DefaultStyledDocument selfDoc;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private boolean undoablecalled = false;
    private boolean syntax = true;
    private boolean spelling = true;

    private UndoManager time = new UndoManager();

    private int rowStart = 0;
    private int rowEnd = 0;

    private TextEditor parent;

    private final ScheduledExecutorService textpane = Executors.newScheduledThreadPool(2);

     SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
     SimpleAttributeSet nullAttr = new SimpleAttributeSet();
     SimpleAttributeSet secondaryAttr = new SimpleAttributeSet();
     SimpleAttributeSet blockAttr = new SimpleAttributeSet();
     SimpleAttributeSet syntaxAttr = new SimpleAttributeSet();
     SimpleAttributeSet errorAttr = new SimpleAttributeSet();
     SimpleAttributeSet tertiaryAttr = new SimpleAttributeSet();
     SimpleAttributeSet variableAttr = new SimpleAttributeSet();
    
    private Point currentBlock[] = {new Point(-1,-1),new Point(-1,-1)};
    private Point currentBracket[] = {new Point(-1,-1),new Point(-1,-1)};

    public StyledDocument getDoc(){
        return selfDoc;
    }
    
    public ScriptWriter(JScrollPane scrollPane, TextEditor p){

        parent = p;
        
        isFocusable();
        time.setLimit(100);

        setDragEnabled(false);
        selfPane = this;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(0,0);
        BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
        final Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "InvisibleCursor");
        final Cursor originalCursor = Cursor.getDefaultCursor();

        /** styledDocument
         * */

        selfDoc = new DefaultStyledDocument() {

            public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
                long start = System.nanoTime();
                str = str.replaceAll("[’]", "\'");
                super.insertString(offset, str, a);
                long finish = System.nanoTime();
                //System.out.println((finish - start) / 100000000.00000000 + ": text rendering time ");
                
                setCharacterAttributes(offset,str.length(),defaultAttr,true);
                char[] input = str.toCharArray();
                int  k = offset;
                for(char c : input) {
                    texts.add(k, Character.toString(c) + "*");
                    if(k>0)texts.mark(k-1);
                    else texts.mark(k);
                    k++;
                }

                if(undoablecalled==false) {
                    time.undoableEditHappened(new UndoableEditEvent(selfDoc,
                            new UndoableTextEdit(selfDoc, str, offset,"inserted")) {});
                }
            }

            public void remove (int offset, int len) throws BadLocationException {
                String removed = getText(offset, len);
                super.remove(offset, len);
                //check();

                char[] input = removed.toCharArray();
                int  j = offset+len;
                for(char temp : input) {
                    texts.remove(j - 1);
                    j--;
                }
                texts.mark(getCaretPosition());

                if(undoablecalled==false) {
                    time.undoableEditHappened(new UndoableEditEvent(selfDoc,
                            new UndoableTextEdit(selfDoc, removed, offset, "removed")) {
                    });
                }
            }

            @Override
            public void setCharacterAttributes(int var1, int var2, AttributeSet var3, boolean var4){
                super.setCharacterAttributes( var1,  var2,  var3, var4);
                //parent.getMinimap().getDoc().setCharacterAttributes(var1, var2, var3, var4);
            }

        };
        setDocument(selfDoc);

        addAncestorListener (new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                countWords();
            }

            public void ancestorRemoved(AncestorEvent event) {
                countWords();
            }

            public void ancestorMoved(AncestorEvent event) {
                countWords();
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                countWords();
                super.keyReleased(keyEvent);
                if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
                    //texts.autoFill(getCaretPosition());
                }
            }
            @Override
            public void keyPressed(KeyEvent keyEvent){
                countWords();
                super.keyReleased(keyEvent);
            }
        });

        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                texts.readScript(getCaretPosition());
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                texts.readScript(getCaretPosition());
            }
        });

        SimpleAttributeSet attribs = new SimpleAttributeSet();
        setParagraphAttributes(attribs, false);

        getCaret().setBlinkRate(800);

        /** keystroke bindings
         */

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
        getActionMap().put("undo", new Undo());

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "selectAll");
        getActionMap().put("selectAll", new selectAll(this));
        
        setMargin(new Insets(10, 20, 10, 10));

        DefaultCaret caret = (DefaultCaret)getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        Font font = new Font("Monospaced", Font.PLAIN, 14);
        setFont(font);
        
        countWords();
    }

    public void redraw(){

        StyleConstants.setForeground(defaultAttr,Theme.getColor("defaultColor"));
        StyleConstants.setForeground(nullAttr, Theme.getColor("nullColor"));
        StyleConstants.setForeground(secondaryAttr, Theme.getColor("bracketColor"));
        StyleConstants.setForeground(blockAttr, Theme.getColor("blockColor"));
        StyleConstants.setForeground(syntaxAttr, Theme.getColor("syntaxColor"));
        StyleConstants.setForeground(errorAttr, Theme.getColor("errorColor"));
        StyleConstants.setForeground(tertiaryAttr, Theme.getColor("tertiaryColor"));
        StyleConstants.setForeground(variableAttr, Theme.getColor("variableColor"));
        
        reInitialize();

        setForeground(Theme.getColor("defaultColor"));
        setCaretColor(Theme.getColor("defaultColor"));
        setBackground(Theme.getColor("paneColor"));
        getParent().setBackground(getBackground());
    }

    public void overrideText(String text) {
        try {
            selfDoc.replace(0, selfDoc.getLength(),text,null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        selfPane.revalidate();
        selfPane.repaint();
        time.discardAllEdits();
    }

    public void reInitialize() {

        for (int i = 0; i < texts.size(); i++) {
            char[] c = texts.get(i).toCharArray();

            switch (c[1]){
                case 'd': selfDoc.setCharacterAttributes(i, 1, defaultAttr, false);
                    break;
                case 'n': selfDoc.setCharacterAttributes(i, 1, nullAttr, false);
                    break;
                case 's': selfDoc.setCharacterAttributes(i, 1, secondaryAttr, false);
                    break;
                case 'b': selfDoc.setCharacterAttributes(i, 1, blockAttr, false);
                    break;
                case 'S': selfDoc.setCharacterAttributes(i, 1, syntaxAttr, false);
                    break;
                case 'e': selfDoc.setCharacterAttributes(i, 1, errorAttr, false);
                    break;
                case 'v': selfDoc.setCharacterAttributes(i, 1, variableAttr, false);
                    break;
                case 't': selfDoc.setCharacterAttributes(i, 1, tertiaryAttr, false);
                    break;

            }

        }
    }

    public void countWords() {
        SwingUtilities.invokeLater(new Runnable() {
            int word = 0;
            int character = 0;
            WorkSpace k = parent.getWorkSpace();

            @Override
            public void run() {

                String text = "";
                try {
                    text = selfDoc.getText(0, selfDoc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }

                String refined = text;
                refined = refined.replaceAll("(\r\n|\n)", " ");
                String[] arr = refined.split(" ");
                for (final String ss : arr) {
                    refined = ss.replaceAll("[\\d[^\\w\\s]]+", "").replaceAll("(\\s{2,})", " ").replaceAll("[^a-zA-Z0-9]", "");
                    if (!refined.equals(" ") && !refined.equals("")) {
                        word++;
                        character += refined.length();
                    }
                }
                k.updateWordCount(word + ":" + character);
            }
        });
    }

    class SmartText extends ArrayList<String> {

         ArrayList<String> ID;

        int ws = -1;
        int we = -1;
        int rs = -1;
        int re = -1;
        int line = -1;

        public SmartText() {
            ID = this;
        }

        @Override
        public void add(int i, String t) {
            String text = "";
            try {
                text = selfDoc.getText(0, selfDoc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            if(size()!=0&&size()>i) {
                if(text.length()>size()) {
                    super.add(i,t);
                }
                else {
                    set(i, t);
                }

            }
            else if(i>size()) {
                while(i!=size()) {
                    add("");
                }
                add(t);
            }
            else {
                super.add(t);
            }
        }

        @Override
        public String remove(int i) {
            {
                if(i>=size()) {
                    if(size()-1>=0) i=size()-1;
                }
                if(size()!=0) super.remove(i);
            }
            
            return null;
        }

        public void mark(final int i){

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ws = wordStart(i);
                    we = wordEnd(i);

                    if (line != getTextLineNumber(i)) {
                        line = getTextLineNumber(i);
                        readScript(i);
                    }

                    rs = rowStart(i);
                    re = rowEnd(i);

                    String word = constructWord(ws, we);
                    if (isOperator(i) ||isEndOperator(i)) {
                        colorSyntax(word, ws);
                    } 
                    if(isOperator(rs) || isEndOperator(rs)){
                        String w = wordAt(rs);
                        colorSyntax(w, rs);
                        selfDoc.setCharacterAttributes(rs + w.length(), re - (rs + w.length()), tertiaryAttr, true);
                        updateSmartText(rs + w.length(), re, "t");
                    }
                    else if (isVar(i)) {
                        selfDoc.setCharacterAttributes(ws, word.length(), variableAttr, true);
                        updateSmartText(ws, we, "v");
                    } else {
                        selfDoc.setCharacterAttributes(rs, re - rs,defaultAttr, true);
                        updateSmartText(rs, re, "t");
                    }
                }
            });
            
        }
        
        private void autoFill(final int pos){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int i = pos;
                    if(i>1){
                        ws = wordStart(--i);
                        we = wordEnd(--i);
                        String word = constructWord(ws, we);

                        if(word.equals("<b>")&&currentBlock[0].equals(new Point(-1, -1))&&currentBlock[1].equals(new
                                Point(-1, -1))){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</>",null);
                                setCaretPosition(i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }

                        else if(word.equals("<t>")&&currentBracket[0].equals(new Point(-1, -1))&&currentBracket[1].equals
                                (new Point(-1, -1))){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</t>",null);
                                setCaretPosition(i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }

                        else if(word.equals("<if>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</if>",null);
                                setCaretPosition(--i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(word.equals("<else>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</else>",null);
                                setCaretPosition(i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        if(word.equals("<elseif>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</elseif>",null);
                                setCaretPosition(i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(word.equals("<o>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</o>",null);
                                setCaretPosition(--i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }

                        else if(word.equals("<d>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</d>",null);
                                setCaretPosition(--i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(word.equals("<u>")){
                            try {
                                i+=2;
                                selfDoc.insertString(i,System.getProperty("line.separator")+"</u>",null);
                                setCaretPosition(i);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            
        }
        
        private void readScript(final int i){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    long start = System.nanoTime();
                    int pos = 0;

                    boolean blockStart = false;
                    boolean foundBlock = false;

                    boolean bracketStart = false;
                    boolean foundBracket = false;
                    
                    String bracketOperator = "";
                    
                    Point blockArea[] = {new Point(-1,-1),new Point(-1,-1)};
                    currentBlock[0] = new Point(-1,-1);
                    currentBlock[1] = new Point(-1,-1);

                    Point bracketArea[] = {new Point(-1,-1),new Point(-1,-1)};
                    currentBracket[0] = new Point(-1,-1);
                    currentBracket[1] = new Point(-1,-1);
                    
                    while(pos<ID.size()){

                        pos = rowStart(pos);
                        String word = wordAt(pos);

                        if(word.equals("<b>")){

                            blockStart = true;

                            blockArea[0] = new Point(-1,-1);
                            
                            if(i>=pos && foundBlock == false) {
                                try {
                                    if(selfPane.isVisible()&&selfPane.modelToView(pos)!=null)
                                    blockArea[0] = selfPane.modelToView(pos).getLocation();
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        if(word.equals("</>") && blockStart){

                            blockStart = false;

                            if(i<=pos  && foundBlock == false && !blockArea[0].equals(new Point(-1,-1))) {
                                try {
                                    currentBlock[0] = blockArea[0];
                                    currentBlock[1] = selfPane.modelToView(pos).getLocation();
                                    foundBlock = true;
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        if(isOperator(pos)){
                            bracketOperator = word;
                            bracketStart = true;
                            
                            bracketArea[0] = new Point(-1,-1);
                            
                            if(i>=pos && foundBracket == false) {
                                try {
                                    if(selfPane.modelToView(pos)!=null)
                                    bracketArea[0] = selfPane.modelToView(pos).getLocation();
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        if(word.equals(bracketOperator.replace("<","</")) && bracketStart){
                            bracketStart = false;

                            if(i<=pos  && foundBracket == false && !bracketArea[0].equals(new Point(-1,-1))) {
                                try {
                                    currentBracket[0] = bracketArea[0];
                                    currentBracket[1] = selfPane.modelToView(pos).getLocation();
                                    
                                    foundBracket = true;
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        
                        
                        repaint();
                        pos = rowEnd(pos);
                        pos++;
                    }

                    long finish = System.nanoTime();
                    //System.out.println((finish - start) / 100000000.00000000 + ": text rendering time ");
                }
            });
        }

        private void colorSyntax(String word, int ws){
            if(word.equals("<t>")||word.equals("</t>")){
                selfDoc.setCharacterAttributes(ws, word.length(), secondaryAttr, true);
                updateSmartText(ws, ws+word.length(), "s");
            }
            else if(word.equals("<b>")||word.equals("<r>")||word.equals("</>")){
                selfDoc.setCharacterAttributes(ws, word.length(), blockAttr, true);
                updateSmartText(ws, ws+word.length(), "b");
            }
            else if(word.equals("<if>")||word.equals("</if>")||word.equals("<else>")||word.equals("</else>")||word
                    .equals("<elseIf>")||word.equals("</elseIf>")||word.equals("<o>")||word.equals("</o>")||word
                    .equals("<d>")||word.equals("</d>")||word.equals("<u>")||word.equals("</u>")||word.equals("<c>")){
                selfDoc.setCharacterAttributes(ws, word.length(), syntaxAttr, true);
                updateSmartText(ws, ws+word.length(), "S");
            }
            else if(isOperator(ws)){
                selfDoc.setCharacterAttributes(ws, word.length(), errorAttr, true);
                updateSmartText(ws, ws+word.length(), "e");
            }
        }

        private void updateSmartText(int offset, int end, String p){
            char[] input = getText().substring(offset,end).toCharArray();
            int  k = offset;
            for(char c : input) {
                texts.set(k, Character.toString(c)+ p);
                k++;
            }
        }

        private String constructWord(int s, int f){
            String result = "";
            for(;s<f;s++){
                result+=ID.get(s).charAt(0);
            }
            return result;
        }

        private int wordStart(int index) {
            while (--index >= 0) {
                char k = ID.get(index).charAt(0);
                if(k==' '||k=='\n'||k=='\r') {
                    break;
                }
            }
            if(index<0)
            {
                index=0;
                return index;
            }
            else
            {
                return ++index;
            }
        }

        private int wordEnd(int index) {
            while (index < ID.size()) {
                char k = ID.get(index).charAt(0);
                if(k==' '||k=='\n'||k=='\r') {
                    break;
                }
                index++;
            }
            return index;
        }

        private String wordAt(int i){
            return constructWord(wordStart(i),wordEnd(i));
        }

        private int rowStart (int index) {
            while (--index >= 0 ) {
                char k = ID.get(index).charAt(0);
                if((k=='\n'||k=='\r')) {
                    break;
                }
            }
            if(index<0)
            {
                index=0;
                return index;
            }
            else
            {
                return ++index;
            }
        }

        private int rowEnd (int index) {
            while (index < ID.size()) {
                char k = ID.get(index).charAt(0);
                if(k=='\n'||k=='\r') {
                    break;
                }
                index++;
            }
            return index;
        }

        public boolean isVar(int offset) {
            String word = constructWord(wordStart(offset),wordEnd(offset));
            if(word.length()>0&&word.charAt(0)=='{'&&word.charAt(word.length()-1)=='}'){
                return true;
            }
            return false;
        }

        public boolean isOperator(int offset) {
            String word = wordAt(offset);
            if(word.length()>0&&word.charAt(0)=='<'&&word.charAt(word.length()-1)== '>'){
                if(word.length()>1&&word.charAt(1)!='/') return true;
            }
            return false;
        }

        public boolean isEndOperator(int offset){
            String word = constructWord(wordStart(offset),wordEnd(offset));
            if(word.length()>1&&word.charAt(0)=='<'&&word.charAt(1)=='/'&&word.charAt(word.length()-1)=='>'){
                return true;
            }
            return false;
        }

        public boolean isTextOperator(int offset){
            String word = constructWord(wordStart(offset), wordEnd(offset));
            if(word.equals("<t>")||word.equals("</t>")){
                return true;
            }
            return false;
        }
        
        protected int getTextLineNumber(int rowStartOffset) {
            Element root = selfPane.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement( index );

            if (line.getStartOffset() == rowStartOffset)
                return index+1;
            else
                return -1;
        }
    }
    
    private class selectAll extends AbstractAction{
        ScriptWriter h;
        public selectAll(ScriptWriter holder) {
            h = holder;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            h.select(0, h.getText().length());
        }
    }

    private class Undo extends AbstractAction{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(time.isSignificant()&&time.canUndo()) {
                time.undo();
            }
        }
    }

    class UndoableTextEdit extends AbstractUndoableEdit {
        private String t;
        private int p;
        private Document d;
        String s = "";
        public UndoableTextEdit(Document doc, String text, int pos, String state) {
            d = doc;
            t = text;
            p = pos;
            s=state;
        }
        public void undo() {
            try {
                if(s.equals("inserted")) {
                    undoablecalled = true;
                    d.remove(p, t.length());
                    undoablecalled = false;
                }
                else if(s.equals("removed")){
                    undoablecalled = true;
                    d.insertString(p, t, null);
                    undoablecalled = false;
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        FontMetrics metric = getFontMetrics(getFont());
        int height = metric.getHeight()/2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(2));
        if(!currentBlock[0].equals(new Point(-1,-1))||!currentBlock[1].equals(new Point(-1, -1))){
            g2.setColor(Theme.getColor("blockColor"));
            g2.drawLine(0, currentBlock[0].y + height, 0, currentBlock[1].y + height);
            g2.drawLine(0,currentBlock[0].y+height,5,currentBlock[0].y+height);
            g2.drawLine(0,currentBlock[1].y+height,5,currentBlock[1].y+height);
        }
        if(!currentBracket[0].equals(new Point(-1,-1))||!currentBracket[1].equals(new Point(-1, -1))){
            g2.setColor(Theme.getColor("nullColor"));
            g2.drawLine(5, currentBracket[0].y + height, 5, currentBracket[1].y + height);
        }
    }


}



