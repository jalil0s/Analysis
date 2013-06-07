package localDB;

import java.util.Iterator;
import java.util.LinkedList;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;

class TH extends Thread {

	public TH(String value) {
		super(value);
	}

    public static void main (String args[]) {
        new TH("1").start();
    }
    
	public void run() {
		GraphDatabaseService db1 = new GraphDatabaseFactory().newEmbeddedDatabase("C:/DB1");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("C:/DB");
		ExecutionEngine engine = new ExecutionEngine(db);
		ExecutionEngine goo = new ExecutionEngine(db1);
		
		//result
		System.out.println(getName()+"//////");
		ExecutionResult result = engine.execute("START n=node("+getName()+") MATCH (n)-->(x) RETURN x");

		//Iterator
		Iterator<Node> n_column = result.columnAs("x");
		long j = 0;
		LinkedList<String> src = new LinkedList<String>();
		src.clear();
		
		for (Node node : IteratorUtil.asIterable(n_column)) {
			j = node.getId();
			// System.out.println(node.getProperty("name"));
			//START a=node(1), b=node(2) CREATE a-[r:RELTYPE]->b;
			goo.execute("CREATE n = {name : 'C"+j+"', title : 'M"+j+"'}");
			goo.execute("START a=node("+getName()+"), b=node("+j+ ")CREATE a-[r:RELTYPE]->b");
			src.add(""+j+"");
		}


		for (String value: src) {
			new TH(""+value+"").start();
		}
		
		db.shutdown();
		db1.shutdown();
	}

}


