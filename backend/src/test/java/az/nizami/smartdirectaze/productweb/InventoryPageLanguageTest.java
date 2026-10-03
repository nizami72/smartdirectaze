package az.nizami.smartdirectaze.productweb;

import az.nizami.smartdirectaze.shop.JsonUtil;
import az.nizami.smartdirectaze.shop.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// The "Products and delivery" page renders fully in each interface language, with no untranslated keys
class InventoryPageLanguageTest {

    private String render(String language) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messages);
        // Outside a web request: links like @{/webhooks/inventory/add} get no context path
        engine.setLinkBuilder(new StandardLinkBuilder() {
            @Override
            protected String computeContextPath(IExpressionContext context, String base, Map<String, Object> parameters) {
                return "";
            }
        });

        ProductDTO named = new ProductDTO();
        named.setTitles(new java.util.HashMap<>(Map.of("ru", "Кроссовки")));
        named.setSalePrice(new BigDecimal("89.00"));
        named.setCurrency("AZN");
        named.setIsAvailable(true);
        ProductDTO unnamed = new ProductDTO();
        unnamed.setIsAvailable(false);

        Context context = new Context(Locale.forLanguageTag(language));
        context.setVariable("products", List.of(named, unnamed));
        context.setVariable("shopId", 3L);
        context.setVariable("jsonUtil", new JsonUtil());
        return engine.process("fragments/product-list", Set.of("inventory-content"), context);
    }

    @Test
    void azerbaijani() {
        String html = render("az");
        assertTrue(html.contains("Çatdırılma şərtləri"));
        assertTrue(html.contains("Stokda var"));
        assertTrue(html.contains("Stokda yoxdur"));
        assertTrue(html.contains("Adsız"));
        assertTrue(html.contains("Кроссовки"), "product names stay as the merchant typed them");
        // Script texts are JavaScript-escaped: "ə" is written as \u0259
        assertTrue(html.contains("deleteConfirm: \"Bu m\\u0259hsul silinsin?\""));
        assertFalse(html.contains("??"), "a key without a translation");
        assertFalse(html.replace("Кроссовки", "").matches("(?s).*[А-Яа-яЁё].*"), "Russian text left on the Azerbaijani page");
    }

    @Test
    void russian() {
        String html = render("ru");
        assertTrue(html.contains("Условия доставки"));
        assertTrue(html.contains("В наличии"));
        assertTrue(html.contains("Без названия"));
        assertFalse(html.contains("??"));
    }
}
