package common.toolkit.java.util;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created intellij
 * Author: caomm
 * Email: cao_manman@126.com
 * Date: 2016.07.20
 */
public class PropertyUtils {
    private static Properties properties = new Properties();
    private static FileOutputStream outStream = null;
    private static InputStream inputStream = null;

    public static void setPropertyValue(Map<String ,String> proMap, String propertyFile ){


        File file = new File(PropertyUtils.class.getClassLoader().getResource(".").getPath()+File.separator+propertyFile);
        //FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            for (String str:proMap.keySet()) {
                properties.setProperty(StringUtil.trimToEmpty(str),StringUtil.trimToEmpty( proMap.get(str)));
            }
            properties.store(outStream, "ourfuture full " + new Date().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /*public static void main(String []args){
        Map<String,String > map = new HashMap<String, String>();
        map.put("code.test","code22222");
        map.put("code.demo","demo22222");
        map.put("code.manager","manager22222");
        map.put("jdbc.url","192.168.9.6:3306");

        setPropertyValue(map,"system.properties");
    }*/
}
