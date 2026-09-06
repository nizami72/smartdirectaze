package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.OrderService;
import az.nizami.smartdirectaze.shop.entities.OrderEntity;
import az.nizami.smartdirectaze.shop.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderDTO createNewOrder(Long shopId, String customerName, String phoneNumber, String deliveryAddress, String itemsSummary, String paymentMethod) {
        OrderEntity orderEntity = OrderEntity.builder()
                .shopId(shopId)
                .customerName(customerName)
                .phoneNumber(phoneNumber)
                .deliveryAddress(deliveryAddress)
                .itemsSummary(itemsSummary)
                .paymentMethod(paymentMethod)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        return OrderDTO.builder()
                .id(savedOrder.getId())
                .shopId(savedOrder.getShopId())
                .customerName(savedOrder.getCustomerName())
                .phoneNumber(savedOrder.getPhoneNumber())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .itemsSummary(savedOrder.getItemsSummary())
                .paymentMethod(savedOrder.getPaymentMethod())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }
}
