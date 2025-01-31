package cart.domain.order;

import cart.domain.delivery.DeliveryPolicy;
import cart.domain.discount.DiscountPolicy;

public class OrderPrice {

    private Long productPrice;
    private final DiscountPolicy discountPolicy;
    private final DeliveryPolicy deliveryPolicy;

    private OrderPrice(final Long productPrice, final DiscountPolicy discountPolicy,
        final DeliveryPolicy deliveryPolicy) {
        this.productPrice = productPrice;
        this.discountPolicy = discountPolicy;
        this.deliveryPolicy = deliveryPolicy;
    }

    public static OrderPrice of(final Order order, final DiscountPolicy discountPolicy,
        final DeliveryPolicy deliveryPolicy) {
        return new OrderPrice(order.getProductPrice(), discountPolicy, deliveryPolicy);
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public Long getDiscountPrice() {
        return discountPolicy.calculate(productPrice);
    }

    public Long getDeliveryFee() {
        return deliveryPolicy.getDeliveryFee(productPrice);
    }

    public Long getTotalPrice() {
        return productPrice - getDiscountPrice() + getDeliveryFee();
    }
}
