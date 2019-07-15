package com.stakx.test.DAL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.stakx.text.model.OrderResponseDataModel;

@Repository
public class OrderResponseDAL {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	public OrderResponseDataModel saveorder(OrderResponseDataModel orderResponse) {
		return mongoTemplate.save(orderResponse);
	}
	
	public OrderResponseDataModel fetchOrder(Long orderId) {
		Query query = new Query();
		 query.addCriteria(Criteria.where("orderId").is(orderId));
		 OrderResponseDataModel orderResponse = mongoTemplate.findOne(query, OrderResponseDataModel.class);
		 return orderResponse;
	}

}
