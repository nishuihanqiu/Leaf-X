package com.lls.leaf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/************************************
 * NetworkUtils
 * @author liliangshan
 * @date 2019-03-08
 ************************************/
public class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);

    public static String getIP() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            LOGGER.error("get network ip error:" + e.getMessage(), e);
        }
        return ip;
    }


}
