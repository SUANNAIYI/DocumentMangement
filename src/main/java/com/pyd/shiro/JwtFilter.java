package com.pyd.shiro;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.pyd.common.Result;
import com.pyd.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtFilter extends AuthenticatingFilter {

    @Autowired
    JwtUtils jwtUtils;

    /* 这里设置不被拦截的请求路径 */
    private static final String[] excludePatterns = new String[]{
            "/swagger-resources", "/webjars", "/v2", "/swagger-ui.html", "/api", "/api-docs", "/doc.html",
            "/web/ws", "/user/login", "/user/register", "/static", "/upload/download", "/docs/saveFile"
    };

    //若请求头中不带jwt则产生jwt token
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if(!StringUtils.hasText(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }


    //是否有权限操作
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue){
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        /* 判断请求路径是否为不拦截的请求路径 */
        String url = request.getRequestURI();
        if(url.equals("") || url.equals("/")){
            return true;
        }
        for(String s: excludePatterns) {
            if(url.startsWith(s) || url.startsWith("/insight-1.0" + s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        // 提示登录返回jwt
        if (jwt == null){
            HttpServletResponse httpReponse= (HttpServletResponse) servletResponse;
            httpReponse.setHeader("Access-Control-Allow-Credentials","true");
            httpReponse.setContentType("text/html; charset=UTF-8");
            Map<String, Object> info = new HashMap<>();
            info.put("code", HttpStatus.HTTP_UNAUTHORIZED);
            info.put("msg", "请先登录");
            httpReponse.getWriter().print(JSON.toJSONString(info));
            httpReponse.setStatus(401);
            return false;
        }
        // 校验jwt
        jwt = jwt.replace("Bearer ", "");
        Claims claim = jwtUtils.getClaimByToken(jwt);//获取jwt信息
        if(claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
//                request.getRequestDispatcher("/unauthorized").forward(servletRequest, servletResponse);
//                throw new ExpiredCredentialsException("token已失效，请重新登录");
            HttpServletResponse httpReponse= (HttpServletResponse) servletResponse;
            httpReponse.setHeader("Access-Control-Allow-Credentials","true");
            httpReponse.setContentType("text/html; charset=UTF-8");
            Map<String, Object> info = new HashMap<>();
            info.put("code", HttpStatus.HTTP_UNAUTHORIZED);
            info.put("msg", "token已失效，请重新登录");
            httpReponse.getWriter().print(JSON.toJSONString(info));
            httpReponse.setStatus(401);
            return false;
        }
        // 执行登录
        return executeLogin(servletRequest, servletResponse);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Throwable throwable = e.getCause() == null ? e : e.getCause();
        Result result = Result.fail(throwable.getMessage());
        String json = JSONUtil.toJsonStr(result);
        try {
            httpServletResponse.getWriter().print(json);
        } catch (IOException ioException) {
        }
        return false;
    }


    //跨域支持
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
