package az.nizami.smartdirectaze.business.controller;

import az.nizami.smartdirectaze.business.BusinessService;
import az.nizami.smartdirectaze.business.dto.ChosenBusinessDto;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;
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

    private final BusinessService businessService;

    @GetMapping("/my")
    public ResponseEntity<List<IndustrySummaryDto>> getMyBusinesses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(businessService.getMyBusinesses(userDetails.getUsername()));
    }

    @PostMapping("/choose-business")
    public ResponseEntity<ChosenBusinessDto> chooseBusiness(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        String businessType = request.get("businessType");
        return ResponseEntity.ok(businessService.chooseBusinessType(userDetails.getUsername(), businessType));
    }
}
