package common.toolkit.java.util;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
/**
 * Created intellij
 * Author: caomm
 * Email: cao_manman@126.com
 * Date: 2016.07.12
 */
public class PropertiesUtil {

	private static Properties properties = new Properties();
	private static FileOutputStream outStream = null;
	private static InputStream inputStream = null;

	/**
	 * properties 根据key  set value (数据源、授权)
	 * @param proMap
	 * @param propertyFile
     */

	public static void setPropertyValue(Map<String ,String> proMap,String propertyFile ){


		File file = new File(PropertiesUtil.class.getClassLoader().getResource(".").getPath()+File.separator+propertyFile);
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

	/**
	 * 根据properties 文件key值获取value
	 * @param configfilename
	 * @param key
     * @return
     */

	public static String getPropertyValue(String configfilename,String key){
		//读取classpath 下的属性文件
		File file = new File(PropertiesUtil.class.getClassLoader().getResource(".").getPath()+File.separator+configfilename);
        String value = null;
		try {
			inputStream = new FileInputStream(file);
			properties.load(inputStream);
			value = properties.getProperty(key);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}



}
