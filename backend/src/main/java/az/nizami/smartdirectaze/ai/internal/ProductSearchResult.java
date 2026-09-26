package az.nizami.smartdirectaze.ai.internal;

import az.nizami.smartdirectaze.shop.ProductDTO;

import java.util.List;

/**
 * What searchProduct gives the model.
 *
 * @param exactMatch false: nothing matched the words of the query, products is the shop's catalog
 *                   and the model picks by meaning (e.g. "pulqabı" asked, "Кошелёк" in the catalog)
 */
record ProductSearchResult(boolean exactMatch, String note, List<ProductDTO> products) {
}
