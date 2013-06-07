package doNativa;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.neo4j.rest.graphdb.RestAPIFacade;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.options.Options;

public class doAnlysis extends AnalysisCallGraph {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		AnalysisCallGraph A = new AnalysisCallGraph();

		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.myTrans", new SceneTransformer() {
					@Override
					protected void internalTransform(String phaseName,
							Map options) {
						CHATransformer.v().transform();
					}
				}));

		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Scene.v().setEntryPoints(getAllMethods());
		Scene.v().loadNecessaryClasses();
		PackManager.v().getPack("wjtp").apply();

		final String SERVER_ROOT_URI = "http://c8260d311.hosted.neo4j.org:7425/db/data/";
		String username = "c67c3a79c";
		String password = "b2e5dc2fe";

		RestAPIFacade rest = new RestAPIFacade(SERVER_ROOT_URI, username,
				password);
		try {
			/*
			 * CREATE n = {name : 'C', title : 'M'} RETURN ID(n) START n=node(*)
			 * RETURN count(*) START n=node(1) MATCH (n)-->(x) RETURN x
			 */
			String query = "START n=node(0) MATCH (n)-->(x) RETURN ID(x)";
			Map<?, ?> result = rest.query(query, null);
			List<List<Integer>> rows = (List<List<Integer>>) result.get("data");
			for (List<Integer> row : rows) {
				// 2 3 4 5
				// row.get(0) = 2
				/*String query1 = "START n=node(" + row.get(0)
						+ ") MATCH (n)-->(x) RETURN ID(x)";
				Map<?, ?> result1 = rest.query(query1, null);
				List<List<Integer>> rows1 = (List<List<Integer>>) result1
						.get("data");

				for (List<Integer> row1 : rows1) {
					// 1
					// 11 12 13
*/
				
					/*
					 * String query2 = "START n=node(" + row1.get(0) +
					 * ") MATCH (n)-->(x) RETURN ID(x)"; Map<?, ?> result2 =
					 * rest.query(query, null); List<List<Integer>> rows2 =
					 * (List<List<Integer>>) result2 .get("data");
					 * 
					 * for (List<Integer> row2 : rows2) { // 1 // 11 12 13
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 * }
					 */
					A.main(row.get(0));

				

			}

			// println

		} finally {
			rest.close();
		}

	}

}

/*
 * String query1 = "START n=node(" + row.get(0) +
 * ") MATCH (n)-->(x) RETURN ID(x)"; Map<?, ?> result1 = rest.query(query,
 * null); List<List<Integer>> rows1 = (List<List<Integer>>) result1 .get("data");
 * 
 * for (List<Integer> row1 : rows) { // 1 // 11 12 13
 * 
 * } *
 */