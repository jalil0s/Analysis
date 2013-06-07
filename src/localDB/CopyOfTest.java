package localDB;

import java.util.Iterator;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;

class CopyOfTest extends Thread {

	public CopyOfTest(long l) {
		super();
	}

	public void run() {
		GraphDatabaseService db1 = new GraphDatabaseFactory().newEmbeddedDatabase("C:/DB1");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("C:/DB");
		ExecutionEngine engine = new ExecutionEngine(db);
		ExecutionEngine goo = new ExecutionEngine(db1);
		
		//result
		ExecutionResult result = engine.execute("START n=node("+getName()+") MATCH (n)-->(x) RETURN x");

		//Iterator
		Iterator<Node> n_column = result.columnAs("x");
		int j = 0;
		for (Node node : IteratorUtil.asIterable(n_column)) {
			j = (int) node.getId();
			// System.out.println(node.getProperty("name"));
			goo.execute("CREATE n = {name : 'C"+j+"', title : 'M"+j+"'}");
			goo.execute("c");
		}
		db.shutdown();
		db1.shutdown();

	}

	public static void main(String[] args) {

	}
}

//https://docs.google.com/forms/d/1kxJY41cRJTlXcPWcqWKyOXrdwl3V8pso8LwHR1-pjdA/viewform