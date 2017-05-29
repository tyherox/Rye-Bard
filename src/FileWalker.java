import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class FileWalker extends JPanel {
    /** Construct a FileWalker */

    private JTree tree;
    File selectedFile;
    File address;
    File copiedFile;
    WorkSpace workSpace;
    JScrollPane scrollpane;
    JPanel self;
    RepaintManager repaintM = new RepaintManager();

    JPopupMenu popupMenu;
    JMenuItem pasteMenuItem;
    JMenuItem copyMenuItem;
    
    public FileWalker(final File dir, WorkSpace w) {
        self = this;
        
        // Make a fileTree list with all the nodes, and make it a JTree
        address = dir;
        workSpace = w;
        tree = new JTree(addNodes(null, dir));

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                if (treeSelectionEvent.getNewLeadSelectionPath() != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionModel().getSelectionPath().getLastPathComponent();
                    File f = treeToFile(node);
                    if (f != null) {
                        selectedFile = f;
                    }
                    System.out.println("SELECTED: " + selectedFile);
                }
            }

            public void filler() {

            }
        });
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                // decide what icons you want by examining the node
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    if (treeToFile(node) instanceof File) {
                        // decide based on some property of your Contact obj
                        File contact = treeToFile(node);
                        if (contact.isDirectory()) {
                            setIcon(UIManager.getIcon("FileView.directoryIcon"));
                        } else if (contact.isFile()) {
                            setIcon(UIManager.getIcon("FileView.fileIcon"));
                        }
                    }
                }

                return this;
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        if(selectedFile.isFile()) workSpace.loadStory(selectedFile);
                    }
                }
            }
        });
        
        popupMenu = new JPopupMenu();
        
        scrollpane = new JScrollPane();
        scrollpane.setViewportView(tree);
        scrollpane.setOpaque(false);
        scrollpane.getViewport().setOpaque(false);
        scrollpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(new OpenAction());
        popupMenu.add(openMenuItem);
        
        JMenu newMenuItem = new JMenu("New");
        popupMenu.add(newMenuItem);

        JMenuItem newFileMenuItem = new JMenuItem("Script");
        newFileMenuItem.addActionListener(new NewFileAction());
        newMenuItem.add(newFileMenuItem);

        JMenuItem newFolderMenuItem = new JMenuItem("Folder");
        newFolderMenuItem.addActionListener(new NewFolderAction());
        newMenuItem.add(newFolderMenuItem);

        copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(new CopyAction());
        popupMenu.add(copyMenuItem);

        pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener(new PasteAction());
        pasteMenuItem.setEnabled(false);
        popupMenu.add(pasteMenuItem);

        JMenuItem renameMenuItem = new JMenuItem("Rename");
        renameMenuItem.addActionListener(new RenameAction());
        popupMenu.add(renameMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(new DeleteAction());
        popupMenu.add(deleteMenuItem);

        tree.addMouseListener(new MouseAdapter() {
            private void showIfPopupTrigger(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    popupMenu.show(mouseEvent.getComponent(),
                            mouseEvent.getX(),
                            mouseEvent.getY());
                }
            }

            public void mousePressed(MouseEvent mouseEvent) {
                showIfPopupTrigger(mouseEvent);
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                showIfPopupTrigger(mouseEvent);
            }
        });
        tree.setBorder(null);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(scrollpane);
        updateTree();
    }

    public void updateTree(){
        
        if (tree.getModel() instanceof DefaultTreeModel)
        {
            final DefaultTreeModel renderer = (DefaultTreeModel)(tree.getModel());

            final ArrayList<TreePath> expanded = new ArrayList<TreePath>();

            for (int i = 0; i < tree.getRowCount(); i++) {
                if(tree.isExpanded(i)) {
                    expanded.add(tree.getPathForRow(i));
                }
            }
            
            renderer.setRoot(addNodes(null, address));

            for (int i = 0; i < tree.getRowCount(); i++) {
                for (int z = 0; z < expanded.size(); z++) {
                    if(tree.getPathForRow(i).toString().equals(expanded.get(z).toString())){
                        tree.expandRow(i);
                        expanded.remove(z);
                    }
                }
            }
        }
        scrollpane.setViewportView(tree);
    }

    public void redraw() {
        setBorder(new LineBorder(Theme.getColor("secondaryColor"), 3));
        self.setBackground(Theme.getColor("paneColor"));
        tree.setBackground(self.getBackground());
        if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer)
        {
            final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)(tree.getCellRenderer());
            renderer.setBackgroundNonSelectionColor(self.getBackground());
            renderer.setBackgroundSelectionColor(Theme.getColor("gentleHighlightColor"));
            renderer.setTextNonSelectionColor(Theme.getColor("defaultColor"));
            renderer.setTextSelectionColor(Theme.getColor("defaultColor"));
        }
    }

    private class OpenAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
           if(selectedFile.isFile()) workSpace.loadStory(selectedFile);
        }
    }

    private class NewFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            File home;
            if(selectedFile.isDirectory()){
                home = selectedFile;
            }
            else{
                home = selectedFile.getParentFile();
            }
            if(home!=null){
                System.out.println("s: " + home.getAbsolutePath());
                workSpace.newScript(home);
            }
            else{
                System.out.println("s: " + home.getAbsolutePath());
                workSpace.newScript();
            }
            workSpace.save();
        }
    }

    private class NewFolderAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            File home;
            if(selectedFile.isDirectory()){
                home = selectedFile;
            }
            else{
                home = selectedFile.getParentFile();
            }
            String k = (String)JOptionPane.showInputDialog(
                    self,
                    "Name your Folder: ",
                    "Rye Bard",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
            if(k!=null&&k.length()>0){
               if(home!=null){
                   File folder = new File(home.getAbsolutePath()+"/"+k);
                   System.out.println(folder.getAbsolutePath());
                   folder.mkdir();
                   updateTree();
               }
                else{
                   File folder = new File(home.getAbsolutePath()+"/"+k);
                   System.out.println(folder.getAbsolutePath());
                   folder.mkdir();
                   updateTree();
               }
            }
        }
    }

    private class CopyAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(selectedFile!=null) {
                copiedFile = selectedFile;
                System.out.println("COPIED: " + copiedFile.getAbsolutePath());
            }
            pasteMenuItem.setEnabled(true);
        }
    }

    private class PasteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                System.out.println("PASTING TO: " + selectedFile.getAbsolutePath());
                copyFile(copiedFile, new File(selectedFile.getAbsoluteFile() + "/" + copiedFile.getName()));
                updateTree();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void copyFile(File sourceFile, File destFile) throws IOException {
            if(!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            finally {
                if(source != null) {
                    source.close();
                }
                if(destination != null) {
                    destination.close();
                }
            }
        }
    }

    private class RenameAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            workSpace.rename(selectedFile);
            updateTree();
        }
    }

    private class DeleteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(selectedFile.isFile()) {
                selectedFile.delete();
            }
            if(selectedFile.isDirectory()) {
                deleteDirectory(selectedFile);
                selectedFile.delete();
            }
            updateTree();
        }

        public boolean deleteDirectory(File dir) {
            if(! dir.exists() || !dir.isDirectory())    {
                return false;
            }

            String[] files = dir.list();
            for(int i = 0, len = files.length; i < len; i++)    {
                File f = new File(dir, files[i]);
                if(f.isDirectory()) {
                    deleteDirectory(f);
                }else   {
                    f.delete();
                }
            }
            return dir.delete();
        }
    }

    public File treeToFile(DefaultMutableTreeNode t){
        File result = null;
        if (tree != null && tree.getSelectionModel() != null){
            DefaultMutableTreeNode temp = t;
            if(temp!=tree.getModel().getRoot()){
                String path = "";
                for (int i = temp.getLevel(); i > 0; i--) {
                    path = "/" +
                            temp.toString() + path;
                    temp = (DefaultMutableTreeNode) temp.getParent();
                }
                File file;
                if (path.length()>0) {
                    file = new File(address.getAbsolutePath() + path);
                } else {
                    file = new File(address.getAbsolutePath()+"/"+t.toString());
                }
                result = new File(file.getAbsolutePath());
            }
            else return new File(address.getAbsolutePath());
        }
        return result;
    }

    /** Add nodes from under "dir" into curTop. Highly recursive. */
    private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
        String curPath = dir.getPath();
        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getName());
        if (curTop != null) { // should only be null at root
            curTop.add(curDir);
        }
        Vector ol = new Vector();
        String[] tmp = dir.list();
        for (int i = 0; i < tmp.length; i++){
            ol.addElement(tmp[i]);
        }
        Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
        File f;
        Vector files = new Vector();
        // Make two passes, one for Dirs and one for Files. This is #1.
        for (int i = 0; i < ol.size(); i++) {
            String thisObject = (String) ol.elementAt(i);
            String newPath;
            if (curPath.equals("."))
                newPath = thisObject;
            else
                newPath = curPath + File.separator + thisObject;
            if ((f = new File(newPath)).isDirectory()){
                addNodes(curDir, f);
            }
            
            else{
                files.addElement(thisObject);
            }
            
        }
        // Pass two: for files.
        for (int fnum = 0; fnum < files.size(); fnum++){
            curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
        }
        
        return curDir;
    }
    
}