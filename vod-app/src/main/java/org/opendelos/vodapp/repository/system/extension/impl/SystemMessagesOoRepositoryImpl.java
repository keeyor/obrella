package org.opendelos.vodapp.repository.system.extension.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opendelos.model.system.SystemMessage;
import org.opendelos.vodapp.repository.system.extension.SystemMessagesOoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SystemMessagesOoRepositoryImpl implements SystemMessagesOoRepository {

	@Autowired
	MongoTemplate mongoTemplate;
	@Override
	public List<SystemMessage> findAllByVisibleAndTargetAndSitesOrderByStartDateDesc(boolean visible, String target, String sites) {

		Query query = new Query();
		List<Criteria> andExpression =  new ArrayList<>();

		if (visible) {
			Criteria expression = new Criteria();
			expression.and("visible").is(true);
			andExpression.add(expression);
		}
		else {
			Criteria expression = new Criteria();
			expression.and("visible").is(false);
			andExpression.add(expression);
		}

		if (target != null && !target.isEmpty()) {
			List<String> target_codes = Arrays.asList(target.split(","));
			Criteria expression = new Criteria();
			expression.and("target").in(target_codes);
			andExpression.add(expression);
		}

		if (sites != null && !sites.isEmpty()) {
			List<String> target_sites= Arrays.asList(sites.split(","));
			Criteria expression = new Criteria();
			expression.and("site").in(target_sites);
			andExpression.add(expression);
		}

		if (!andExpression.isEmpty()) {
			Criteria andCriteria = new Criteria();
			query.addCriteria(andCriteria.andOperator(andExpression.toArray(new Criteria[andExpression.size()])));
		}

		Sort sort = Sort.by(Sort.Order.desc("startDate"));
		query = query.with(sort);

		return mongoTemplate.find(query, SystemMessage.class);
	}

}
