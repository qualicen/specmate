package com.specmate.robotframework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.Folder;
import com.specmate.model.batch.Operation;
import com.specmate.model.testspecification.RobotProcedure;
import com.specmate.model.testspecification.RobotStep;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.impl.RobotStepImpl;
import com.specmate.urihandler.IURIFactory;
// import com.specmate.model.support.internal.EObjectUriFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;


public class RobotFileCreator {
	private static String ROBOT_PATH = "/Users/Dominik/Documents/Arbeit/Qualicen/Specmate/Robotframework";
	
	public static void updateFile(Folder project, IURIFactory factory) {
		System.out.println("Update File");
		
		Vector<Object> worklist = new Vector<Object>();
		worklist.add(project);
		
		while(!worklist.isEmpty() ) {
			Object current = worklist.remove(0);
			
			try {
				List<EObject> children = SpecmateEcoreUtil.getChildren(current);
				
				if(current instanceof TestSpecification) {
					
					boolean hasRobotTests = children.stream().anyMatch(e -> (e instanceof RobotProcedure));
					if(! hasRobotTests) {
						continue;
					}
					System.out.println(current);
					
					URI uri = EcoreUtil.getURI((EObject) current);
					// Project/Folder/Requirement/Model/Testsuite/Testcase/RobotProcedure
					String projectPath = factory.getURI((EObject) current);
					String fullPath = ROBOT_PATH+"/"+projectPath+".robot";
					
					File file = new File(fullPath);
					file.getParentFile().mkdirs();
					FileWriter writer = new FileWriter(file);
					
					for(EObject obj: children) {
						if(obj instanceof RobotProcedure) {
							writer.write(getRobotTest((RobotProcedure)obj));
							writer.write("---\n\n");
						}
					}
					
					writer.close();
					
				} else {
					for(EObject obj: children) {
						worklist.add(obj);
					}
				}
			} catch(Exception e) {}
		}
	}
	
	private static String getRobotTest(RobotProcedure proc) throws SpecmateException {
		String out = "";
		List<EObject> children = SpecmateEcoreUtil.getChildren((EObject)proc);
		for(EObject obj: children) {
			if(obj instanceof RobotStep) {
				RobotStep step = (RobotStep) obj;
				out+= step.getName()+":\n";
				out+= step.getDescription().trim()+"\n";
				out+= "END\n\n";
			}
		}
		
		return out.trim();
	}
}
