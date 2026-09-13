package az.nizami.smartdirectaze.business.service;

import az.nizami.smartdirectaze.business.BusinessService;
import az.nizami.smartdirectaze.business.domain.Business;
import az.nizami.smartdirectaze.business.domain.BusinessMember;
import az.nizami.smartdirectaze.business.domain.Industry;
import az.nizami.smartdirectaze.business.dto.ChosenBusinessDto;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;
import az.nizami.smartdirectaze.business.repository.BusinessMemberRepository;
import az.nizami.smartdirectaze.business.repository.BusinessRepository;
import az.nizami.smartdirectaze.event.service.EventService;
import az.nizami.smartdirectaze.exception.UserNotFoundException;
import az.nizami.smartdirectaze.exception.ErrorMessage;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.business.domain.BusinessRole;
import az.nizami.smartdirectaze.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final UserService userService;
    private final ShopService shopService;
    private final EventService eventService;

    @Override
    @Transactional
    public ChosenBusinessDto chooseBusinessType(String email, String businessType) {
        UserDto user = requireUser(email);

        Industry industry = switch (businessType.toUpperCase()) {
            case "STORE", "SHOP" -> Industry.SHOP;
            case "EVENTS" -> Industry.EVENTS;
            case "DENTAL" -> Industry.DENTAL;
            default -> throw new IllegalArgumentException("Unknown business type: " + businessType);
        };

        // Дедупликация: один Business на (пользователь + индустрия).
        BusinessMember existing = businessMemberRepository
                .findByUserIdAndBusiness_Industry(user.getId(), industry)
                .orElse(null);
        if (existing != null) {
            Business business = existing.getBusiness();
            log.info("Reusing existing business {} ({}) for user {}", business.getId(), industry, email);
            return new ChosenBusinessDto(business.getId(), industry);
        }

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

        log.info("Created business {} ({}) for user {}", business.getId(), industry, email);
        return new ChosenBusinessDto(business.getId(), industry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndustrySummaryDto> getMyBusinesses(String email) {
        UserDto user = requireUser(email);

        return businessMemberRepository.findByUserId(user.getId()).stream()
                .map(BusinessMember::getBusiness)
                .distinct()
                .map(b -> new IndustrySummaryDto(
                        b.getIndustry(),
                        displayName(b.getIndustry()),
                        countChildren(b.getIndustry(), b.getId())))
                .toList();
    }

    private long countChildren(Industry industry, java.util.UUID businessId) {
        return switch (industry) {
            case SHOP -> shopService.countByBusiness(businessId);
            case EVENTS -> eventService.countByBusiness(businessId);
            case DENTAL -> 0L;
        };
    }

    private String displayName(Industry industry) {
        return switch (industry) {
            case SHOP -> "Shop";
            case EVENTS -> "Events";
            case DENTAL -> "Dental";
        };
    }

    private UserDto requireUser(String email) {
        return userService.findByEmail(email).orElseThrow(() -> {
            log.error(ErrorMessage.USER_NOT_FOUND.getSystemMessage());
            return new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getUserMessage());
        });
    }
}
