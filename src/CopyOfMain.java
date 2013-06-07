import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.rest.graphdb.*;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;


public class CopyOfMain {

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		
		final String SERVER_ROOT_URI = "http://c8260d311.hosted.neo4j.org:7425/db/data/";
		String username = "c67c3a79c";
		String password = "b2e5dc2fe";
		
//		final String SERVER_ROOT_URI_1 = "http://b94490aa7.hosted.neo4j.org:7704/db/data/";
//		String username_1 = "df33cb688";
//		String password_1 = "cf423e603";
		
		RestAPIFacade rest = new RestAPIFacade(SERVER_ROOT_URI, username,
				password);
//		RestAPIFacade rest1 = new RestAPIFacade(SERVER_ROOT_URI_1, username_1,
//				password_1);
		
		String query1 = "START n=node(0) MATCH (n)-->(x) RETURN x.name,x.Class";
		Map<?, ?> result1 = rest.query(query1, null);
		List<List<String>> rows1 = (List<List<String>>) result1.get("data");
//		//row1.get(0) --> n.name        
//        
//        
//        
		String filename = "C:/Users/Jalil0s/Desktop/signature.txt";
		FileWriter fw = new FileWriter(filename, true);
		for (List<String> row1 : rows1) {
			
			//System.out.println(row1.get(0));
			fw.write(row1.get(1)+"  "+row1.get(0)+" \r\n");

//			//Creation de noeuf et de relation
//			String query2 = "START n=node("+row1.get(0)+") MATCH (n)-->(x) RETURN x.name";
//			Map<?, ?> result2 = rest.query(query2, null);
//			List<List<String>> rows2 = (List<List<String>>) result2.get("data");
//			
//			for (List<String> row2 : rows2) {
//				Map<?, ?> result_1 = rest1.query("CREATE n = {name : '" +row2.get(0)+ "'} Return ID(n)", null);
//				List<List<Integer>> rows_1 = (List<List<Integer>>) result_1.get("data");
//				Map<?, ?> result__1 = rest1.query("START a=node(0), b=node(" + rows_1.get(0).get(0)+ ") CREATE a-[r:RELTYPE]->b", null);
//			}			
		}
//		
//		rest.close();
		
		fw.close();
		rest.close();
    }

}