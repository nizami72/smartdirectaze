package az.nizami.smartdirectaze.business;

import az.nizami.smartdirectaze.business.domain.Business;
import az.nizami.smartdirectaze.business.domain.BusinessMember;
import az.nizami.smartdirectaze.business.domain.BusinessRole;
import az.nizami.smartdirectaze.business.repository.BusinessMemberRepository;
import az.nizami.smartdirectaze.business.repository.BusinessRepository;
import az.nizami.smartdirectaze.business.service.BusinessServiceImpl;
import az.nizami.smartdirectaze.identity.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BusinessMemberRepository businessMemberRepository;

    @Mock
    private UserService userService;

    private BusinessServiceImpl businessService;

    @BeforeEach
    void setUp() {
        businessService = new BusinessServiceImpl(businessRepository, businessMemberRepository, userService, List.of());
    }

    @Test
    void ensureBusiness_Existing_ShouldReuseIt() {
        // Arrange
        UUID businessId = UUID.randomUUID();
        Business business = Business.builder().id(businessId).industry(Industry.SHOP).build();
        when(businessMemberRepository.findByUserIdAndBusiness_Industry(7L, Industry.SHOP))
                .thenReturn(Optional.of(BusinessMember.builder().userId(7L).business(business).build()));

        // Act
        UUID result = businessService.ensureBusiness(7L, Industry.SHOP);

        // Assert
        assertEquals(businessId, result);
        verify(businessRepository, never()).save(any());
        verify(businessMemberRepository, never()).save(any());
    }

    @Test
    void ensureBusiness_Missing_ShouldCreateBusinessWithOwner() {
        // Arrange
        UUID businessId = UUID.randomUUID();
        when(businessMemberRepository.findByUserIdAndBusiness_Industry(7L, Industry.SHOP)).thenReturn(Optional.empty());
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> {
            Business saved = inv.getArgument(0);
            saved.setId(businessId);
            return saved;
        });

        // Act
        UUID result = businessService.ensureBusiness(7L, Industry.SHOP);

        // Assert
        assertEquals(businessId, result);
        ArgumentCaptor<BusinessMember> member = ArgumentCaptor.forClass(BusinessMember.class);
        verify(businessMemberRepository).save(member.capture());
        assertEquals(7L, member.getValue().getUserId());
        assertEquals(BusinessRole.OWNER, member.getValue().getRole());
        assertEquals(Industry.SHOP, member.getValue().getBusiness().getIndustry());
    }
}
