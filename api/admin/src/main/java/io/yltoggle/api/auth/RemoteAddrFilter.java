package io.yltoggle.api.auth;

import io.yltoggle.api.base.tenant.TenantContext;
import io.yltoggle.api.utils.IpUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RemoteAddrFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String remoteAddr = IpUtil.getRemoteIp(request);
        if (StringUtils.isNotBlank(remoteAddr)) {
            TenantContext.setCurrentRemoteAddr(remoteAddr);
        }
        filterChain.doFilter(request, response);
    }

}
