package com.sakura.boot_init.support.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;

/**
 * 缃戠粶宸ュ叿绫? *
 * @author Sakura
 */
public class NetUtils {

    /**
     * 鑾峰彇瀹㈡埛绔?IP 鍦板潃
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                // 鏍规嵁缃戝崱鍙栨湰鏈洪厤缃殑 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 澶氫釜浠ｇ悊鐨勬儏鍐碉紝绗竴涓狪P涓哄鎴风鐪熷疄IP,澶氫釜IP鎸夌収','鍒嗗壊
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }

}



