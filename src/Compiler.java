/**
 * Created by JohnBae on 11/30/15.
 */
public class Compiler {
    
    private String address;
    private String engineVersion = "EMPTY";
    private String engineAddress = "Engine/Engine.jar";
    private String scriptAddress = address+"/Script";
    private String buildAddress = address;
    private String imageAddress = address+"/Image";
    
    public Compiler(String addr){
        address = addr;
        scriptAddress = address+"/Script";
        buildAddress = address;
        imageAddress = address+"/Image";
    }
    
    public void setEngine(String address){
        engineAddress = address;
        
    }
    public String getEngineVersion(){
        return engineVersion;
    }
    
    public void setScriptAddress(String address){
        scriptAddress = address;
    }
    
    public String getScriptAddress(){
        return scriptAddress;
    }
    
    public void setBuildAddress(String address){
        buildAddress = address;
    }
    
    public String getBuildAddress(){
        return buildAddress;
    }
    
    public void setImageAddress(String address){
        imageAddress = address;
    }
    
    public String getImageAddress(){
        return imageAddress;
    }

    public String getEngineAddress() {
        return engineAddress;
    }
}
