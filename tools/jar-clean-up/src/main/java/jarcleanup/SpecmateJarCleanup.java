package jarcleanup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** To clean up unnecessary jar files from the bnd local cache folder */
public class SpecmateJarCleanup {

	private static String[] WHITELIST = new String[] { "osgi.*", "biz.*", "org.junit.*", "org.mockito.*" };

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Usage: SpecmateJarCleanup [path-to-local-repo] [path-to-bundles]");
			System.exit(1);
		} else {
			new SpecmateJarCleanup(args[0], args[1]);
		}
	}

	public SpecmateJarCleanup(String localRepoFolder, String bundleFolder) throws IOException {
		Stream<JarEntry> jarFiles = listJars(localRepoFolder).map(SpecmateJarCleanup::toJarEntry)
				.filter(Optional::isPresent).map(Optional::get);

		Stream<JarEntry> rundBundles = listRunRequirements(bundleFolder);
		Map<String, List<JarEntry>> runMap = new HashMap<>();
		for (JarEntry je : rundBundles.collect(Collectors.toList())) {
			String symbolicName = je.getSymbolicName();
			List<JarEntry> exl = runMap.get(symbolicName);
			if (exl == null) {
				exl = new ArrayList<JarEntry>();
				runMap.put(symbolicName, exl);
			}
			if (!exl.isEmpty()) {
				JarEntry ex = exl.get(0);
				if (compare(ex.getVersion(), je.getVersion()) != 0) {
					System.out.println("WARNING: Different requirements " + symbolicName + " in " + ex.getSource()
							+ " and " + je.getSource());
				}
			}
			exl.add(je);
		}

		Stream<JarEntry> toDelete = jarFiles.filter(jf -> {
			List<JarEntry> constraints = runMap.get(jf.getSymbolicName());
			if (constraints == null) {
				// not mentioned - we can delete
				return true;
			}
			return constraints.stream().allMatch(constraint -> {
				if (compare(jf.getVersion(), constraint.getVersion()) <= -1) {
					return true;
				}
				if (compare(jf.getVersion(), constraint.getMaxVersion()) >= 0) {
					return true;
				}
				return false;
			});
		});

		System.out.println("============== To Delete ==============");
		toDelete.filter(jf -> Arrays.stream(WHITELIST).noneMatch(p -> jf.getSymbolicName().matches(p))).forEach(jf -> {
			System.out.println(jf.getSymbolicName() + " " + jf.getVersion() + " " + jf.getPath());
			try {
				Files.delete(jf.getPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private static int compare(String version1, String version2) {
		List<Integer> versionParts1 = toVersionArray(version1);
		List<Integer> versionParts2 = toVersionArray(version2);
		return compareVersionList(versionParts1, versionParts2);
	}

	private static List<Integer> toVersionArray(String version1) {
		return Arrays.asList(version1.split("\\.")).stream().filter(vp -> vp.matches("\\d+"))
				.map(vp -> Integer.parseInt(vp)).collect(Collectors.toList());
	}

	private Stream<JarEntry> listRunRequirements(String bundleFolder) {
		try {
			return Files.walk(Paths.get(bundleFolder)).filter(p -> {
				String pathString = p.toString();
				return pathString.endsWith(".bnd") || pathString.endsWith(".bndrun");
			}).flatMap(SpecmateJarCleanup::parseBndFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Stream.empty();
		}
	}

	private static Stream<JarEntry> parseBndFile(Path bndPath) {
		List<JarEntry> entries = new ArrayList<>();
		String bndContent;
		try {
			bndContent = Files.readString(bndPath);
		} catch (IOException e) {
			e.printStackTrace();
			return Stream.empty();
		}
		Pattern pattern = Pattern.compile("^\\s+(.*);version='\\[(.*),(.*)\\)'", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(bndContent);
		while (matcher.find()) {
			entries.add(new JarEntry(bndPath.toString(), matcher.group(1), matcher.group(2), matcher.group(3)));
		}
		return entries.stream();
	}

	private static Optional<JarEntry> toJarEntry(Path jarPath) {
		JarFile jarFile;
		try {
			jarFile = new JarFile(jarPath.toFile());
			String symbolicName = jarFile.getManifest().getMainAttributes().getValue("Bundle-SymbolicName")
					.split(";")[0];
			String version = jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");
			jarFile.close();
			return Optional.of(new JarEntry(symbolicName, version, jarPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Optional.empty();

	}

	private Stream<Path> listJars(String folder) throws IOException {
		return Files.walk(Paths.get(folder)).filter(p -> p.toString().endsWith(".jar"));
	}

	private static int compareVersionList(List<Integer> o1, List<Integer> o2) {
		Iterator<Integer> i1 = o1.iterator();
		Iterator<Integer> i2 = o2.iterator();
		int result;

		do {
			if (!i1.hasNext()) {
				if (!i2.hasNext()) {
					return 0;
				} else {
					return -1;
				}
			}
			if (!i2.hasNext()) {
				return 1;
			}

			result = i1.next().compareTo(i2.next());
		} while (result == 0);

		return result;
	}

}
