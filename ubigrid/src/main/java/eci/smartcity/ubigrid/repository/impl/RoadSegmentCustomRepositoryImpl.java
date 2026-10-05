package eci.smartcity.ubigrid.repository.impl;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import eci.smartcity.ubigrid.model.RoadSegment;
import eci.smartcity.ubigrid.repository.RoadSegmentCustomRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RoadSegmentCustomRepositoryImpl implements RoadSegmentCustomRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<RoadSegment> findRoadSegmentsNearPoint(double longitude, double latitude, double radiusMeters) {
        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusMeters / 1000, org.springframework.data.geo.Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
        
        // Check if either start or end location is within radius
        Query query = new Query(new Criteria().orOperator(
            Criteria.where("startLocation").withinSphere(circle),
            Criteria.where("endLocation").withinSphere(circle),
            Criteria.where("geometry").elemMatch(Criteria.where("coordinates").withinSphere(circle))
        ));
        
        return mongoTemplate.find(query, RoadSegment.class);
    }
    
    @Override
    public void updateCongestionLevel(String roadSegmentId, double congestionLevel) {
        Query query = new Query(Criteria.where("_id").is(roadSegmentId));
        Update update = new Update().set("currentCongestionLevel", congestionLevel);
        mongoTemplate.updateFirst(query, update, RoadSegment.class);
    }
    
    @Override
    public void updateVehicleCount(String roadSegmentId, int vehicleCount) {
        Query query = new Query(Criteria.where("_id").is(roadSegmentId));
        Update update = new Update().set("currentVehicleCount", vehicleCount);
        mongoTemplate.updateFirst(query, update, RoadSegment.class);
    }
    
    @Override
    public void updateAverageSpeed(String roadSegmentId, double averageSpeed) {
        Query query = new Query(Criteria.where("_id").is(roadSegmentId));
        Update update = new Update().set("currentAverageSpeed", averageSpeed);
        mongoTemplate.updateFirst(query, update, RoadSegment.class);
    }
}