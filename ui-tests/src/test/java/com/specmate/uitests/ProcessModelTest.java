package com.specmate.uitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.interactions.Actions;

import com.specmate.uitests.pagemodel.CommonControlElements;
import com.specmate.uitests.pagemodel.LoginElements;
import com.specmate.uitests.pagemodel.ProcessEditorElements;
import com.specmate.uitests.pagemodel.RequirementOverviewElements;

public class ProcessModelTest extends TestBase {

	public ProcessModelTest(String os, String version, String browser, String deviceName, String deviceOrientation) {
		super(os, version, browser, deviceName, deviceOrientation);
	}
	
	/**
	  * Runs a test verifying the creation of a process model.
	  * @throws InvalidElementStateException
	  */
	@Test
	public void verifyProcessModelTest() {
		Actions builder = new Actions(driver);
		
		RequirementOverviewElements requirementOverview = new RequirementOverviewElements(driver);
		CommonControlElements commonControl = new CommonControlElements(driver);
		LoginElements login = new LoginElements(driver);
		ProcessEditorElements processEditor = new ProcessEditorElements(driver, builder);
		
		driver.get("http://localhost:8080/");
		
		if(!login.isLoggedIn()) {
			performLogin(login); 
			assertTrue(login.isLoggedIn());
		} 
			
		// Navigation to requirement
		processEditor.clickOnRelatedRequirement("Erlaubnis Autofahren");
		
		// Creating and opening new process
		String processName = "Process Model By Automated UI Test " +  new Timestamp(System.currentTimeMillis());
		requirementOverview.createProcessModelFromRequirement(processName);		

		// Create Start node
		String startNode = processEditor.createStart(50, 80);

		// Create Activity 
		String initActivity = processEditor.createActivity("Initialise", 200, 170);

		// Set expected outcome of init activity 
		processEditor.setExpectedOutcome("Initialisation completed");

		// Set description of init activity 
		processEditor.setDescription("Description for activity");

		// Create Decision
		String decision1 = processEditor.createDecison("Age-Check", 150, 270);

		// Create Activity 
		String childActivity = processEditor.createActivity("Child", 75, 350);

		// Create Activity 
		String parentActivity = processEditor.createActivity("Parent", 350, 350);

		// Reference requirement 
		processEditor.addRelatedRequirement("Zellenmarkierung");

		// Assert, that the requirement is in the list 
		assertTrue(processEditor.relatedRequirementDisplayed());

		// Delete referenced requirement 
		processEditor.removeRelatedRequirement();

		assertFalse(processEditor.relatedRequirementDisplayed());

		// Reference the requirement again, so that it is visible in the requirements overview
		processEditor.addRelatedRequirement("Zellenmarkierung");

		// Create End node
		String endNode = processEditor.createEnd(0, 450);

		// Check if error message is shown (Assert true)
		assertTrue(processEditor.errorMessageDisplayed());
		
		
		// Create connections between the activities 
		processEditor.connectActivity("", startNode, initActivity);

		processEditor.connectActivity("", initActivity, decision1);

		processEditor.connectActivity("isChild", decision1, childActivity);

		processEditor.connectActivity("isParent", decision1, parentActivity);

		processEditor.connectActivity("", childActivity, endNode);

		processEditor.connectActivity("", parentActivity, endNode);

		// Check if error message is hidden
		assertTrue(processEditor.noWarningsMessageDisplayed());
		
		// Check if correct model is created (6 nodes, 6 connections)
		assertTrue(processEditor.correctModelCreated(6, 6));

		// Save Process
		commonControl.save();

		// Create test specification
		processEditor.generateTestSpecification();
		
		// Assert that the test specification contains two rows
		assertTrue(processEditor.correctTestSpecificationGenerated(2));

		// Click on created process in the requirement overview
		processEditor.clickOnRelatedRequirement("Erlaubnis Autofahren");
		requirementOverview.refreshRequirementOverviewPage();
		// Assert that the related requirement is shown in the requirement overview
		assertTrue(requirementOverview.checkForRelatedRequirement());
		
		requirementOverview.clickOnCreatedProcess(processName);

		// Duplicate process
		processEditor.clickOnRelatedRequirement("Erlaubnis Autofahren");
		requirementOverview.duplicateProcess(processName);
		// Click on it, to check if the duplication created a new model
		requirementOverview.clickOnDuplicateProcess(processName);

		// Delete duplicate
		processEditor.clickOnRelatedRequirement("Erlaubnis Autofahren");
		requirementOverview.deleteDuplicateProcess(processName);
		requirementOverview.refreshRequirementOverviewPage();
		// The process should be deleted, thus, use assertFalse
		assertFalse(requirementOverview.checkForDeletedDuplicateProcess(processName));

		// Delete created process
		requirementOverview.deleteProcess(processName);
		requirementOverview.refreshRequirementOverviewPage();
		// The should should be deleted, thus, use assertFalse
		assertFalse(requirementOverview.checkForDeletedProcess(processName));

		requirementOverview.refreshRequirementOverviewPage();

		// Assert that the related requirement is not shown in the requirement overview, as we deleted the model referencing the requirement
		assertFalse(requirementOverview.checkForRelatedRequirement());
	}
}