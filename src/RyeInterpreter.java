import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JohnBae on 11/7/15.
 */
public class RyeInterpreter {

    private static Map<String,Variable> variables = new HashMap<String,Variable>();
    private static Map<String,StoryBlock> storyBlocks = new HashMap<String,StoryBlock>();
    
    public RyeInterpreter(){
        
        
        
    }
    
    public void addVariable(String name, int value){
        
    }

    public void addVariable(String name, String value){
        Variable variable = new Variable(name,value);
        variables.put(name,variable);
    }
    
    public void addStoryBlock(String name, int start, int end){
        StoryBlock storyBlock = new StoryBlock(name,start,end);
        storyBlocks.put(name,storyBlock);
    }
    
    private class Variable{
        
        private String name;
        private int numericalValue;
        private String stringValue;
        
        public Variable(String n, int num){
            name = n;
            numericalValue = num;
        }

        public Variable(String n, String s){
            name = n;
            stringValue = s;
        }
        
        public void setValue(int num){
            numericalValue = num;
        }
        
        public void setValue(String s){
            stringValue = s;
        }
        
        public int getValue(int n){
            return numericalValue;
        }
        
        public String getValue(String s){
            return stringValue;
        }
    }
    
    private class StoryBlock {
        private String name;
        private int position[];

        private String text;
        private String content;
        private ArrayList<String> parents = new ArrayList<String>();
        private ArrayList<String> children = new ArrayList<String>();
        
        public StoryBlock(String n, int s, int e){
            name = n;
            position[0] = s;
            position[1] = e;
        }
        
        public void addParent(){
            
        }
        
        public void removeParent(){
            
        }
        
        public void addChildren(){
            
        }
        
        public void removeChildren(){
            
        }
        
        public void setName(String t){
            name = t;
        }
        
        public String getName(){
            return name;
        }
        
        public void setText(String t){
            text = t;
        }
        
        public String getText(){
            return text;
        }
        
        public void setContent(String t){
            content = t;
        }
        
        public String getContent(String t){
            return content;
        }
        
        public void setPosition(int s, int e){
            position[0] = s;
            position[1] = e;
        }
        
        public int[] getPosition(){
            return position;
        }
        
    }
    
}
