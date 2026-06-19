package com.common.auth.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    //----- Constants -----//
    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };
    
    public static InetAddress getClientIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        
        for (String header : IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(header);
            if (StringUtils.hasText(headerValue) && !"unknown".equalsIgnoreCase(headerValue)) {
                ipAddress = headerValue.split(",")[0].trim();
                break;
            }
        }
        
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

}