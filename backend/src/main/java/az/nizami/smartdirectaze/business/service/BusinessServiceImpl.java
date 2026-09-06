package az.nizami.smartdirectaze.business.service;

import az.nizami.smartdirectaze.business.BusinessService;
import az.nizami.smartdirectaze.business.domain.Business;
import az.nizami.smartdirectaze.business.domain.BusinessMember;
import az.nizami.smartdirectaze.business.domain.Industry;
import az.nizami.smartdirectaze.business.repository.BusinessMemberRepository;
import az.nizami.smartdirectaze.business.repository.BusinessRepository;
import az.nizami.smartdirectaze.exception.UserNotFoundException;
import az.nizami.smartdirectaze.exception.ErrorMessage;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.business.domain.BusinessRole;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Log4j2
public class BusinessServiceImpl implements BusinessService {


    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final UserService userService;

    public BusinessServiceImpl(BusinessRepository businessRepository, BusinessMemberRepository businessMemberRepository, UserService userService) {
        this.businessRepository = businessRepository;
        this.businessMemberRepository = businessMemberRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public UserDto chooseBusinessType(String email, String businessType) {

        Optional<UserDto> userOp = userService.findByEmail(email);
        if(userOp.isEmpty()) {
            log.error(ErrorMessage.USER_NOT_FOUND.getSystemMessage());
            throw new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getUserMessage());
        }
        UserDto user = userOp.get();

        Industry industry = switch (businessType.toUpperCase()) {
            case "STORE" -> Industry.SHOP;
            case "EVENTS" -> Industry.EVENTS;
            case "DENTAL" -> Industry.DENTAL;
            default -> throw new IllegalArgumentException("Unknown business type: " + businessType);
        };

        Business business = Business.builder()
                .name(industry.name() + " for " + email)
                .industry(industry)
                .build();
        business = businessRepository.save(business);

        BusinessMember member = BusinessMember.builder()
                .userId(user.getId())
                .business(business)
                .role(BusinessRole.OWNER)
                .build();
        businessMemberRepository.save(member);
       // todo consider use of another return object
        return user;
    }

}
