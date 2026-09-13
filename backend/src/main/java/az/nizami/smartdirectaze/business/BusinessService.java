package az.nizami.smartdirectaze.business;

import az.nizami.smartdirectaze.business.dto.ChosenBusinessDto;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;

import java.util.List;

public interface BusinessService {

    /**
     * Выбор типа бизнеса. Переиспользует существующий Business пользователя
     * в этой индустрии (без дублей) и возвращает его id.
     */
    ChosenBusinessDto chooseBusinessType(String email, String businessType);

    /**
     * Сводка бизнесов пользователя по индустриям с РЕАЛЬНЫМ количеством
     * дочерних сущностей (магазины/ивенты), а не числом строк Business.
     */
    List<IndustrySummaryDto> getMyBusinesses(String email);
}
