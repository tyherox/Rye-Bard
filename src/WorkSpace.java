import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;


public class WorkSpace extends JFrame {
    
    private File saveAdress = null;

    private JSplitPane splitPane;
    private JPanel contentPane;
    private JPanel topToolbar;
    private JPanel bottomToolBar;
    
    private TabbedPane tabHolder;
    private JLabel wordCount;

    private JCheckBoxMenuItem miniMapMenuItem;
    private JScrollPane graphic;
    private FileWalker fileTree;
    private Compiler compiler;
    
    private boolean mapEnabled;

    /**
     * Launch the application.
     */

    /**
     * Create the frame.
     */
    
    public void settingChange(){
        for(int i = tabHolder.getTabCount()-1;i>=0;i--){
            Component tester = tabHolder.getComponentAt(i);
            if(tester instanceof  TextEditor){
                TextEditor temp = (TextEditor) tester;
                temp.setMapState(mapEnabled);
            }
        }
    }

    public WorkSpace(File f) throws IOException {
        saveAdress = f;
        compiler = new Compiler(f.getAbsolutePath());
        Theme.initialize();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 800, 450);
        setMinimumSize(new Dimension(getWidth() / 4 * 3, 10));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem newProjectMenuItem = new JMenuItem("New Project");
        newProjectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.newProject();
            }
        });
        fileMenu.add(newProjectMenuItem);

        JMenuItem loadProjectMenuItem = new JMenuItem("Load Project");
        loadProjectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.loadProject();
            }
        });
        fileMenu.add(loadProjectMenuItem);
        
        fileMenu.add(new JSeparator());

        JMenuItem newScriptMenuItem = new JMenuItem("New Script");
        newScriptMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newScript();
            }
        });
        fileMenu.add(newScriptMenuItem);

        JMenuItem loadMenuItem = new JMenuItem("Import Script");
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File file = loadChooser();
                if (file != null) {
                    loadStory(file);
                }
            }

            public void filler() {

            }
        });
        fileMenu.add(loadMenuItem);

        fileMenu.add(new JSeparator());

        JMenuItem imageMenuItem = new JMenuItem("Import Image");
        imageMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File file = loadChooser();
                System.out.println("IMAGE: " + saveAdress.getAbsolutePath()+"/Images");
                try {
                    copyFile(file,new File(saveAdress.getAbsolutePath()+"/Images/"+file.getName()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                save();
            }

            private void copyFile(File sourceFile, File destFile)
                    throws IOException {
                if (!sourceFile.exists()) {
                    return;
                }
                if (!destFile.exists()) {
                    destFile.createNewFile();
                }
                FileChannel source = null;
                FileChannel destination = null;
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                if (destination != null && source != null) {
                    destination.transferFrom(source, 0, source.size());
                }
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }

            }

            public void filler() {

            }
        });
        fileMenu.add(imageMenuItem);

        fileMenu.add(new JSeparator());

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        fileMenu.add(saveMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(confirmDialog("Would you like to save before creating a new Story?", "Save")){
                    File file = saveStory();
                    save();
                }
                System.exit(0);
            }
            
            public void filler(){
                
            }
        });
        fileMenu.add(exitMenuItem);

        /*final JMenu projectMenu = new JMenu("Project");
        menuBar.add(projectMenu);

        JMenuItem structureMenuItem = new JMenuItem("Structure");
        structureMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                projectMenu();
            }
        });
        projectMenu.add(structureMenuItem);

        JMenuItem engineMenuItem = new JMenuItem("Engine");
        projectMenu.add(engineMenuItem);

        JMenuItem informationMenuItem = new JMenuItem("Information");
        projectMenu.add(informationMenuItem);

        JMenuItem versionMenuItem = new JMenuItem("Version History");
        projectMenu.add(versionMenuItem);*/

        JMenu optionMenu = new JMenu("Option");
        menuBar.add(optionMenu);

        final JCheckBoxMenuItem inverseMenuItem = new JCheckBoxMenuItem("Inverse");
        inverseMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (inverseMenuItem.isSelected()) Theme.setInverse(true);
                else Theme.setInverse(false);
                
                Theme.drawTheme();
            }
        });
        optionMenu.add(inverseMenuItem);

        miniMapMenuItem = new JCheckBoxMenuItem("Minimap");
        miniMapMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (miniMapMenuItem.isSelected()) mapEnabled = true;
                else mapEnabled = false;

                settingChange();
            }
        });
        optionMenu.add(miniMapMenuItem);

        JMenuItem spellcheckMenuItem = new JMenuItem("Spell Check");
        //optionMenu.add(spellcheckMenuItem);

        JMenuItem syntaxMenuItem = new JMenuItem("Syntax Check");
        //optionMenu.add(syntaxMenuItem);

        JMenu scriptMenu = new JMenu("Script");
        //menuBar.add(scriptMenu);

        JMenuItem checkBugMenuItem = new JMenuItem("Check Bug");
        scriptMenu.add(checkBugMenuItem);

        JMenuItem cleanMenuItem = new JMenuItem("Clean ");
        scriptMenu.add(cleanMenuItem);

        JMenu organizeMenuItem = new JMenu("Organize by..");
        //scriptMenu.add(organizeMenuItem);

        JMenuItem depthMenuItem = new JMenuItem("Depth");
        organizeMenuItem.add(depthMenuItem);

        JMenuItem chronologicalMenuItem = new JMenuItem("Chronological");
        organizeMenuItem.add(chronologicalMenuItem);

        JMenuItem branchCheckMenuItem = new JMenuItem("Branch Check");
        //scriptMenu.add(branchCheckMenuItem);

        JCheckBoxMenuItem storyModeMenuItem = new JCheckBoxMenuItem("Story Mode");
        //scriptMenu.add(storyModeMenuItem);

        contentPane = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Theme.getColor("borderColor"));
                g.fillRect(0, 0, contentPane.getWidth(), contentPane.getHeight());
            }

            public void filler(){

            }
        };
        contentPane.setBorder(null);
        Theme.register(contentPane);
        setContentPane(contentPane);
        contentPane.setLayout(new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("default:grow"),
                ColumnSpec.decode("default:grow"),},
                new RowSpec[]{
                        RowSpec.decode("15dlu"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("15dlu"),}));

        topToolbar = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Theme.getColor("backgroundColor"));
                g.fillRect(0, 0, topToolbar.getWidth(), topToolbar.getHeight());
                g.setColor(Theme.getColor("borderColor"));
                g.drawRect(0,0, topToolbar.getWidth()-1, topToolbar.getHeight()-1);
            }
            
            public void filler(){
                
            }
        };
        Theme.register(topToolbar);
        topToolbar.setLayout(new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("center:5dlu"),
                ColumnSpec.decode("center:15dlu"),
                ColumnSpec.decode("center:1dlu"),
                ColumnSpec.decode("default:grow"),
                ColumnSpec.decode("center:1dlu"),
                ColumnSpec.decode("center:15dlu"),
                ColumnSpec.decode("center:1dlu"),
                ColumnSpec.decode("center:15dlu"), 
                ColumnSpec.decode("center:1dlu"),
                ColumnSpec.decode("center:15dlu"),
                ColumnSpec.decode("center:5dlu"),},
                new RowSpec[]{
                        RowSpec.decode("15dlu"),}));
        contentPane.add(topToolbar, "1, 1, 2, 1, fill, fill");
        
        IconButtons fileButton = new IconButtons("F"){
            BufferedImage icon = ImageIO.read(new FileInputStream("Images/fileIcon.png"));
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.drawImage(icon, 0, 0, getWidth(),getHeight(),null);
                graphics.dispose();
            }
        };
        fileButton.addActionListener(new ActionListener() {
            boolean closed = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!closed) {
                    fileTree.setVisible(false);
                    splitPane.setDividerSize(0);
                    closed = true;
                } else {
                    fileTree.setVisible(true);
                    splitPane.setDividerSize(10);
                    closed = false;
                    splitPane.resetToPreferredSizes();
                }
            }
        });
        Theme.register(fileButton);
        topToolbar.add(fileButton, "2,1");
        
        IconButtons runButton = new IconButtons("R"){
            BufferedImage icon = ImageIO.read(new FileInputStream("Images/runIcon.png"));
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.drawImage(icon, 0, 0, getWidth(),getHeight(),null);
                graphics.dispose();
            }
        };
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                Process proc = null;

                try {
                    String name = compiler.getBuildAddress();
                    String engine = compiler.getEngineAddress();

                    File[] listOfFiles = new File(name).listFiles();
                    String finalScript = "";

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isDirectory()) {
                            if (listOfFiles[i].getName().equals("Script")) {
                                File[] scriptFiles = listOfFiles[i].listFiles();
                                for (int s = 0; s < scriptFiles.length; s++) {
                                    if (!scriptFiles[s].isHidden()) {
                                        if (scriptFiles[s].isFile() && scriptFiles[s].exists()) {
                                            BufferedReader reader = new BufferedReader(new FileReader(scriptFiles[s]
                                                    .getAbsolutePath()));
                                            String text;
                                            while ((text = reader.readLine()) != null) {
                                                finalScript += text + "\n";
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }


                    BufferedWriter writer = null;
                    String build = "";
                    if (new File(compiler.getBuildAddress()).isFile())
                        build = new File(compiler.getBuildAddress()).getParentFile().getAbsolutePath();
                    else build = compiler.getBuildAddress();

                    build += "/result";
                    writer = new BufferedWriter(new FileWriter(build));
                    writer.write(finalScript);
                    writer.close();

                    System.out.println("RUNNING FILE IN: \"" + build + "\"");
                    System.out.println("RUNNING ENGINE IN: \"" + engine + "\"");

                    proc = Runtime.getRuntime().exec("java -jar " + engine + " " + build);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                InputStream in = proc.getInputStream();
                InputStream err = proc.getErrorStream();
            }

            public void filler() {

            }
        });
        Theme.register(runButton);
        topToolbar.add(runButton, "6,1");

        IconButtons buildButton = new IconButtons("B"){
            BufferedImage icon = ImageIO.read(new FileInputStream("Images/buildIcon.png"));
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.drawImage(icon, 0, 0, getWidth(),getHeight(),null);
                graphics.dispose();
            }
        };
        buildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();

                try {
                    String name = compiler.getBuildAddress();
                    String engine = compiler.getEngineAddress();

                    File[] listOfFiles = new File(name).listFiles();
                    String finalScript ="";

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isDirectory()) {
                            if (listOfFiles[i].getName().equals("Script")) {
                                File[] scriptFiles = listOfFiles[i].listFiles();
                                for (int s = 0; s < scriptFiles.length; s++) {
                                    if (!scriptFiles[s].isHidden()) {
                                        if(scriptFiles[s].isFile()&&scriptFiles[s].exists()){
                                            BufferedReader reader = new BufferedReader(new FileReader(scriptFiles[s]
                                                    .getAbsolutePath()));
                                            String text;
                                            while((text = reader.readLine()) != null) {
                                                finalScript+=text+"\n";
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }


                    BufferedWriter writer = null;
                    String build = "";
                    if(new File(compiler.getBuildAddress()).isFile()) build = new File(compiler.getBuildAddress()).getParentFile().getAbsolutePath();
                    else build = compiler.getBuildAddress();

                    build +="/result";
                    writer = new BufferedWriter(new FileWriter(build));
                    writer.write(finalScript);
                    writer.close();

                    //System.out.println("RUNNING FILE IN: \"" + build + "\"");
                    //System.out.println("RUNNING ENGINE IN: \"" + engine + "\"");

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        Theme.register(buildButton);
        topToolbar.add(buildButton, "8,1");

        IconButtons storyButton = new IconButtons("S"){
            BufferedImage icon = ImageIO.read(new FileInputStream("Images/storyIcon.png"));
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.drawImage(icon, 0, 0, getWidth(),getHeight(),null);
                graphics.dispose();
            }
        };
        storyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TextEditor temp = (TextEditor) tabHolder.getCurrentComponent();
                //temp.getMinimap().draw();
            }
        });
        Theme.register(storyButton);
        //topToolbar.add(storyButton, "10,1");
        
        splitPane = new JSplitPane();
        splitPane.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
                final JLabel test = new JLabel("TEST");
                test.setOpaque(false);
                test.setPreferredSize(new Dimension(10, Integer.MAX_VALUE));

                BasicSplitPaneDivider divider = new BasicSplitPaneDivider(this) {
                    public int getDividerSize() {
                        if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                            return test.getPreferredSize().width;
                        }
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Theme.getColor("borderColor"));
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paint(g);
                    }
                };
                divider.setBackground(Color.RED);
                divider.add(test);
                return divider;
            }

            public void filler() {

            }
        });
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        contentPane.add(splitPane, "1, 2, 2, 1, fill, fill");

        graphic = new JScrollPane();
        graphic.getHorizontalScrollBar().setUnitIncrement(10);
        graphic.getVerticalScrollBar().setUnitIncrement(10);
        graphic.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        graphic.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        fileTree = new FileWalker(saveAdress, this);
        fileTree.setPreferredSize(new Dimension(getWidth() / 4, getHeight()));
        Theme.register(fileTree);
        splitPane.setLeftComponent(fileTree);

        ScriptDrawer drawingCanvas = new ScriptDrawer();
        graphic.getViewport().scrollRectToVisible(new Rectangle(3000, 3000, 100, 100));
        drawingCanvas.setBorder(null);
        drawingCanvas.setBackground(new Color(60, 111, 153));
        graphic.setViewportView(drawingCanvas);

        tabHolder = new TabbedPane();
        tabHolder.setBorder(null);
        splitPane.setRightComponent(tabHolder);

        bottomToolBar = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Theme.getColor("backgroundColor"));
                g.fillRect(0, 0, bottomToolBar.getWidth(), bottomToolBar.getHeight());
                g.setColor(Theme.getColor("borderColor"));
                g.drawRect(0,0, bottomToolBar.getWidth()-1, bottomToolBar.getHeight()-1);
            }

            public void filler(){

            }
        };
        bottomToolBar.setLayout(new FormLayout(new ColumnSpec[]{
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("center:15dlu"),
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("pref:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("center:pref:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,},
                new RowSpec[]{
                        RowSpec.decode("default:grow"),}));
        
        contentPane.add(bottomToolBar, "1, 3, 2, 1, fill, fill");
        
        wordCount = new JLabel("TEST");
        wordCount.setFont(Theme.getDefaultFont());
        Theme.register(wordCount);
        bottomToolBar.add(wordCount, "6, 1");
        
        initializeTabs(f);

        pack();
        setVisible(true);
        setTitle(saveAdress.getName());

        setLocationRelativeTo(null);
    }
    
    public void projectMenu(){
        final JDialog project = new JDialog();
        project.setModal(true);
        project.setTitle("Project Structure");
        project.setSize(new Dimension(500, 300));
        project.setLocationRelativeTo(null);
        project.setResizable(false);
        
        JPanel contentPane = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Theme.getColor("backgroundColor"));
                g.fillRect(0, 0, project.getWidth(), project.getHeight());
                g.setColor(Theme.getColor("borderColor"));
                g.drawRect(0,0, project.getWidth()-1, project.getHeight()-1);
            }

            public void filler(){

            }
        };
        contentPane.setLayout(new FormLayout(new ColumnSpec[]{
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("pref:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("center:15dlu"),
                FormFactory.RELATED_GAP_COLSPEC,},
                new RowSpec[]{
                        RowSpec.decode("center:5dlu"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("center:20dlu"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("center:20dlu"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("default:grow"),
                        RowSpec.decode("center:20dlu"),
                        RowSpec.decode("default:grow"),}));
        project.setContentPane(contentPane);
        
        final JLabel sLocation = new JLabel("Script Location:");
        sLocation.setForeground(Theme.getColor("defaultColor"));
        contentPane.add(sLocation, "2,2");
        
        final JTextField sLocField = new JTextField();
        sLocField.setText(compiler.getScriptAddress());
        contentPane.add(sLocField, "2,3");
        
        JButton sLocButton = new JButton(" ... ");
        sLocButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File name = loadChooser();
                if(name!=null) {
                    sLocField.setText(name.getAbsolutePath());
                    compiler.setScriptAddress(name.getAbsolutePath());
                }
            }
            
            public void filler(){
                
            }
        });
        sLocButton.setForeground(Theme.getColor("defaultColor"));
        sLocButton.setBorder(BorderFactory.createLineBorder(Theme.getColor("nullColor"), 1));
        contentPane.add(sLocButton, "4,3");
        
        JLabel imageLocation = new JLabel("Image Location:");
        imageLocation.setForeground(Theme.getColor("defaultColor"));
        contentPane.add(imageLocation, "2,4");

        final JTextField iLocField = new JTextField();
        iLocField.setText(compiler.getImageAddress());
        contentPane.add(iLocField, "2,5");

        JButton iLocButton = new JButton(" ... ");
        iLocButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File name = loadChooser();
                if(name!=null) {
                    iLocField.setText(name.getAbsolutePath());
                    compiler.setImageAddress(name.getAbsolutePath());
                }
            }

            public void filler(){

            }
        });
        iLocButton.setForeground(Theme.getColor("defaultColor"));
        iLocButton.setBorder(BorderFactory.createLineBorder(Theme.getColor("nullColor"), 1));
        contentPane.add(iLocButton, "4,5");

        JLabel buildLocation = new JLabel("Build Location:");
        buildLocation.setForeground(Theme.getColor("defaultColor"));
        contentPane.add(buildLocation, "2,6");

        final JTextField bLocField = new JTextField();
        bLocField.setText(compiler.getBuildAddress());
        contentPane.add(bLocField, "2,7");

        JButton bLocButton = new JButton(" ... ");
        bLocButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File name = loadChooser();
                if(name!=null) {
                    bLocField.setText(name.getAbsolutePath());
                    compiler.setBuildAddress(name.getAbsolutePath());
                }
            }

            public void filler(){

            }
        });
        bLocButton.setForeground(Theme.getColor("defaultColor"));
        bLocButton.setBorder(BorderFactory.createLineBorder(Theme.getColor("nullColor"), 1));
        contentPane.add(bLocButton, "4,7");
        
        project.setVisible(true);
        
    }
    
    public void updateWordCount(String i){
        wordCount.setText(i);
    }
    
    public void rename(File f){
        String name = (String)JOptionPane.showInputDialog(
                this,
                "Type a New Name: ",
                "Rye Bard",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
        
        if(name!=null) {
            File result;
            for(int i = tabHolder.getTabCount()-1;i>=0;i--){
                Component tester = tabHolder.getComponentAt(i);
                if(tester instanceof  TextEditor){
                    TextEditor temp = (TextEditor) tabHolder.getComponentAt(i);
                    if(temp.getAddress().equals(f)){
                        //temp.
                    }
                }
            }
            f.renameTo(new File(f.getParentFile().getAbsolutePath()+"/"+name));
        }
        
    }
    
    public void newScript(){
        String name = newFile();
        if(name!=null) {
            tabHolder.addTab(name, new TextEditor(name, new File(saveAdress.getAbsolutePath() + "/Script" + "/" + 
                    name),this));
        }
        save();
    }

    public void newScript(File f){
        String name = newFile();
        if(name!=null) {
            tabHolder.addTab(name, new TextEditor(name,new File(f+"/"+name),this));
        }
        save();
    }
    
    public void save(){
        File file = new File(saveAdress.getAbsolutePath()+"/Script");
        if(file!=null) {
            for(int i = tabHolder.getTabCount()-1;i>=0;i--){
                Component tester = tabHolder.getComponentAt(i);
                if(tester instanceof  TextEditor){
                    TextEditor temp = (TextEditor) tabHolder.getComponentAt(i);
                    Database.save(temp.getAddress(), temp.getReader().getText());
                }
            }
        }
        fileTree.updateTree();
    }
    
    public void initializeTabs(final File f){

        final WorkSpace k = this;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                File[] listOfFiles = f.listFiles();

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isDirectory()) {
                        if (listOfFiles[i].getName().equals("Script")) {
                            File[] scriptFiles = listOfFiles[i].listFiles();
                            for (int s = 0; s < scriptFiles.length; s++) {
                                if (!scriptFiles[s].isHidden()) {
                                    final String name = scriptFiles[s].getName();
                                    if(scriptFiles[s].isFile()&&scriptFiles[s].exists()){
                                        tabHolder.addTab(name, new TextEditor(name,scriptFiles[s], k));
                                        TextEditor temp = (TextEditor) tabHolder.getCurrentComponent();
                                        temp.getReader().overrideText(Database.load(scriptFiles[s]));   
                                    }
                                }

                            }
                        } else if (listOfFiles[i].getName().equals("Pictures")) {
                            File[] pictureFiles = f.listFiles();
                            for (int p = 0; p<pictureFiles.length;p++){
                            }

                        }
                    }
                }

                //tabHolder.addTab("Layout", graphic);
                
                mapEnabled = false;
                miniMapMenuItem.setSelected(false);
                
                tabHolder.revalidate();
                tabHolder.repaint();
                
                settingChange();
            }

        });
        t.start();

    }
    
    public String newFile(){
        String k = (String)JOptionPane.showInputDialog(
                this,
                "Name your File: ",
                "Rye Bard",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
       return k;
    }

    public boolean confirmDialog(String content, String title){
        int n = JOptionPane.showConfirmDialog(this, content
                , title, JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(n==JOptionPane.NO_OPTION) {
           return false;
        }
        else{
            return true;
        }
    }

    public File saveStory(){
        JFileChooser fc = new JFileChooser(Database.getSaveFiles());
        int returnVal = fc.showDialog(this,
                "Save");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return file;
        }
        return null;
    }

    public void loadStory(File f){
        String name = f.getName();
        tabHolder.addTab(name, new TextEditor(name,f,this));
        TextEditor temp = (TextEditor) tabHolder.getCurrentComponent();
        temp.getReader().overrideText(Database.load(f));
        repaint();
    }
    
    public File loadChooser(){
        JFileChooser fc = new JFileChooser(Database.getSaveFiles()){
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                // intercept the dialog created by JFileChooser
                JDialog dialog = super.createDialog(null);
                dialog.setModal(true);  // set modality (or setModalityType)
                return dialog;
            }
        };
        int returnVal = fc.showDialog(this,
                "Load");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return file;
        }
        return null;
    }
    
    class TabbedPane extends JPanel{
        
        private JPanel tabBar;
        private JPanel view;
        private JPanel drag = new JPanel();
        
        private Tab previousTab;
        private Tab selectedTab;
        private JPanel glass = new JPanel();
        
        private int xDrag = 0;
        private int startDrag = 0;

        CardLayout card = new CardLayout(){
            @Override
            public void show(Container c, String n){
                super.show(c, n);
                for(Component temp : tabBar.getComponents()){
                    if(n.equals(temp.getName())){
                        previousTab = selectedTab;
                        if(previousTab!=null) previousTab.deselect();
                        selectedTab = (Tab) temp;
                        selectedTab.select();
                        repaint();
                    }
                }
            }
            
            public void filler(){
                
            }
        };
        
        public TabbedPane(){
            setLayout(new FormLayout(new ColumnSpec[]{
                    ColumnSpec.decode("default:grow"),},
                    new RowSpec[]{
                            RowSpec.decode("15dlu"),
                            RowSpec.decode("3dlu"),
                            RowSpec.decode("default:grow"),}));
            
            tabBar = new JPanel(){
                @Override
                public void paintComponent(Graphics g){
                    super.paintComponent(g);
                    g.setColor(Theme.getColor("backgroundColor"));
                    g.fillRect(0, 0, tabBar.getWidth(), tabBar.getHeight());
                    g.setColor(Theme.getColor("borderColor"));
                    g.drawRect(0,0,tabBar.getWidth()-1, tabBar.getHeight()-1);
                }

                public void filler(){

                }
            };
            tabBar.setLayout(new BoxLayout(tabBar, BoxLayout.X_AXIS));
            add(tabBar, "1, 1, fill, fill");
            
            JPanel seperator = new JPanel(){
                @Override
                public void paintComponent(Graphics g){
                    g.setColor(Theme.getColor("backgroundColor"));
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());
                }
                
                public void filler(){
                    
                }
            };
            seperator.setBorder(null);
            add(seperator, "1,2,fill,fill");

            view = new JPanel(){
                @Override
                public void paintComponent(Graphics g){
                    super.paintComponent(g);
                    g.setColor(Theme.getColor("backgroundColor"));
                    g.fillRect(0, 0, view.getWidth(), view.getHeight());
                    g.setColor(Theme.getColor("borderColor"));
                    g.drawRect(0,0,view.getWidth()-1, view.getHeight()-1);
                }

                public void filler(){

                }
            };
            view.setLayout(card);
            add(view, "1, 3, fill, fill");

            Theme.register(this);
            setGlassPane(glass);
        }

        public void renameTab(Tab t, String name){
            t.renameTab(name);
            repaint();
        }
            
        public void addTab(final String name, final JComponent c){
            
            final Tab addedTab = new Tab(c,name);
            boolean exists = false;
            for(Component temp : tabBar.getComponents()){
                if(temp instanceof Tab) 
                    if(addedTab.getName().equals(temp.getName())) {
                        card.show(view,name);
                        exists = true;
                    }
            }
            if(exists==false){
                tabBar.add(addedTab);

                view.add(c, name);
                addedTab.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent componentEvent) {
                        super.componentHidden(componentEvent);
                        removeTab(addedTab);
                    }

                    public void filler() {

                    }
                });
                addedTab.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        super.mousePressed(mouseEvent);
                        card.show(view, name);

                        Point location = new Point(mouseEvent.getComponent().getLocation().x + mouseEvent.getPoint().x, 0);
                        if (tabBar.getComponentAt(location) instanceof Tab) {
                            Tab compare = (Tab) tabBar.getComponentAt(location);
                            drag.setBackground(Theme.getColor("highlightColor"));
                            
                            xDrag = mouseEvent.getXOnScreen();
                            startDrag = compare.getLocationOnScreen().x;
                            
                            glass.add(drag);
                            glass.setOpaque(false);
                            glass.setVisible(true);
                            glass.setLayout(null);
                            
                            drag.setSize(compare.getWidth(), compare.getHeight());
                            drag.setLocation(startDrag - (xDrag - mouseEvent.getXOnScreen()) - contentPane.getLocationOnScreen().x, drag.getHeight());
                        }
                        
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        super.mouseReleased(mouseEvent);
                        card.show(view, name);
                        xDrag = -1;
                        startDrag = -1;
                        glass.setVisible(false);
                        glass.setLayout(null);
                        glass.remove(drag);
                    }
                });
                addedTab.addMouseMotionListener(new MouseAdapter() {
                    int order = -2;
                    Tab compare;
                    boolean dragStart = false;

                    @Override
                    public void mouseDragged(MouseEvent mouseEvent) {
                        super.mouseDragged(mouseEvent);

                        Point location = new Point(mouseEvent.getComponent().getLocation().x + mouseEvent.getPoint().x, 0);

                        if (compare != null && compare.equals(tabBar.getComponentAt(location))) {
                            
                            drag.setLocation(startDrag - (xDrag - mouseEvent.getXOnScreen()) - contentPane.getLocationOnScreen().x, drag.getHeight());
                            
                        } else {
                            if (tabBar.getComponentAt(location) instanceof Tab)
                                compare = (Tab) tabBar.getComponentAt(location);
                            if (order != tabBar.getComponentZOrder(compare)) {
                                order = tabBar.getComponentZOrder(compare);
                                if (order != -1) {
                                    tabBar.setComponentZOrder(addedTab, order);
                                    tabBar.revalidate();
                                    tabBar.repaint();
                                }
                            }
                        }

                    }
                });
                view.revalidate();

                card.show(view, name);
            }
        }

        public void removeTab(Tab c){
            Tab previous = null;
            for(Component temp : tabBar.getComponents()){
                Tab tab = (Tab) temp;
                if(tab.equals(c) && previous != null){
                    card.show(view,previous.getName());
                    previous.select();
                }
                previous = tab;
            }
            tabBar.remove(c);
            view.remove(c.getSubstance());
            
        }
        
        public int getTabCount(){
            return tabBar.getComponentCount();
        }

        public Component getCurrentComponent(){
            return selectedTab.getSubstance();
        }

        public Component getComponentAt(int i){
            int threshhold = tabBar.getComponentCount();
            Tab t = (Tab) tabBar.getComponent(i);
            if(threshhold-1<i||!(t instanceof Tab)){
                return null;
            }
            return t.getSubstance();
        }
        
        public void showComponent(int i){
            int threshhold = tabBar.getComponents().length;
            if(threshhold-1>=i){
                Component[] c = tabBar.getComponents();
                Tab test = (Tab) c[threshhold];
                card.show(view,test.getName());
            }
        }
        
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            for(Component temp : tabBar.getComponents()){
                if(temp instanceof Tab) ((Tab) temp).redraw();
            }
        }
        
        private class Tab extends JPanel{
            
           JComponent substance;
            
            JLabel name;
            JLabel exit;
            
            boolean dragged = false;
            
            boolean selected = false;
            
            public Tab(JComponent c, String n){
                substance = c;

                Theme.register(this);
                setBorder(BorderFactory.createLineBorder(Theme.getColor("borderColor")));
                setLayout(new FormLayout(new ColumnSpec[]{
                        ColumnSpec.decode("default:grow"),
                        FormFactory.RELATED_GAP_COLSPEC,
                        ColumnSpec.decode("10dlu"),},
                        new RowSpec[]{
                                RowSpec.decode("default:grow"),}));
                setBackground(Color.WHITE);
                
                name = new JLabel();
                name.setHorizontalAlignment(SwingConstants.CENTER);
                name.setFont(Theme.getDefaultFont().deriveFont(12f));
                name.setText(n);
                add(name, "1,1");
                
                exit = new JLabel("x");
                exit.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {
                        super.mouseExited(mouseEvent);
                        exit.setForeground(Theme.getColor("borderColor"));
                    }

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {
                        super.mouseEntered(mouseEvent);
                        exit.setForeground(Theme.getColor("defaultColor"));
                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {
                        super.mouseReleased(mouseEvent);
                        setVisible(false);
                        
                    }
                });
                exit.setForeground(Theme.getColor("borderColor"));
                exit.setHorizontalAlignment(SwingConstants.CENTER);
                add(exit,"3,1");
            }
            
            public void select(){
                selected = true;
                redraw();
            }

            public void deselect(){
                selected = false;
                redraw();
            }
            
            public String getName(){
                return name.getText();
            }
            
            public void renameTab(String n){
                name.setText(n);
                redraw();
                repaint();
            }
            
            public JComponent getSubstance(){
                return substance;
            }

            public void redraw(){
                name.setForeground(Theme.getColor("defaultColor"));
                exit.setForeground(Theme.getColor("borderColor"));
                setBorder(BorderFactory.createLineBorder(Theme.getColor("borderColor")));
                
                if(selected) {
                    setBackground(Theme.getColor("backgroundColor"));
                }
                else {
                    setBackground(Theme.getColor("secondaryColor"));
                }
            }
        }
    }
}
