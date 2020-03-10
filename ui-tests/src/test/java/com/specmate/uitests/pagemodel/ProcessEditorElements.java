package com.specmate.uitests.pagemodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Class
 * Process Editor Elements
 */
public class ProcessEditorElements extends EditorElements {

	By toolbarStep = By.id("toolbar-tools.addStep-button");
	By toolbarDecision = By.id("toolbar-tools.addDecision-button");
	By toolbarStart = By.id("toolbar-tools.addStart-button");
	By toolbarEnd = By.id("toolbar-tools.addEnd-button");
	By toolbarConnection = By.id("toolbar-tools.addProcessConnection-button");
	
	By expectedOutcome = By.id("properties-expectedOutcome-textfield");
	
	By nodeSelector = By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']");
	
	public ProcessEditorElements(WebDriver driver, Actions builder) {
		super(driver, builder);
	}
	
	/**
	 * creates a new start with corresponding variable and condition at position x,y
	 * and returns the newly created node
	 */
	public int createStart(int x, int y) {
		
		int index = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")).size();

		UITestUtil.dragAndDrop(toolbarStart, x, y, driver);

		return index;
	}
	
	public int createActivity(String variable, int x, int y) {

		int index = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")).size();
		
		UITestUtil.dragAndDrop(toolbarStep, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));


		WebElement activityTextfield = driver.findElement(propertiesName);
		activityTextfield.clear();
		activityTextfield.sendKeys(variable);

		return index;
	}
	
	public int connectActivity(String connectionCondition, int node1, int node2) {
		int connectionIndex = super.connect(node1, node2, nodeSelector);
		
		// A condition is required if the connection originated from a decision node
		WebElement conditionTextfield = driver.findElement(propertiesCondition);
		conditionTextfield.clear();
		conditionTextfield.sendKeys(connectionCondition);
		
		return connectionIndex; 
	}
	
	public void setExpectedOutcome(String outcome) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(expectedOutcome));
		WebElement outcomeTextfield = driver.findElement(expectedOutcome);
		
		outcomeTextfield.clear();
		outcomeTextfield.sendKeys(outcome);
	}
	
	public int createEnd(int x, int y) {

		int index = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")).size();
		
		UITestUtil.dragAndDrop(toolbarEnd, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));

		return index;
	}
	
	public int createDecison(String name, int x, int y) {

		int numberOfDecisions = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")).size();

		UITestUtil.dragAndDrop(toolbarDecision, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));


		WebElement decisionTextfield = driver.findElement(propertiesName);
		decisionTextfield.clear();
		decisionTextfield.sendKeys(name);

		return numberOfDecisions;
	}

	public boolean relatedRequirementDisplayed() {
		return UITestUtil.isElementPresent(By.cssSelector("tracing-link"), driver);
	}
}