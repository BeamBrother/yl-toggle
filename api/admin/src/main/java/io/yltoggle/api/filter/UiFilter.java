package io.yltoggle.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class UiFilter implements Filter {

    private final String passUri = "/static/css/**;/static/js/**;/static/media/**;/api/**;/internal/**;" +
            "/favicon.ico;/asset-manifest.json;/actuator/**;";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request =(HttpServletRequest) servletRequest;
        if (!pass(request.getRequestURI())) {
            servletRequest.getRequestDispatcher("/index.html").forward(servletRequest, servletResponse);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean pass(String uri) {
        AntPathMatcher matcher = new AntPathMatcher();
        for (String  pattern : passUri.split(";")) {
            if (matcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

}
