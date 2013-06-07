package doNativa;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;



public class AnalysisClass {
	static String libo="C:/Users/Jalil0s/Desktop/Java/rt.jar";
	//static String java_home = System.getProperty("java.home");
	static String src;
	
	public static ArrayList<SootMethod> getAllMethods() throws IOException {
		ArrayList<SootMethod> entrypoints = new ArrayList<SootMethod>();
		for (String klassName :  getClasseNames(libo)) {

			Scene.v().forceResolve(klassName, SootClass.SIGNATURES);
			SootClass klass = Scene.v().getSootClass(klassName);

			// adding all non-abstract method as entrypoint
			for (SootMethod m : klass.getMethods()) {
				if (!m.isAbstract()) {
					entrypoints.add(m);
				}
			}
		}
		return entrypoints;
	}

	public static SootMethod getMethod(SootClass soot_class, String subsig) {
		SootClass base_class = soot_class;
		while (true) {
			if (soot_class.declaresMethod(subsig)) {
				return soot_class.getMethod(subsig);
			}
			if (soot_class.hasSuperclass()) {
				soot_class = soot_class.getSuperclass();
			} else {
				System.exit(1);
				//return soot_class.getMethod(subsig);
			}
		}
	}
	
	public static ArrayList<String> getClasseNames(String jarName) {
	    ArrayList<String> classes = new ArrayList<String>();

	    boolean debug = true;
		if (debug)
	        //System.out.println("Jar " + jarName );
	    try {
	        @SuppressWarnings("resource")
			JarInputStream jarFile = new JarInputStream(new FileInputStream(
	                jarName));
	        JarEntry jarEntry;

	        while (true) {
	            jarEntry = jarFile.getNextJarEntry();
	            if (jarEntry == null) {
	                break;
	            }
	            if (jarEntry.getName().endsWith(".class")) {
					if (debug)
	                	src = jarEntry.getName().replaceAll(".class", "");
						src = src.replaceAll("/", "\\.");
	                	classes.add(src);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

		return classes;
		
	}

}
