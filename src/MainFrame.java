import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by JohnBae on 10/27/15.
 */
public class MainFrame {

    private static File systemFiles = new File("/Users/" + System.getProperty("user.name") +"/Documents/Rye Bard");
    private static File saveFiles = new File(systemFiles.getAbsolutePath()+"/Workspace");

    public static void main(String[] args) {

        //UIManager.put("Tree.rendererFillBackground", false);
        
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Rye Bard");
        
        System.out.println("Total memory (bytes): " +
                Runtime.getRuntime().totalMemory() * .000001);
        System.out.println("Free memory (bytes): " +
                Runtime.getRuntime().freeMemory() * .000001);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        int choice = JOptionPane.showOptionDialog(null, //Component parentComponent
                "Welcome to Rye Bard", //Object message,
                "Rye Bard", //String title
                JOptionPane.YES_NO_OPTION, //int optionType
                JOptionPane.PLAIN_MESSAGE, //int messageType
                null, //Icon icon,
                new String[]{"New Project", "Load Project"}, //Object[] options,
                "New Project");//Object initialValue
        if(choice == 0 ){
            newProject();
        }
        else if (choice== 1){
            loadProject();
        }
        else{
            System.exit(0);
        }
    }

    public static void newProject(){

        String k = (String)JOptionPane.showInputDialog(
                null,
                "Name your Project: ",
                "Rye Bard",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
        if(k!=null){
            JFileChooser fc = new JFileChooser(saveFiles);
            fc.setSelectedFile(new File(saveFiles.getAbsolutePath()+"/"+k));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setDialogType(JFileChooser.SAVE_DIALOG);

            int returnVal = fc.showDialog(null,
                    "Save Here");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                File bard = new File(file.getAbsolutePath()+"/"+k+".bard");
                file.mkdir();

                File Script = new File(file.getAbsolutePath()+"/Script");
                Script.mkdir();
                File Pictures = new File(file.getAbsolutePath()+"/Image");
                Pictures.mkdir();

                try {
                    bard.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            WorkSpace worker = new WorkSpace(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void loadProject(){
        JFileChooser fc = new JFileChooser(saveFiles);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                int i = f.getName().lastIndexOf(".");
                if(i!=-1) {
                    String ext = f.getName().substring(i);
                    return ext.equals(".bard")?true : false;
                }
                else {
                    if(f.isDirectory()) return true;
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "Any folder";
            }

        });

        int returnVal = fc.showDialog(null,
                "Select");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            int i = file.getName().lastIndexOf(".");
            if(i!=-1) {
                String ext = file.getName().substring(i);
                 if(ext.equals(".bard")){
                     System.out.println(file.getParentFile().getAbsolutePath());

                     EventQueue.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 WorkSpace worker = new WorkSpace(file.getParentFile());
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                         }
                     });

                 }
            }
        }
    }

}
