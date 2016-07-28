package com.future.yanhuang.util.filter;

import com.future.yanhuang.util.SystemUtils;
import common.toolkit.java.util.PropertiesUtil;
import sun.misc.BASE64Decoder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created intellij
 * Author: caomm
 * Date: 2016.07.04
 */
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse res = (HttpServletResponse)servletResponse;
        String []osname = System.getProperties().getProperty("os.name").toLowerCase().split(" ");
        String mac = null;
        InetAddress ia = SystemUtils.getIpAddress();
        if(osname[0].equals("windows")){
            mac = SystemUtils.getWinLocalMac(ia);
        }else{
            mac = SystemUtils.getLocalMac(ia);
        }


        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String nowdate = df.format(new Date());// new Date()为获取当前系统时间
        Date date = null;
        try {
            date= df.parse( nowdate );
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String encipher = PropertiesUtil.getPropertyValue("system.properties","encipher.code");
        //String encipher = PropertyUtils.getValue("encipher.code").get();
        String ret = null;
        try {
            ret = new String(new BASE64Decoder().decodeBuffer(encipher));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String [] temp = ret.split("\\|");
        String timeString = null;
        String macstring = null;
        String ipstring = null;
        if (temp.length>=3)  timeString = temp[2];
        if (temp.length>=1)  macstring = temp[0];
        if (temp.length>=2)  ipstring = temp[1];

        Date date1 = null;
        try {
            if (timeString != null) date1 = df.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //启用mac地址校验
        if(macstring.equals(mac)&& SystemUtils.compareTodate(date,date1)&&ia.getHostAddress().equals(ipstring)){
        //if( SystemUtils.compareTodate(date,date1)&&ia.getHostAddress().equals(ipstring)){

            filterChain.doFilter(req,res);
        }else{
            String path = req.getContextPath();
            String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+path;

            res.sendRedirect(basePath+"/unauthorizedfield");
            //res.sendRedirect("/WEB-INF/jsp/unauthorized.jsp");
        }

       // destroy();


     //   res.sendRedirect("index.jsp");

    }

    @Override
    public void destroy() {
        System.out.println("********************销毁！*******");

    }
}
