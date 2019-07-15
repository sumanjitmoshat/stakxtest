package com.stakx.text.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.binance.api.client.domain.account.NewOrderResponse;

@Document(collection = "orderresponse")
public class OrderResponseDataModel {

	@Id
	private Long orderId;

	private NewOrderResponse newOrderResponse;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public NewOrderResponse getNewOrderResponse() {
		return newOrderResponse;
	}

	public void setNewOrderResponse(NewOrderResponse newOrderResponse) {
		this.newOrderResponse = newOrderResponse;
	}

	public OrderResponseDataModel(Long orderId, NewOrderResponse newOrderResponse) {
		super();
		this.orderId = orderId;
		this.newOrderResponse = newOrderResponse;
	}

	public OrderResponseDataModel() {
		super();
	}

}
