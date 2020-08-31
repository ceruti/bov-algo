package integration;

import java.util.Arrays;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BsonField;
import org.bson.Document;
import org.bson.conversions.Bson;

public class AggregationQueryUtil {

    public static AggregateIterable<Document> getAllAggregations(MongoCollection<Document> collection) {
        return collection.aggregate(Arrays.asList(getMatch(), group("ALL", getAggregateFields())));
    }

    public static AggregateIterable<Document> getAggregationBySport(MongoCollection<Document> collection) {
        return collection.aggregate(Arrays.asList(getMatch(), group(eq("sport", "$sport"), getAggregateFields())));
    }

    private static BsonField[] getAggregateFields() {
        return new BsonField[]{
                sum("netProfit", "$profitRealized"),
                avg("averageProfit", "$profitRealized"),
                sum("totalBets", "$numBetsPlaced"),
                avg("averageBets", "$numBetsPlaced"),
                sum("eventsBetOn", 1L),
                avg("averageFavoriteReversals", "$favoriteReversals"),
                avg("averageWinnerOddsStandardDeviation", "$winnerOddsStandardDeviation"),
                avg("averageLoserOddsStandardDeviation", "$loserOddsStandardDeviation"),
                avg("averageNumOddsQuoted", "$numOddsQuoted")
        };
    }

    private static Bson getMatch() {
        return match(and(exists("profitRealized", true), and(exists("numBetsPlaced", true), gt("numBetsPlaced", 0L))));
    }

}
