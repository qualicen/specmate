package jarcleanup;

import java.nio.file.Path;

public class JarEntry {
	private String symbolicName;
	private String version;
	private Path path;
	private String maxVersion;
	private String source;

	public JarEntry(String symbolicName, String version, Path path) {
		super();
		this.symbolicName = symbolicName;
		this.version = version;
		this.path = path;
	}

	public JarEntry(String source, String symbolicName, String minVersion, String maxVersion) {
		this.symbolicName = symbolicName;
		version = minVersion;
		this.maxVersion = maxVersion;
		this.source = source;

	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getMaxVersion() {
		return maxVersion;
	}

	public void setMaxVersion(String maxVersion) {
		this.maxVersion = maxVersion;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}