package com.future.yanhuang.util.listener;

import com.future.yanhuang.util.SystemUtils;
import com.future.yanhuang.util.zkwatcher.PushOrPull;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**  WEB-INF/config/
 * Created intellij
 * Author: caomm
 * Date: 2016.07.12
 */

@Component
public class InitAppListener implements ServletContextListener {
    private String zk_path;
    private String zkcontext;
    private String configfile;
    private Integer session_timeout;



    // @Autowired
    //ConfigrationService wonfigrationService;
    public void contextInitialized(ServletContextEvent sce) {
        /*WebApplicationContextUtils
                .getRequiredWebApplicationContext(sce.getServletContext())
                .getAutowireCapableBeanFactory()
                .autowireBean("zookeeper");*/
        /*final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        final InitApp props = (InitApp)springContext.getBean("zookeeper");*/
       String path =  sce.getServletContext().getRealPath("WEB-INF/config/zookeeper.properties");

     //   PropertiesUtil.getContextPathProperValue(path)
        Map<String,String> propertyMap = SystemUtils.getContextPathProperValue(path);
        this.setConfigfile(propertyMap.get("configfile").toString());
        this.setSession_timeout(Integer.parseInt(propertyMap.get("session_timeout").toString()));
        this.setZk_path(propertyMap.get("zk_path").toString());
        this.setZkcontext(propertyMap.get("zkcontext").toString());
        System.out.println("----------------初始化参数--------------------");
        PushOrPull pushOrPull = new PushOrPull(zk_path,configfile);
        pushOrPull.createConnection(zkcontext,session_timeout);
        //pushOrPull.deleteAllTestPath();
        if(pushOrPull.createPath(zk_path, "")) {
            pushOrPull.readData(zk_path, true);
        }

       // ThreadUtil.sleep(300000000L);


    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("**************************");
    }


    public void setZk_path(String zk_path) {
        this.zk_path = zk_path;
    }

    public void setZkcontext(String zkcontext) {
        this.zkcontext = zkcontext;
    }

    public void setConfigfile(String configfile) {
        this.configfile = configfile;
    }

    public void setSession_timeout(Integer session_timeout) {
        this.session_timeout = session_timeout;
    }
}
