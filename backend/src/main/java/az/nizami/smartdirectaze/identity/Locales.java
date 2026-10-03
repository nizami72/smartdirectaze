package az.nizami.smartdirectaze.identity;

import java.util.Set;

/**
 * Interface languages of SmartDirect. Azerbaijani is the default: most Baku merchants read it first.
 */
public final class Locales {

    public static final String AZ = "az";
    public static final String RU = "ru";
    public static final String DEFAULT = AZ;
    private static final Set<String> SUPPORTED = Set.of(AZ, RU);

    private Locales() {
    }

    /** The given language if supported, otherwise the default */
    public static String supported(String locale) {
        return locale != null && SUPPORTED.contains(locale.trim().toLowerCase()) ? locale.trim().toLowerCase() : DEFAULT;
    }
}
