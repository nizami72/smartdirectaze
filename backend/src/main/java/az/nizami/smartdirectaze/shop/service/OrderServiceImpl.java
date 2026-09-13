package az.nizami.smartdirectaze.shop.service;

import az.nizami.smartdirectaze.shop.OrderDTO;
import az.nizami.smartdirectaze.shop.OrderService;
import az.nizami.smartdirectaze.shop.entities.OrderEntity;
import az.nizami.smartdirectaze.shop.entities.OrderStatus;
import az.nizami.smartdirectaze.shop.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .status(OrderStatus.NEW)
                .build();

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        return toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersForShop(Long shopId) {
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long shopId, Long orderId, OrderStatus status) {
        OrderEntity order = orderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order " + orderId + " not found for shop " + shopId));

        order.setStatus(status);
        return toDto(orderRepository.save(order));
    }

    private OrderDTO toDto(OrderEntity order) {
        return OrderDTO.builder()
                .id(order.getId())
                .shopId(order.getShopId())
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .itemsSummary(order.getItemsSummary())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
