package it.polimi.spf.demo.couponing.client;

import it.polimi.spf.lib.services.ServiceInterface;
import it.polimi.spf.lib.services.ServiceInvocationException;

@ServiceInterface(
		app = "it.polimi.spf.demo.couponing.client",
		name = "Coupon Delivery Service",
		version = "0.0.1"
)
public interface CouponDeliveryService {

	public void deliverCoupon(Coupon coupon) throws ServiceInvocationException;

}
