import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import java.util.Map;
import static org.neo4j.helpers.collection.MapUtil.map;

public class TestRun {
	public static void main(String[] args) {

		String username = "8817534ba";
		String password = "aef8e97ef";
		System.out.println("starting test");
		final RestAPI api = new RestAPIFacade(
				"http://16b3313d2.hosted.neo4j.org:7513/db/data", username,
				password);
		System.out.println("API created");
		final RestCypherQueryEngine engine = new RestCypherQueryEngine(api);
		System.out.println("engine created");
		final QueryResult<Map<String, Object>> result = engine.query(
				"start n=node({id}) return n, id(n) as id;", map("id", 0));
		System.out.println("query created");
		for (Map<String, Object> row : result) {
			long id = ((Number) row.get("id")).longValue();
			System.out.println("id is " + id);
		}
	}
}