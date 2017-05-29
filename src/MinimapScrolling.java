import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MinimapScrolling extends JScrollPane {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    private BufferedImage image;
    private BufferedImage topScrollImage;
    private BufferedImage bottomScrollImage;
    
    private JScrollPane mimic;
    private JScrollPane self;
    private JScrollBar sb;
    private JPanel thumb;
    
    private ScriptWriter scriptText;

    private int realHeight;
    
    private int workSpaceY;
    private int topSpaceY;
    private int bottomSpaceY;
    
    private boolean showMap;
    private boolean mapeEnabled;

    double pValue;
    
    private Preview preview;
    
    WheelListener wheelListener = new WheelListener();
    MouseListener mouseListener = new MouseListener();
    ComponentListener componentListener = new ComponentListener();

    public MinimapScrolling( JScrollPane m, ScriptWriter s) {
        self = this;
        mimic = m;
        scriptText = s;
        
        getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setOpaque(false);
        getViewport().setBorder(null);
        setBorder(null);
        
        preview = new Preview();
        preview.setLayout(null);
        Theme.register(preview);
        setViewportView(preview);

        thumb = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(showMap){
                    g.setColor(Theme.getColor("highlightColor"));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.dispose();
                }
            }

            public void filler() {
        
            }

        };
        thumb.setOpaque(false);
        preview.add(thumb);

        mimic.addMouseWheelListener(wheelListener);
        scriptText.addComponentListener(componentListener);
        scriptText.addMouseListener(mouseListener);
        
    }
    
    public class MouseListener extends MouseAdapter{
        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            super.mouseReleased(mouseEvent);
            setScrollPaint();
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            super.mouseDragged(mouseEvent);
            setScrollPaint();
        }
    }
    
    public class ComponentListener extends ComponentAdapter{
        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            realHeight = getContentHeight(scriptText.getText());

            pValue = (double) getWidth() / (double) scriptText.getWidth();

            preview.setPreferredSize(new Dimension((int) (scriptText.getWidth() * pValue), (int) (scriptText.getHeight()
                    * pValue)));

            if(getWidth()>0&&getHeight()>0){
                image = new BufferedImage((int) (getViewport().getWidth() / pValue), (int) (getViewport()
                        .getHeight() / pValue), BufferedImage.TYPE_INT_ARGB);

                topScrollImage = new BufferedImage((int) (getViewport().getWidth() / pValue), (int) (getViewport().getHeight() / pValue), BufferedImage.TYPE_INT_ARGB);

                bottomScrollImage = new BufferedImage((int) (getViewport().getWidth() / pValue), (int)
                        (getViewport().getHeight() / pValue), BufferedImage.TYPE_INT_ARGB);
            }
            else{
                image = null;
                topScrollImage = null;
                bottomScrollImage = null;
            }

            setScrollPaint();
        }
    }
    
    public class WheelListener extends MouseAdapter{
        Timer show;
        int time = 1000;

        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            super.mouseWheelMoved(mouseWheelEvent);
            if(mapeEnabled){
                if (show!=null&&show.isRunning()) {
                    if(showMap==false) show.stop();
                    else show.restart();
                } else {
                    showMap = true;
                    show = new Timer(time, new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            time--;
                            if (time <= 0) {
                                show.stop();
                                time = 1000;
                                showMap = false;
                                self.repaint();
                                thumb.repaint();
                            }
                        }
                    });
                    setScrollPaint();
                    show.start();
                    show.setDelay(1);
                }

                if (mimic.getViewport().getHeight() != 0 && scriptText.getHeight() != 0) {


                    double size = (double) getViewport().getHeight() * pValue;

                    double trackChange = preview.getPreferredSize().getHeight() / (double) getViewport().getHeight();
                    double trackOffset = preview.getPreferredSize().getHeight() / trackChange;

                    double pChange = (double) mimic.getVerticalScrollBar().getValue() / (double) (scriptText
                            .getHeight() - mimic
                            .getViewport().getHeight());
                    double p = (preview.getPreferredSize().getHeight() - trackOffset) * pChange;

                    if (realHeight > getViewport().getHeight() / pValue) {
                        getViewport().setViewPosition(new Point(0, (int) p));
                    }

                    pChange = (double) mimic.getVerticalScrollBar().getValue() / ((double) scriptText.getHeight() - (double) mimic.getViewport().getHeight());
                    p = (preview.getPreferredSize().getHeight() - size) * pChange;

                    thumb.setLocation(0, (int) p);
                    thumb.setSize(getWidth(), (int) (mimic.getHeight() * pValue));

                    scrollPaint();
                }
            }
        }
    }
    
    private class Preview extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Theme.getColor("paneColor"));
            g.fillRect(0, 0, preview.getWidth(), preview.getHeight());

            if(showMap&&mapeEnabled){

                if(image != null){
                    g.drawImage(image, 0, (workSpaceY), (int) (image.getWidth() * pValue), (int) (image.getHeight() * pValue), null);
                }
                if(bottomScrollImage != null){
                    g.drawImage(bottomScrollImage, 0,(bottomSpaceY), (int) (bottomScrollImage.getWidth() * pValue), (int) (bottomScrollImage.getHeight() * pValue), null);
                }
                if(topScrollImage != null){
                    g.drawImage(topScrollImage, 0,(topSpaceY), (int) (topScrollImage.getWidth() * pValue), (int) (topScrollImage.getHeight() * pValue), null);
                }
            }
        }
    }
    
    public void setScrollPaint(){

        if(mapeEnabled){
            if(workSpaceY!=getViewport().getHeight()&&bottomScrollImage!=null&&topScrollImage!=null){

                int constHeight = getViewport().getHeight();

                int viewableStart = getViewport().getViewPosition().y;
                int viewableEnd = getViewport().getViewPosition().y+getViewport().getHeight();

                workSpaceY = viewableStart;
                topSpaceY = workSpaceY - constHeight;
                bottomSpaceY = workSpaceY + constHeight;

                Graphics2D g = bottomScrollImage.createGraphics();
                g.translate(0, (-bottomSpaceY) / pValue);
                scriptText.paint(g);

                g = image.createGraphics();
                g.translate(0, -workSpaceY / pValue);
                scriptText.paint(g);

                g = topScrollImage.createGraphics();
                g.translate(0, (-topSpaceY) / pValue);
                scriptText.paint(g);
                g.dispose();

                repaint();
            }
        }
    }

    public void scrollPaint() {
        
        if(mapeEnabled){
            
            int constHeight = getViewport().getHeight();

            int viewableStart = getViewport().getViewPosition().y;
            int viewableEnd = getViewport().getViewPosition().y + getViewport().getHeight();
            if (viewableStart <= topSpaceY) {
                if (bottomScrollImage != null && topScrollImage != null) {
                    workSpaceY = viewableStart;
                    topSpaceY = workSpaceY - constHeight;
                    bottomSpaceY = workSpaceY + constHeight;

                    Graphics2D g = bottomScrollImage.createGraphics();
                    g.translate(0, (-bottomSpaceY) / pValue);
                    scriptText.paint(g);

                    g = image.createGraphics();
                    g.translate(0, -workSpaceY / pValue);
                    scriptText.paint(g);

                    g = topScrollImage.createGraphics();
                    g.translate(0, (-topSpaceY) / pValue);
                    scriptText.paint(g);
                    g.dispose();
                    repaint();
                }

            } else if (viewableEnd >= bottomSpaceY + constHeight) {
                if (bottomScrollImage != null && topScrollImage != null) {
                    workSpaceY = viewableEnd - constHeight;
                    topSpaceY = workSpaceY - constHeight;
                    bottomSpaceY = workSpaceY + constHeight;

                    Graphics2D g = bottomScrollImage.createGraphics();
                    g.translate(0, (-bottomSpaceY) / pValue);
                    scriptText.paint(g);

                    g = image.createGraphics();
                    g.translate(0, -workSpaceY / pValue);
                    scriptText.paint(g);

                    g = topScrollImage.createGraphics();
                    g.translate(0, (-topSpaceY) / pValue);
                    scriptText.paint(g);
                    g.dispose();
                    repaint();
                }
            }else{
                workSpaceY = viewableStart;
                topSpaceY = workSpaceY - constHeight;
                bottomSpaceY = workSpaceY + constHeight;
            }
        
        }
    }
    
    public int getContentHeight(String content) {
        JEditorPane dummyEditorPane=new JEditorPane();
        dummyEditorPane.setFont(scriptText.getFont());
        dummyEditorPane.setSize(getWidth(),Short.MAX_VALUE);
        dummyEditorPane.setText(content);

        return dummyEditorPane.getPreferredSize().height;
    }

    public void update(){
        preview.repaint();
        setScrollPaint();
        thumb.repaint();
    }
    
    public void mapState(boolean k){
        mapeEnabled = k;
    }

}