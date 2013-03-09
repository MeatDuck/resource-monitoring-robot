import java.util.HashMap;
import java.util.Map;


public class ProcessMap {
	private Map<String, Integer> hosts = new HashMap<String, Integer>();
	private int last_id;
	
	public ProcessMap(Map<String, Integer> hosts, int last_id){
		this.hosts = hosts;
		this.last_id = last_id;
	}
	
	public Map<String, Integer> getHosts(){
		return hosts;
	}
	
	public int getLastId(){
		return last_id;	
	}

	public void setLastId(int i) {
		this.last_id = i;		
	}
}
