package loris.parfume.Configurations.Loggings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        filterChain.doFilter(wrappedRequest, response);

        String clientIp = request.getRemoteAddr();
        String requestBody = extractBody(wrappedRequest);
        logger.info("Request from IP: {}, Path: {}, Body: {}", clientIp, request.getRequestURI(), requestBody);
    }

    private String extractBody(ContentCachingRequestWrapper request) {

        byte[] buf = request.getContentAsByteArray();

        if (buf.length > 0) {

            try {

                int length = Math.min(buf.length, 1024);

                return new String(buf, 0, length, request.getCharacterEncoding());
            }
            catch (UnsupportedEncodingException ex) {

                logger.error("Error reading request body", ex);
            }
        }

        return "";
    }
}