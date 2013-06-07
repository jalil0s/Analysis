package doNativa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.rest.graphdb.RestAPIFacade;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;

public class AnalysisCallGraph extends AnalysisClass {

	public static void main(Integer a) throws IOException {

		final String SERVER_ROOT_URI = "http://c8260d311.hosted.neo4j.org:7425/db/data/";
		String username = "c67c3a79c";
		String password = "b2e5dc2fe";

		RestAPIFacade rest = new RestAPIFacade(SERVER_ROOT_URI, username,
				password);
		
		try {
			
			String query = "START n=node(" + a + ") RETURN n.Class,n.name";
			Map<?, ?> result = rest.query(query, null);
			List<List<String>> rows = (List<List<String>>) result.get("data");

			System.out.println(rows.get(0).get(0));
			System.out.println(rows.get(0).get(1));
			System.out.println("------------------");

			SootClass klass = Scene.v().getSootClass(rows.get(0).get(0));
			SootMethod tgt = getMethod(klass, rows.get(0).get(1));

			CallGraph cg = Scene.v().getCallGraph();
			Iterator<MethodOrMethodContext> source = new Sources(
					cg.edgesInto(tgt));

			/*
			 * Store from file to HashSet
			 * */
			String filename="";
			BufferedReader reader = new BufferedReader(new FileReader(filename));
		    
		    Set<String> lines = new HashSet<String>(100000); // maybe should be bigger
		    String line;
		    while ((line = reader.readLine()) != null) {
		        lines.add(line);
		    }
		    reader.close();
		    
		    Set<String> hs = new HashSet<String>(100000);
		    HashMap<String,String> hm = new HashMap<String,String>(100000);

		    
			while (source.hasNext()) {
				SootMethod src = (SootMethod) source.next();
				hm.put(""+src.getSubSignature(),""+src.getDeclaringClass());
				lines.retainAll(hs); // s1 now contains only elements in both sets
				Map<?, ?> result1 = rest.query(
						"CREATE n = {Class : '" + src.getDeclaringClass()
								+ "', name : '" + src.getSubSignature()
								+ "'} Return ID(n)", null);
				List<List<Integer>> rows1 = (List<List<Integer>>) result1
						.get("data");
				System.out.println(rows1.get(0).get(0));
				Map<?, ?> result2 = rest.query("START a=node(" + a
						+ "), b=node(" + rows1.get(0).get(0)
						+ ") CREATE a-[r:RELTYPE]->b", null);
			}
			
			Set set = hm.entrySet();
		    Iterator i = set.iterator();
		      while(i.hasNext()) {
		         Map.Entry me = (Map.Entry)i.next();
		         hs.add(""+me.getKey()+"@"+me.getValue());
		      }

		      


		} finally {
			rest.close();
		}
	}

}
