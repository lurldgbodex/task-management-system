package task_management_system.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import task_management_system.exception.UnauthorizedException;
import task_management_system.user.entity.User;

@Component
@RequiredArgsConstructor
public class Utils {

    public static User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        } throw new UnauthorizedException("user not authenticated");
    }
}
