package facegallery.utils;

public class ByteArray {
	private byte[] bytes;
	
	public ByteArray(byte[] bytes) {
		this.bytes = bytes;
	}

    public ByteArray() {
    }
	
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytesNew) {
		this.bytes = bytesNew;
	}
}
