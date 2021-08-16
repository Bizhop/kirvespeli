package fi.bizhop.jassu.util;

import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.service.AuthService;
import fi.bizhop.jassu.service.UserService;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@Component
public class RequestUserResolver implements HandlerMethodArgumentResolver {
    final AuthService AUTH_SERVICE;
    final UserService USER_SERVICE;

    public RequestUserResolver(AuthService authService, UserService userService) {
        this.AUTH_SERVICE = authService;
        this.USER_SERVICE = userService;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(ParameterUser.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        var request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        return this.authorizeAndAuthenticate(request);
    }

    private User authorizeAndAuthenticate(HttpServletRequest request) throws ResponseStatusException {
        String email = this.AUTH_SERVICE.getEmailFromJWT(request);
        if(email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        User user = this.USER_SERVICE.get(email);
        if(user == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Tunnusta ei löytynyt: %s", email));

        return user;
    }
}
