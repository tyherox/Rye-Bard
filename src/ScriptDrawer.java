import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by JohnBae on 10/14/15.
 */
public class ScriptDrawer extends JPanel {

    public ScriptDrawer(){
        setPreferredSize(new Dimension(5000,5000));
        setLayout(null);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount()==2){
                    add(new StoryBlock(e.getPoint()));
                    repaint();
                    revalidate();
                    System.out.println("DOUBLE: "+getWidth() +", " + getHeight());
                }
            }
        });
    }

    private class StoryBlock extends JButton{

        public StoryBlock(Point t){
            setLocation(t);
            setSize(new Dimension(50,50));
            setBackground(Color.YELLOW);
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setColor(Color.WHITE);
        for(int i = 0; i<=5000; i+=50){
            graphics.drawLine(0,i,5000,i);
            graphics.drawLine(i,0,i,5000);
        }
        graphics.dispose();
    }

}
