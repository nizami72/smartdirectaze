package az.nizami.smartdirectaze.business;

import java.util.UUID;

/**
 * Implemented by modules whose entities belong to a Business (shops, events),
 * so the business module can count them without depending on those modules.
 */
public interface BusinessChildCounter {

    Industry industry();

    long countByBusiness(UUID businessId);
}
