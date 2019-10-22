package com.specmate.robotframework;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.Folder;
import com.specmate.model.base.IContainer;
import com.specmate.model.base.IDescribed;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.RobotProcedure;
import com.specmate.model.testspecification.RobotStep;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.urihandler.IURIFactory;

public class RobotFileCreator {
	private static String ROBOT_PATH = System.getProperty("user.dir") + "/Robotframework/";

	public static void updateFile(Folder project, IURIFactory factory) {
		System.out.println("Update File");

		Vector<Object> worklist = new Vector<Object>();
		worklist.add(project);
		while (!worklist.isEmpty()) {
			Object current = worklist.remove(0);

			try {
				List<EObject> children = SpecmateEcoreUtil.getChildren(current);
				if (current instanceof TestSpecification) {
					if (!hasRobotTests((TestSpecification) current)) {
						continue;
					}
					// Project/Folder/Requirement/Model/Testsuite/Testcase/RobotProcedure
					String fullPath = ROBOT_PATH + getPath((IContainer) current, project);
					System.out.println(fullPath);

					File file = new File(fullPath);
					file.getParentFile().mkdirs();
					FileWriter writer = new FileWriter(file);

					writer.write(getRobotHeader((TestSpecification) current));

					for (EObject tCase : children) {
						List<EObject> procedures = SpecmateEcoreUtil.getChildren(tCase);
						if (!procedures.stream().anyMatch(e -> (e instanceof RobotProcedure))) {
							continue;
						}

						// Write the test name
						writer.write(((IContainer) tCase).getName() + ":\n");
						// TODO Write Testdescription
						for (EObject proc : procedures) {
							if (proc instanceof RobotProcedure) {
								// Write the test content
								writer.write(getRobotTestContent((RobotProcedure) proc));
							}
						}
						writer.write("\n\n");
					}

					writer.close();

				} else {
					for (EObject obj : children) {
						worklist.add(obj);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private static String getRobotHeader(TestSpecification spec) {
		String result = "*** Settings ***\nDocumentation    ";
		String description = ((IDescribed) ((EObject) spec).eContainer()).getDescription().replaceAll(",", ",\n")
				.replaceAll("\\.", ".\n");

		String[] desc = description.split("\n");
		result += desc[0].trim();
		for (int i = 1; i < desc.length; i++) {
			result += "\n...              " + desc[i].trim();
		}
		result += "\nResource    ../../../../keywords.robot";
		result += "\n\n*** Test Cases ***\n";
		return result;
	}

	private static String getPath(IContainer testsuite, IContainer root) {
		String result = sanitizeName(testsuite.getName() + ".robot");

		EObject current = testsuite;
		while (current != root) {
			current = current.eContainer();
			result = sanitizeName(((IContainer) current).getName()) + "/" + result;
		}

		return result;
	}

	private static String sanitizeName(String name) {
		return name.replaceAll("[/\\\\:\\*\\?<>\"]", "_");
	}

	private static boolean hasRobotTests(TestSpecification spec) throws SpecmateException {
		List<EObject> testcases = SpecmateEcoreUtil.getChildren(spec);
		for (EObject tCase : testcases) {
			if (tCase instanceof TestCase) {
				List<EObject> children = SpecmateEcoreUtil.getChildren(tCase);
				boolean hasRobotTests = children.stream().anyMatch(e -> (e instanceof RobotProcedure));
				if (hasRobotTests) {
					return true;
				}
			}
		}

		return false;
	}

	private static String getRobotTestContent(RobotProcedure proc) throws SpecmateException {
		String result = "";

		List<RobotStep> children = SpecmateEcoreUtil.getChildren(proc).stream().filter(e -> e instanceof RobotStep)
				.map(e -> (RobotStep) e).sorted((a, b) -> {
					return Integer.compare(a.getPosition(), b.getPosition());
				}).collect(Collectors.toList());

		for (RobotStep step : children) {
			String line = step.getName() + "    ";
			line += step.getDescription().trim().replaceAll("\n", "    ");
			result += "\t" + line.trim() + "\n";
		}
		// Trim only the end
		return ("." + result).trim().substring(1);
	}
}
