import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;

public class CypherRestShell {
    public static void main(String[] args) throws IOException {
    	final String SERVER_ROOT_URI = "http://16b3313d2.hosted.neo4j.org:7513/db/data/";
    	String username = "8817534ba";
		String password = "aef8e97ef";

		RestAPIFacade restAPIFacade = new RestAPIFacade(SERVER_ROOT_URI, username, password);
        try {
			/*
			 * CREATE n = {name : 'C', title : 'M'} 
			 * START n=node(*) RETURN count(*)
			 * START n=node(1) MATCH (n)-->(x) RETURN x
			 */
        	String query = "START n=node(1) MATCH (n)-->(x) RETURN ID(x)";
            Map<?,?> result = restAPIFacade.query(query, null);
            List<List<Integer>> rows = (List<List<Integer>>) result.get("data");
            for (List<Integer> row : rows) {
                System.out.println(row.get(0));
            }

        } finally {
            restAPIFacade.close();
        }
    }
}
