package zookeeper.configStation.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Common {
	// 定义log4j的日志实例
	private static Logger LOG = Logger.getLogger(Common.class);
	
	/** 
	 * 读取文件所有行 
	 * @param  filename    文件名 
	 * @return 文件所有行 
	 */  
	public static List<String> readLines(String filename) {
		File file = new File(filename);
		BufferedReader reader = null;
		List<String> lines = new ArrayList<String>();
		String line = null;
		try {
			//读取一行文件
			LOG.info("readFirstLine:" + filename);
			reader = new BufferedReader(new FileReader(file));
			//一次读入一行
			while((line = reader.readLine()) != null) {
				lines.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return lines;
	}
	
	/**
	 * 读取配置文件
	 * @param filename
	 * @return
	 */
	public static Map<String, String> readConfig(String filename, boolean lowercase) {
		Map<String, String> m = new HashMap<String, String>();
		for(String line : readLines(filename)) {
			line = line.trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			String[] kv = line.split("=", 2);
			if(kv.length != 2) continue;
			if(lowercase) m.put(kv[0].trim().toLowerCase(), kv[1].trim());
			else m.put(kv[0].trim(), kv[1].trim());
		}
		return m;
	}
	
	// 加密  
    public static String encodeBase64(String str) {  
        byte[] b = null;  
        String s = null;  
        try {  
            b = str.getBytes("utf-8");  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
        if (b != null) {  
            s = new BASE64Encoder().encode(b);  
        }  
        return s;  
    }  
  
    // 解密  
    public static String decodeBase64(String s) {  
        byte[] b = null;  
        String result = null;  
        if (s != null) {  
            BASE64Decoder decoder = new BASE64Decoder();  
            try {  
                b = decoder.decodeBuffer(s);  
                result = new String(b, "utf-8");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return result;  
    }  
    
    public static int getFlagNum(String line)
    {
    	int num = 0;
    	byte[] data = line.getBytes();
    	
    	for(int i=0; i<data.length; i++)
    	{
    		if(data[i] == '#')
    		{
    			num++;
    		}
    		else
    		{
    			break;
    		}
    	}
    	
    	return num;
    }
}
