
public class ValueVersion {
	private String value;
	private int ver;
	
	public ValueVersion(String value, int ver){
		this.value = value;
		this.ver = ver;
	}

	public String getValue() {
		return value;
	}

	public int getVer() {
		return ver;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setVer(int ver) {
		this.ver = ver;
	}
}
