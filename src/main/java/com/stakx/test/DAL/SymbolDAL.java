package com.stakx.test.DAL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.stakx.text.model.SymbolDataModel;

@Repository
public class SymbolDAL {
	@Autowired
	MongoTemplate mongoTemplate;

	public SymbolDataModel savedata(SymbolDataModel symbolData) {
		System.out.println("saving data to db");
		return mongoTemplate.save(symbolData);

	}
	 public SymbolDataModel fetchdata(String symbol) {
		 System.out.println("fetching data from db");
		 Query query = new Query();
		 query.addCriteria(Criteria.where("_id").is(symbol));
		 SymbolDataModel symbolData = mongoTemplate.findOne(query, SymbolDataModel.class);
		 return symbolData;
	 }

}
