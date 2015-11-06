
package de.uni_leipzig.asv.toolbox.ChineseWhispers.parameterHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Rocco & seb
 *
 */
public class PropertyLoader {

    private static final String s = System.getProperty("file.separator");
    private static final String dirPrefix = System.getProperty("user.home") + s + "ChineseWhispers";
    private  String propertyFilenamePrefix = dirPrefix + s ; //"CW_Properties.ini";
    private String propertyFilename;
    private Properties props;

    /**
     * Creates new property file.
     * @param fileName The property filename.
     */
    public PropertyLoader(String fileName){
        if(!new File(dirPrefix).exists()){
            System.out.println("Created directory: "+dirPrefix);
            new File(dirPrefix).mkdirs();
        }
        this.propertyFilename=this.propertyFilenamePrefix+fileName;
            props = new Properties();
    } // end PropertyLoader


    /**
     * Setzt einen Proberty-Eintrag
     * @param param Proberty-name
     * @param value Proberty-Wert
     */
    public void setParam(String param,String value) {

        props.setProperty(param, value);
        try
        {
                props.store(new FileOutputStream(new File(propertyFilename)), "ChineseWhispers");
        }
        catch (FileNotFoundException e)
        {
                System.out.println("File not found beim setzen");
        }
        catch (IOException e)
        {
                e.printStackTrace();
        }
    } // end setParam


    /**
     * Reads a property entry
     * @param param Property-name
     * @return Property-value or null if not set
     */
    public String getParam(String param){

        Properties props = new Properties();
        try
        {
                props.load(new FileInputStream(new File(propertyFilename)));


                return (String)props.get(param);
        }
        catch (FileNotFoundException e)
        {
                return null;
        }
        catch (IOException e)
        {
                e.printStackTrace();
        }
        return null;
    } // end getParam


} // end PropertyLoader
