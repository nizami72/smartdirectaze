package az.nizami.smartdirectaze.business.repository;

import az.nizami.smartdirectaze.business.domain.BusinessMember;
import az.nizami.smartdirectaze.business.dto.BusinessSummaryDto;
import az.nizami.smartdirectaze.business.dto.IndustrySummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, UUID> {

    @Query("""
              select new az.nizami.smartdirectaze.business.dto.BusinessSummaryDto(
                b.id,
                b.name,
                b.industry
              )
              from BusinessMember bm
              join bm.business b
              join User u on u.id = bm.userId
              where u.email = :email
              order by b.name
            """)
    List<BusinessSummaryDto> findBusinessSummariesByUserEmail(@Param("email") String email);



    @Query("""
              select new az.nizami.smartdirectaze.business.dto.IndustrySummaryDto(
                b.industry,
                case
                  when b.industry = az.nizami.smartdirectaze.business.domain.Industry.SHOP then 'Shop'
                  when b.industry = az.nizami.smartdirectaze.business.domain.Industry.EVENTS then 'Events'
                  when b.industry = az.nizami.smartdirectaze.business.domain.Industry.DENTAL then 'Dental'
                  else cast(b.industry as string)
                end,
                count(distinct b.id)
              )
              from BusinessMember bm
              join bm.business b
              join User u on u.id = bm.userId
              where u.email = :email
              group by b.industry
              order by b.industry
            """)
    List<IndustrySummaryDto> findIndustrySummariesByUserEmail(@Param("email") String email);


}
