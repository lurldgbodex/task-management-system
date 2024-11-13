package task_management_system.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private FilterChain filterChain;
    private StringWriter responseWriter;
    private Cache<String, AtomicInteger> requestCountsPerIpAddress;

    @BeforeEach
    void setup() throws IOException {
        rateLimitFilter = new RateLimitFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        requestCountsPerIpAddress = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testCacheStoresRequestsByIpAddress() {
        String ipAddress = "192.168.1.1";
        requestCountsPerIpAddress.put(ipAddress, new AtomicInteger(1));

        assertNotNull(requestCountsPerIpAddress.getIfPresent(ipAddress));
        assertEquals(1, Objects.requireNonNull(
                requestCountsPerIpAddress.getIfPresent(ipAddress)).get());
    }

    @Test
    void testCacheExpiresAfterOneMinute() throws InterruptedException {
        String ipAddress = "192.168.1.2";
        requestCountsPerIpAddress.put(ipAddress, new AtomicInteger(1));

        Thread.sleep(61000); // 61 seconds
        assertNull(requestCountsPerIpAddress.getIfPresent(ipAddress));
    }

    @Test
    void testRateLimiter_allowRequestsWithinLimit() throws ServletException, IOException {
        for (int i = 0; i < 50; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }
        verify(filterChain, times(50)).doFilter(request, response);
    }

    @Test
    void testRateLimiter_blocksRequestsExceedingLimit() throws ServletException, IOException {
        for (int i = 0; i < 51; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        responseWriter.flush();
        assertTrue(responseWriter.toString().contains("Too many requests"));
    }
}