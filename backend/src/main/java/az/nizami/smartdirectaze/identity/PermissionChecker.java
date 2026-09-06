package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Component for checking user permissions and roles within the application.
 * <p>
 * This class provides utility methods to verify if an authenticated user
 * has specific roles or permissions, often used with Spring Security's
 * {@code @PreAuthorize} annotations.
 * </p>
 */
@Component("authz")
@Log4j2
public class PermissionChecker {

    private final UserService userService;

    public PermissionChecker(UserService userService) {
        this.userService = userService;
    }


    /**
     * Checks if the authenticated user has the 'SUPER_ADMIN' role.
     *
     * @param auth The current {@link Authentication} object.
     * @return {@code true} if the user is a super admin, {@code false} otherwise.
     */
    public boolean isSuperAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));
    }

    /**
     * Checks if the authenticated user has any of the specified roles.
     * <p>
     * If the user is a 'SUPER_ADMIN', this method immediately returns {@code true},
     * bypassing further role checks.
     * </p>
     *
     * @param auth  The current {@link Authentication} object.
     * @param roles A variable number of role names (e.g., "EATERY_ADMIN", "WAITER").
     * @return {@code true} if the user has any of the specified roles or is a super admin,
     *         {@code false} otherwise.
     */
    public boolean hasAnyRole(Authentication auth, String... roles) {
        if (isSuperAdmin(auth)) return true;

        for (String role : roles) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * <h3>
     * Checks if the authenticated user has any of the specified roles and access to a specified eatery.
     * </h3>
     *
     * @param auth  The current {@link Authentication} object.
     * @param roles A variable number of role names (e.g., "EATERY_ADMIN", "WAITER").
     * @param eateryId An eateryId the user wants to have access to.
     * @return {@code true} if the user has a specified role and access the specified eatery {@code false} otherwise.
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRoleAndAccess(Authentication auth, Long eateryId, String... roles) {
//        if (isSuperAdmin(auth)) return true;
//        boolean r = hasAnyRole(auth, roles);
//        if (!r) return false;
//
//        UserDto user = (UserDto) auth.getPrincipal();
//        Optional<UserProfile> userProfileOptional = userService.findProfileByUserWithEateries(user);
//
//        if (userProfileOptional.isEmpty()) return false;
//
//        List<Long> eateryList = userProfileOptional.get().getEateries()
//                .stream()
//                .map(Eatery::getId)
//                .toList();
//        boolean b = eateryList.contains(eateryId);
//        if(!b) log.warn("Eatery list {} doesnt contain specified eatery id: [{}]", eateryList, eateryId);
//
        return false;
    }


}
