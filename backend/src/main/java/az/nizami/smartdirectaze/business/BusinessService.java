package az.nizami.smartdirectaze.business;

import az.nizami.smartdirectaze.identity.UserDto;

public interface BusinessService {
    UserDto chooseBusinessType(String email, String businessType);
}
