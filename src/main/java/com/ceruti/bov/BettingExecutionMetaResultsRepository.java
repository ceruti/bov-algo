package com.ceruti.bov;

import com.ceruti.bov.model.BettingExecutionMetaResults;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BettingExecutionMetaResultsRepository extends MongoRepository<BettingExecutionMetaResults, String> {

}
