package spring.controller;

import org.springframework.security.core.Authentication;
import spring.entity.CustomUserDetails;

public abstract class BaseController {
    protected Integer userId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }
}
