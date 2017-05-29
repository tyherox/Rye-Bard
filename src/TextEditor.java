import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

/**
 * Created by JohnBae on 10/26/15.
 */
public class TextEditor extends JPanel {

    private JPanel self;
    private MinimapScrolling minimap;
    private TextLineNumber tln;
    private ScriptWriter scriptText;
    private JScrollPane textScroll;
    
    private boolean mapEnabled;
    private String name;
    private File address;
    
    private WorkSpace parent; 
    
    public TextEditor(String n, File a, WorkSpace p){
        parent = p;
        address = a;
        name = n;
        self = this;
        setLayout(new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("min(10dlu;default):grow(6)"),
                ColumnSpec.decode("min(10dlu;default):grow(1)"),},
                new RowSpec[]{
                        RowSpec.decode("min(10dlu;default):grow"),}));
        setBorder(BorderFactory.createLineBorder(Theme.getColor("borderColor")));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        minimap.setPreferredSize(new Dimension(self.getWidth() / 6, 0));
                    }
                });
            }
        });
        
        textScroll = new JScrollPane();
        textScroll.getViewport().setOpaque(false);
        textScroll.getViewport().setBorder(null);
        textScroll.setBorder(null);
        textScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        textScroll.getVerticalScrollBar().setUnitIncrement(5);
        textScroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        textScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.trackColor = Theme.getColor("paneColor");
                this.thumbColor = Theme.getColor("nullColor");
                this.thumbDarkShadowColor = Theme.getColor("nullColor");
                this.thumbHighlightColor = Theme.getColor("nullColor");
                this.thumbLightShadowColor = Theme.getColor("nullColor");
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton filler = new JButton();
                filler.setVisible(false);
                filler.setPreferredSize(new Dimension(0, 0));
                return filler;
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton filler = new JButton();
                filler.setVisible(false);
                filler.setPreferredSize(new Dimension(0, 0));
                return filler;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                g.setColor(Theme.getColor("nullColor"));
                g.fillRect((int) thumbBounds.getX(), (int) thumbBounds.getY(), (int) thumbBounds.getWidth(), (int) thumbBounds.getHeight());

            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(Theme.getColor("secondaryColor").brighter());
                g.fillRect((int) trackBounds.getX(), (int) trackBounds.getY(), (int) trackBounds.getWidth(), (int) trackBounds.getHeight());

            }

        });
        add(textScroll, "1, 1, fill, fill");

        JPanel holder = new JPanel();
        holder.setLayout(new FormLayout(new ColumnSpec[]{
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("min(10dlu;default):grow"),},
                new RowSpec[]{
                        RowSpec.decode("default:grow"),}));
        textScroll.setViewportView(holder);
        holder.setOpaque(false);

        JPanel sidePanel = new JPanel();
        holder.add(sidePanel, "2, 1, fill, fill");
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.X_AXIS));

        scriptText = new ScriptWriter(textScroll,this){

            @Override
            public void setFont(Font f){
                super.setFont(f);
                if(tln!=null) tln.setFont(f);
            }
        };
        scriptText.requestFocus();
        holder.add(scriptText, "4, 1, fill, fill");

        tln = new TextLineNumber(scriptText);
        tln.setBorderGap(10);
        textScroll.setRowHeaderView(tln);

        minimap = new MinimapScrolling(textScroll, scriptText);
        add(minimap, "2,1,fill,fill");

        Theme.register(scriptText);
        Theme.register(tln);
        Theme.register(this);
        redraw();
    }
    
    public WorkSpace getWorkSpace(){
        return parent;
    }
    
    public void setAddress(File a){
        address = a;
    }
    
    public File getAddress(){
        return address;
    }
    
    public void setName(String n){
        name = n;
    }
    
    public String getName(){
        return name;
    }
    
    public void setMapState(boolean k){
        mapEnabled = k;
        if(mapEnabled==false) {
            minimap.setVisible(false);
            minimap.mapState(false);
        }
        else {
            minimap.setVisible(true);
            minimap.mapState(true);
        }
    }

    public ScriptWriter getReader() {
        return scriptText;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Theme.getColor("paneColor"));
        g.fillRect(0, 0, getWidth(), getHeight());

    }

    public void redraw(){
        scriptText.redraw();
        
        setBorder(BorderFactory.createLineBorder(Theme.getColor("borderColor")));
        setBackground(Theme.getColor("paneColor"));
        
        tln.setForeground(Theme.getColor("nullColor"));
        tln.setBackground(Theme.getColor("paneColor"));
        textScroll.setBackground(Theme.getColor("paneColor"));
        
        minimap.update();
    }

}
