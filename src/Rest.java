import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;

public class Rest {

	public static void main(int i) throws IOException {
		
		final String SERVER_ROOT_URI = "http://16b3313d2.hosted.neo4j.org:7513/db/data/";
		String username = "8817534ba";
		String password = "aef8e97ef";

		RestAPIFacade restAPIFacade = new RestAPIFacade(SERVER_ROOT_URI,
				username, password);
		try {
			String query = "START n=node(9) RETURN ID(n)";
			Map<?, ?> result = restAPIFacade.query(query, null);
			List<List<Integer>> rows = (List<List<Integer>>) result.get("data");
			System.out.println("ok"+rows.get(0).get(0)+"ok");
		} finally {
			restAPIFacade.close();
		}

	}
}