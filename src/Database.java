import java.io.*;

/**
 * Created by JohnBae on 10/14/15.
 */
public class Database {

    private  static File systemFiles = new File("/Users/" + System.getProperty("user.name") +"/Documents/Rye Bard");
    private  static File saveFiles = new File(systemFiles.getAbsolutePath()+"/Workspace");
    private  static File systemSettings = new File(systemFiles+"/system");

    private  static File recent = null;
    private  static String themeName = "plain";
    private  static boolean spellCheck = true;
    private  static boolean syntaxCheck = true;
    private  static boolean lineWrap = true;

    public static void initialize(){

        loadSystemFiles();

    }

    public static void setRecent(File file){
        recent = file;
    }

    public static  File getRecent(){
        return recent;
    }

    public static void setSystemFiles(File file){
        systemFiles = file;
    }

    public static void setThemeName(String k){
        themeName = k;
    }

    public static void setSpellCheck(boolean k){
        spellCheck = k;
    }

    public static void setSyntaxCheck(boolean k){
        syntaxCheck = k;
    }

    public static void setLineWrap(boolean k){
        lineWrap = k;
    }

    public static void save(File file, String text){
        System.out.println(file.getAbsolutePath());
        if(!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
            writer.write(text);
            writer.close();
        }
        catch ( IOException e)
        {
            System.out.println("File not found");
        }
    }

    public static File getSaveFiles(){
        return saveFiles;
    }

    public static String load(File file){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            String text;
            String result = "";

            while((text = br.readLine()) != null) {
                result += text + System.getProperty("line.separator");
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void loadSystemFiles(){
        try {
            systemFiles.mkdir();
            systemFiles.createNewFile();
            saveFiles.mkdir();
            saveFiles.createNewFile();

            if (systemSettings.exists() && !systemSettings.isDirectory()) {

                BufferedReader br = new BufferedReader(new FileReader(systemSettings));

                String line;

                while((line =  br.readLine())!=null) {
                    String[] temp = line.split(":");
                    if(temp[0].equals("recent")&&!temp[1].equals("NEW")) {
                        recent = new File(temp[1]);
                    }
                    else recent = null;
                    if(temp[0].equals("theme")&&temp.length>1) {
                        themeName = temp[1];
                    }
                    else themeName = "plain";
                    if(temp[0].equals("selfDoc")&&temp.length>1) {
                        String k = temp[1];
                        if(isNumeric(k)){
                            if(Integer.parseInt(k)==0) spellCheck = false;
                            else spellCheck = true;
                        }
                        else spellCheck = true;
                    }
                    else spellCheck = true;
                    if(temp[0].equals("syntaxCheck")&&temp.length>1) {
                        String k = temp[1];
                        if(isNumeric(k)){
                            if(Integer.parseInt(k)==0) syntaxCheck = false;
                            else syntaxCheck = true;
                        }
                        else syntaxCheck = true;
                    }
                    else syntaxCheck = true;
                    if(temp[0].equals("lineWrap")&&temp.length>1) {
                        String k = temp[1];
                        if(isNumeric(k)){
                            if(Integer.parseInt(k)==0) lineWrap = false;
                            else lineWrap = true;
                        }
                        else lineWrap = true;
                    }
                    else lineWrap = true;
                }

            }
            else {
                BufferedWriter writer = null;
                try
                {
                    writer = new BufferedWriter(new FileWriter(systemSettings));
                    writer.write("recent:NEW");
                    writer.newLine();
                    writer.write("theme:plain");
                    writer.newLine();
                    writer.write("selfDoc:1");
                    writer.newLine();
                    writer.write("syntaxCheck:1");
                    writer.newLine();
                    writer.write("lineWrap:1");
                    writer.close( );
                }
                catch ( IOException e)
                {
                    System.out.println("not found");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSystemFiles(){
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(systemSettings));
            if(getRecent()!=null) writer.write("recent:"+ getRecent().getAbsolutePath());
            else writer.write("recent:NEW");
            writer.newLine();
            writer.write("theme:"+themeName);
            writer.newLine();
            writer.write("selfDoc:"+numericalBoolean(spellCheck));
            writer.newLine();
            writer.write("syntaxCheck:"+numericalBoolean(syntaxCheck));
            writer.newLine();
            writer.write("lineWrap:"+numericalBoolean(lineWrap));
            writer.close( );
        }
        catch ( IOException e)
        {
            System.out.println("not found");
        }
    }

    public static int numericalBoolean(boolean k){
        if(k==false) return 0;
        else return 1;
    }

    public static  boolean isNumeric(String str) {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
