package eci.smartcity.ubigrid.repository.impl;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import eci.smartcity.ubigrid.model.AverageSpeedResult;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.repository.VehicleNodeCustomRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class VehicleNodeCustomRepositoryImpl implements VehicleNodeCustomRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public double getAverageSpeedOnRoadSegment(String roadSegmentId, LocalDateTime cutoffTime) {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("currentRoadSegmentId").is(roadSegmentId)
                             .and("lastUpdateTime").gt(cutoffTime)),
            Aggregation.group().avg("currentSpeed").as("averageSpeed")
        );
        
        AggregationResults<AverageSpeedResult> results = mongoTemplate.aggregate(
            agg, "vehicles", AverageSpeedResult.class);
        
        if (results.getMappedResults().isEmpty()) {
            return 0.0;
        }
        
        return results.getMappedResults().get(0).getAverageSpeed();
    }
    
    @Override
    public List<Vehicle> findVehiclesWithPlannedRouteContaining(String roadSegmentId) {
        Query query = new Query(Criteria.where("plannedRoute.roadSegmentId").is(roadSegmentId));
        return mongoTemplate.find(query, Vehicle.class);
    }
    
    @Override
    public void updateVehicleCurrentRoadSegment(String vehicleId, String roadSegmentId) {
        Query query = new Query(Criteria.where("_id").is(vehicleId));
        Update update = new Update().set("currentRoadSegmentId", roadSegmentId);
        mongoTemplate.updateFirst(query, update, Vehicle.class);
    }
    
    @Override
    public List<Vehicle> findVehiclesNearPoint(double longitude, double latitude, double radiusMeters) {
        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusMeters / 1000, org.springframework.data.geo.Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
        
        Query query = new Query(Criteria.where("currentLocation").withinSphere(circle));
        return mongoTemplate.find(query, Vehicle.class);
    }
}

