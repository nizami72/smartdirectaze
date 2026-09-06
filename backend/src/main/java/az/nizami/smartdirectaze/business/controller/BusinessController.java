package az.nizami.smartdirectaze.business.controller;

import az.nizami.smartdirectaze.business.BusinessService;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;
import az.nizami.smartdirectaze.business.repository.BusinessMemberRepository;
import az.nizami.smartdirectaze.identity.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessService businessService;

    @GetMapping("/my")
    public ResponseEntity<List<IndustrySummaryDto>> getMyBusinesses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        List<IndustrySummaryDto> dtoList = businessMemberRepository.findIndustrySummariesByUserEmail(email);
        return ResponseEntity.ok(dtoList);
    }


    @PostMapping("/choose-business")
    public ResponseEntity<UserDto> chooseBusiness(@RequestBody Map<String, String> request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        String businessType = request.get("businessType");
        return ResponseEntity.ok(businessService.chooseBusinessType(userDetails.getUsername(), businessType));
    }
}
