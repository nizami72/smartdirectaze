package az.nizami.smartdirectaze.productweb;

import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.productweb.controller.InventoryController;
import az.nizami.smartdirectaze.productweb.controller.TelegramAuthService;
import az.nizami.smartdirectaze.shop.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryControllerTest {

    private static final Long SHOP_ID = 4L;
    private static final Long PRODUCT_ID = 10L;

    private final ProductService productService = mock(ProductService.class);
    private final TelegramAuthService telegramAuthService = mock(TelegramAuthService.class);
    private final UserService userService = mock(UserService.class);
    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(productService, telegramAuthService, userService, "bot-token");
        when(userService.findByEmail("owner@example.com")).thenReturn(Optional.of(UserDto.builder().id(3L).build()));
        when(userService.findByEmail("other@example.com")).thenReturn(Optional.of(UserDto.builder().id(99L).build()));
        when(productService.isShopBelongToUser(SHOP_ID, 3L)).thenReturn(true);
        when(productService.isShopBelongToUser(SHOP_ID, 555L)).thenReturn(true);
    }

    private UserDetails user(String email) {
        return new User(email, "x", List.of());
    }

    private HttpStatus statusOf(Runnable call) {
        return HttpStatus.valueOf(assertThrows(ResponseStatusException.class, call::run).getStatusCode().value());
    }

    @Test
    void delete_WithoutAuth_ShouldReturn401() {
        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(() -> controller.deleteProduct(SHOP_ID, null, null, PRODUCT_ID)));
        verify(productService, never()).deleteProduct(anyLong(), anyLong());
    }

    @Test
    void delete_ForeignShop_ShouldReturn404() {
        assertEquals(HttpStatus.NOT_FOUND, statusOf(() -> controller.deleteProduct(SHOP_ID, user("other@example.com"), null, PRODUCT_ID)));
        verify(productService, never()).deleteProduct(anyLong(), anyLong());
    }

    @Test
    void delete_Owner_ShouldDelete() {
        controller.deleteProduct(SHOP_ID, user("owner@example.com"), null, PRODUCT_ID);
        verify(productService).deleteProduct(SHOP_ID, PRODUCT_ID);
    }

    @Test
    void delete_TelegramOwnerWithValidInitData_ShouldDelete() {
        when(telegramAuthService.isValid("signed", "bot-token")).thenReturn(true);
        when(telegramAuthService.getUserIdFromInitData("signed")).thenReturn(555L);

        controller.deleteProduct(SHOP_ID, null, "signed", PRODUCT_ID);
        verify(productService).deleteProduct(SHOP_ID, PRODUCT_ID);
    }

    @Test
    void delete_ForgedInitData_ShouldReturn401() {
        when(telegramAuthService.isValid("forged", "bot-token")).thenReturn(false);

        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(() -> controller.deleteProduct(SHOP_ID, null, "forged", PRODUCT_ID)));
        verify(productService, never()).deleteProduct(anyLong(), anyLong());
    }
}
