package task_management_system.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import task_management_system.exception.TooManyRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Order(2)
@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUEST_PER_MINUTE = 50;

    private final Cache<String, AtomicInteger> requestCountsPerIpAddress = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String clientIpAddress = httpServletRequest.getRemoteAddr();

        AtomicInteger requestCount = null;
        try {
            requestCount = requestCountsPerIpAddress.get(clientIpAddress, AtomicInteger::new);
            if (requestCount.incrementAndGet() > MAX_REQUEST_PER_MINUTE) {
                httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpServletResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        } catch (ExecutionException ex) {
            throw new TooManyRequest(ex.getMessage());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
