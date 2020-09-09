package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.SimulatedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimulatedEventRepository extends MongoRepository<SimulatedEvent, String> {
}

