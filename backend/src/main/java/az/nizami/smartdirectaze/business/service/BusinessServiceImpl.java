package az.nizami.smartdirectaze.business.service;

import az.nizami.smartdirectaze.business.BusinessChildCounter;
import az.nizami.smartdirectaze.business.BusinessService;
import az.nizami.smartdirectaze.business.domain.Business;
import az.nizami.smartdirectaze.business.domain.BusinessMember;
import az.nizami.smartdirectaze.business.Industry;
import az.nizami.smartdirectaze.business.dto.ChosenBusinessDto;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;
import az.nizami.smartdirectaze.business.repository.BusinessMemberRepository;
import az.nizami.smartdirectaze.business.repository.BusinessRepository;
import az.nizami.smartdirectaze.exception.UserNotFoundException;
import az.nizami.smartdirectaze.exception.ErrorMessage;
import az.nizami.smartdirectaze.identity.UserDto;
import az.nizami.smartdirectaze.identity.UserService;
import az.nizami.smartdirectaze.business.domain.BusinessRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final UserService userService;
    // Shops, events: counted through their modules without depending on them
    private final List<BusinessChildCounter> childCounters;

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

        return new ChosenBusinessDto(ensureBusiness(user.getId(), industry), industry);
    }

    @Override
    @Transactional
    public UUID ensureBusiness(Long userId, Industry industry) {
        // Дедупликация: один Business на (пользователь + индустрия).
        Optional<BusinessMember> existing = businessMemberRepository.findByUserIdAndBusiness_Industry(userId, industry);
        if (existing.isPresent()) {
            return existing.get().getBusiness().getId();
        }

        Business business = businessRepository.save(Business.builder()
                .name(industry.name() + " for user " + userId)
                .industry(industry)
                .build());

        businessMemberRepository.save(BusinessMember.builder()
                .userId(userId)
                .business(business)
                .role(BusinessRole.OWNER)
                .build());

        log.info("Created business {} ({}) for user {}", business.getId(), industry, userId);
        return business.getId();
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
        return childCounters.stream()
                .filter(counter -> counter.industry() == industry)
                .mapToLong(counter -> counter.countByBusiness(businessId))
                .sum();
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
