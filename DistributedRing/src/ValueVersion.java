import java.io.Serializable;

public class ValueVersion implements Serializable {
	private static final long serialVersionUID = 1L;
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
