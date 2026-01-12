package com.chat.core.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author y
 * @since 2026-01-07
 */
public class CommonUtils {

    /**
     * 获取url参数
     *
     * @param url 路径
     * @return 用户ID
     */
    public static Integer getUrlParams(String url) {
        if (!url.contains("=")) {
            return null;
        }

        return Integer.parseInt(url.substring(url.indexOf("=") + 1));
    }


    /**
     * 获取本机 ip
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取本机 ip 失败");
        }
    }

}
