package com.example.demo.interceptor;

import com.example.demo.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class MonitoringInterceptor implements HandlerInterceptor {

    ThreadLocal<StopWatch> stopWatchLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StopWatch stopWatch = new StopWatch(handler.toString());
        stopWatch.start(handler.toString());
        stopWatchLocal.set(stopWatch);

        String nickname = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            nickname = userDetails.getNickname();
        }

        log.info("User: {}", nickname);
        log.info("URL: {}", getURLPath(request));
        log.info("RequestStart: {}", getCurrentTime());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        log.info("RequestEnd: {}", getCurrentTime());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        StopWatch stopWatch = stopWatchLocal.get();
        if (stopWatch != null) {
            stopWatch.stop();
            log.info("RequestTotal: {}ms", stopWatch.getTotalTimeMillis());
            stopWatchLocal.remove();
        }
        log.info("");
    }

    private String getURLPath(HttpServletRequest request) {
        String query = request.getQueryString();
        return request.getRequestURI() + (query != null ? "?" + query : "");
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }
}
