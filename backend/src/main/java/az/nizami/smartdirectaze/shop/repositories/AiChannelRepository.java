package az.nizami.smartdirectaze.shop.repositories;

import az.nizami.smartdirectaze.shop.entities.AiChannelEntity;
import az.nizami.smartdirectaze.shop.entities.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiChannelRepository extends JpaRepository<AiChannelEntity, Long> {
    Optional<AiChannelEntity> findByShopIdAndChannelType(Long shopId, ChannelType channelType);
}
