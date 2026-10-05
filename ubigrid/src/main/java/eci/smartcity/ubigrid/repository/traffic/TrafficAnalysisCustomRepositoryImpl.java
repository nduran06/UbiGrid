package eci.smartcity.ubigrid.repository.traffic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.bson.Document;
import com.mongodb.client.DistinctIterable;

import eci.smartcity.ubigrid.model.traffic.TrafficAnalysisResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrafficAnalysisCustomRepositoryImpl implements TrafficAnalysisCustomRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<TrafficAnalysisResult> findLatestAnalysisForAllLocations() {
		// First, get all unique location IDs
		DistinctIterable<String> locations = mongoTemplate.getCollection("trafficAnalysisResults")
				.distinct("locationId", String.class);

		List<TrafficAnalysisResult> results = new ArrayList<>();

		// For each location, find the document with the latest timestamp
		for (String locationId : locations) {
			Query query = new Query(Criteria.where("locationId").is(locationId))
					.with(Sort.by(Sort.Direction.DESC, "timestamp")).limit(1);

			TrafficAnalysisResult result = mongoTemplate.findOne(query, TrafficAnalysisResult.class);
			if (result != null) {
				results.add(result);
			}
		}

		return results;
	}

	@Override
	public Map<String, Double> getAverageCongestionByTimeOfDay(String locationId, LocalDateTime startDate,
			LocalDateTime endDate) {
		// Extract hour of day and group by it to get average congestion
		AggregationOperation match = Aggregation
				.match(Criteria.where("locationId").is(locationId).and("timestamp").gte(startDate).lte(endDate));

		AggregationOperation project = Aggregation.project("congestionLevel").and(DateOperators.DateToString
				.dateOf("timestamp").toString("%H").withTimezone(DateOperators.Timezone.valueOf("UTC")))
				.as("hourOfDay");

		AggregationOperation group = Aggregation.group("hourOfDay").avg("congestionLevel").as("averageCongestion");

		Aggregation aggregation = Aggregation.newAggregation(match, project, group);
		AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "trafficAnalysisResults",
				Document.class);

		Map<String, Double> hourlyAverages = new HashMap<>();
		for (Document doc : results.getMappedResults()) {
			String hour = doc.getString("_id");
			Double avg = doc.getDouble("averageCongestion");
			hourlyAverages.put(hour, avg);
		}

		return hourlyAverages;
	}

	@Override
	public List<TrafficAnalysisResult> findSimilarHistoricalPatterns(TrafficAnalysisResult current, int limitResults) {
		// Build query to find similar patterns based on congestion level and time
		// patterns
		// This is a simplified example and may need adjustment based on your specific
		// similarity criteria

		double congestionLevel = current.getCurrentCongestionLevel();
		double congestionTolerance = 0.2; // 20% tolerance

		// Get the day of week and hour from the current timestamp
		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(current.getAnalysisTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		int hourTolerance = 1; // Look for patterns within ±1 hour

		// Create criteria for finding similar patterns
		Criteria timeCriteria = new Criteria();

		// Custom scoring function could be implemented here for more advanced
		// similarity metrics
		// For now, we'll just sort by how close the time of day is

		// Find patterns with similar congestion level
		Query query = new Query(Criteria.where("analysisId").ne(current.getAnalysisId()).and("locationId")
				.is(current.getLocationId()).and("currentCongestionLevel").gte(congestionLevel - congestionTolerance)
				.and("currentCongestionLevel").lte(congestionLevel + congestionTolerance));

		// Retrieve potential matches
		List<TrafficAnalysisResult> potentialMatches = mongoTemplate.find(query, TrafficAnalysisResult.class);

		// Filter by day of week and hour (since MongoDB doesn't easily support this in
		// queries)
		List<TrafficAnalysisResult> filteredResults = potentialMatches.stream().filter(result -> {
			Calendar resultCal = Calendar.getInstance();
			resultCal.setTime(Date.from(result.getAnalysisTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
			int resultDayOfWeek = resultCal.get(Calendar.DAY_OF_WEEK);
			int resultHourOfDay = resultCal.get(Calendar.HOUR_OF_DAY);

			// Match day of week and approximate hour
			return resultDayOfWeek == dayOfWeek && Math.abs(resultHourOfDay - hourOfDay) <= hourTolerance;
		}).limit(limitResults).collect(Collectors.toList());

		return filteredResults;
	}

	@Override
	public void updatePredictionAccuracy(String analysisId, Map<String, Double> metrics) {
		Query query = new Query(Criteria.where("_id").is(analysisId));

		Update update = new Update();
		for (Map.Entry<String, Double> entry : metrics.entrySet()) {
			update.set("predictionAccuracy." + entry.getKey(), entry.getValue());
		}

		// Add a field to indicate this record has been evaluated
		update.set("predictionEvaluated", true);
		update.set("lastEvaluatedAt", LocalDateTime.now());

		mongoTemplate.updateFirst(query, update, TrafficAnalysisResult.class);
	}
}