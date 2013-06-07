import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;


public class JSON{
	
	//========================================================================
	/**
	 * <b><p> retrieveJSONArray(ArrayList<</String[]>> jsonArray)</b></p>
	 * <br>
	 * <ul><li>Returns JSON formed Array from the ArrayList provided.</li>
	 * 		<li><b>jsonArray</b> will be ArrayList of array.</li>
	 * 		<li>the elements provided in array will be arranged in consecutive keys</li>
	 * 		<li>ex:<b> [{"key0","1st element of array"},{"key1","2nd element of array"}]</b> </li>
	 * </ul>
	 */
	//========================================================================
	public static String retrieveJSONArray(ArrayList<String[]> jsonArray){
		
		try{
			
			String[] jsonObject=new String[2];
			JSONStringer stringer=new JSONStringer();
			
			stringer.array();
			
			int arrayLength=jsonArray.size();
			
			for(int i=0;i<arrayLength;i++){
				
				jsonObject=jsonArray.get(i);
				
				stringer.object();
				
				for(int j=0;j<jsonObject.length;j++)
					stringer.key("key"+j).value(jsonObject[j]);
				
				stringer.endObject();
			}
			
			stringer.endArray();
			
			return stringer.toString();
				
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	//========================================================================
	/**
	 * <b><p> retrieveJSONArray(ArrayList<</String[]>> jsonArray,String[] key)</b></p>
	 * <br>
	 * <ul><li>Returns JSON formed Array from the ArrayList provided.</li>
	 * 		<li><b>jsonArray</b> will be ArrayList of array.</li>
	 * 		<li>the elements provided in array will be arranged in consecutive keys</li>
	 * 		<li>ex:<b> [{"key[0]","1st element of array"},{"key[1]","2nd element of array"}]</b> </li>
	 * </ul>
	 */
	//========================================================================
	public static String retrieveJSONArray(ArrayList<String[]> jsonArray,String[] key){
		
		try{
			
			String[] jsonObject=new String[2];
			JSONStringer stringer=new JSONStringer();
			
			stringer.array();
			
			int arrayLength=jsonArray.size();
			
			for(int i=0;i<arrayLength;i++){
				
				jsonObject=jsonArray.get(i);
				
				stringer.object();
				
				for(int j=0;j<jsonObject.length;j++)
					stringer.key(key[j]).value(jsonObject[j]);
				
				stringer.endObject();
			}
			
			stringer.endArray();
			
			return stringer.toString();
				
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	//========================================================================
	/**
	 * <b><p> retrieveJSONString(ArrayList<</String[]>> jsonArray)</b></p>
	 * <br>
	 * <ul><li>Returns JSON formed string from the ArrayList provided.</li>
	 * 		<li><b>jsonArray</b> will be ArrayList of array.</li>
	 * 		<li>ex:<b> {"key0":"1st element of array","key1":"2nd element of array"}</b> </li>
	 * </ul>
	 */
	//========================================================================
	public static String retrieveJSONString(ArrayList<String[]> jsonObject){
		
		try{
			
			String[] arr_jsonObject=new String[2];
			JSONStringer stringer=new JSONStringer();
			
			stringer.object();
			
			for(int i=0;i<jsonObject.size();i++){
				
				arr_jsonObject=jsonObject.get(i);
				stringer.key(arr_jsonObject[0]).value(arr_jsonObject[1]);
			}
			
			stringer.endObject();
			
			return stringer.toString();
			
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	//========================================================================
	/**
	 * <p>Converts jsonArray to an arrayList of String[]. Where each row contains values in json
	 * String array, in increasing order of key values provided, without there key counterparts.
	 * <br><br>
	 * For ex: if JSON string is<br><br> [{"key00":"value00","key01":"value01"},{"key10":"value10","key11":"value11"}],
	 * <br><br> then the rows of an array will be as follows <br>
	 * <ul><li>First row : 1st element- value00, 2nd element - value01</li>
	 * <li>Second row : 1st element- value10, 2nd element - value11</li></ul>
	 * </p>
	 * 
	 * */
	//========================================================================
	public static ArrayList<String[]> convertJSONArraytoArrayList(String jsonArray,String[] key){
		
		try{
			
			JSONArray JsonArray=new JSONArray(jsonArray);
			JSONObject JsonObject=new JSONObject();
			
			int jsonArraySize=JsonArray.length();
			
			String[] jsonObjectArray;
			ArrayList<String[]> jsonArrayList=new ArrayList<String[]>();
			
			for(int i=0;i<jsonArraySize;i++){
				
				JsonObject=JsonArray.getJSONObject(i);
				
				jsonObjectArray=new String[key.length];
				
				for(int j=0;j<key.length;j++)
					jsonObjectArray[j]=JsonObject.getString(key[j]);
				
				jsonArrayList.add(jsonObjectArray);
			}
			
			return jsonArrayList;
			
		}catch(Exception e){
			
			e.printStackTrace();
			return null;
		}
	}

	//========================================================================
	/**
	 * <p>Converts jsonString to an arrayList of String[].
	 * <br>
	 * For ex: if JSON string is<br><br> {"key00":"value00","key01":"value01"},
	 * <br><br> then the rows of an array will be as follows <br>
	 * <ul><li>First row : 1st element- value00</li>
	 * <li>Second row : 1st element- value10</li></ul>
	 * </p>
	 * 
	 * */
	//========================================================================
	public static ArrayList<String[]> convertJSONStringtoArrayList(String jsonString,String[] key){
		
		try{
			
			JSONObject jsonObject=new JSONObject(jsonString);
			
			ArrayList<String[]> jsonArrayList=new ArrayList<String[]>();
			
			for(int i=0;i<key.length;i++)
				jsonArrayList.add(new String[]{jsonObject.getString(key[i])});
			
			return jsonArrayList;
			
		}catch(Exception e){
			
			e.printStackTrace();
			return null;
		}
	}

	
}