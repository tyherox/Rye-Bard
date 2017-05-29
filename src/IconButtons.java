import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class IconButtons extends JButton {

    boolean selected = false;

    Color defaultButton;
    Color rolloverButton;
    Color pressedButton;
    Color current = null;

    AffineTransform at = new AffineTransform();


    public IconButtons(String text) {
        super.setHorizontalTextPosition(SwingConstants.CENTER);
        super.setVerticalTextPosition(SwingConstants.CENTER);
        setContentAreaFilled(false);

        defaultButton = new Color(128, 128, 128, 0);
        rolloverButton = Theme.getColor("gentleHighlightColor");
        pressedButton = Theme.getColor("borderColor");
        current = defaultButton;

        setBorderPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseReleased(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = pressedButton;
                    getParent().repaint();
                }
                super.mousePressed(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = rolloverButton;
                    getParent().repaint();
                }
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseExited(e);
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(!isEnabled()){
                    current = pressedButton;
                    if(getParent()!=null) getParent().repaint();
                }
            }
        });
        revalidate();

        repaint();
    }

    public void deselect(){
        selected = false;
        current = defaultButton;
        repaint();
    }

    public void select(){
        selected = true;
        current = pressedButton;
        repaint();
    }

    @Override
    public void setEnabled(boolean k){
        super.setEnabled(k);
        if(k) deselect();
        else select();
    }

    public boolean isPressed(){
        if (selected)return true;
        else return false;
    }
    
    public void redraw(){
        defaultButton = new Color(128, 128, 128, 0);
        rolloverButton = Theme.getColor("gentleHighlightColor");
        pressedButton = Theme.getColor("borderColor");
        current = defaultButton;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setColor(current);
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        graphics.dispose();

        super.paintComponent(g);
    }
}
