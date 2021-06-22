package com.specmate.uitests.pagemodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Class Process Editor Elements
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
	 * creates a new start at position x,y and returns the newly created node
	 */
	public String createStart(int x, int y) {
		UITestUtil.dragAndDrop(toolbarStart, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement node = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector("g > g:nth-child(2) > g > g > foreignObject > div > div")));
		String nodeId = node.getAttribute("id");

		return nodeId;
	}

	public String createActivity(String variable, int x, int y) {
		UITestUtil.dragAndDrop(toolbarStep, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));

		WebElement node =  wait.until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector("g > g:nth-child(2) > g > g > foreignObject > div > div")));
		String nodeId = node.getAttribute("id");

		WebElement activityTextfield = driver.findElement(propertiesName);
		activityTextfield.clear();
		activityTextfield.sendKeys(variable);

		return nodeId;
	}

	public void connectActivity(String connectionCondition, String nodeId1, String nodeId2) {
		super.connectById(nodeId1, nodeId2);

		// A condition is required if the connection originated from a decision node
		if (!connectionCondition.equals("")) {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement conditionTextfield = wait.until(ExpectedConditions.visibilityOfElementLocated(propertiesCondition));
			conditionTextfield.clear();
			conditionTextfield.sendKeys(connectionCondition);
			UITestUtil.absoluteWait(1500);
		}

	}

	public void setExpectedOutcome(String outcome) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement outcomeTextfield = wait.until(ExpectedConditions.visibilityOfElementLocated(expectedOutcome));

		outcomeTextfield.clear();
		outcomeTextfield.sendKeys(outcome);
	}

	public String createEnd(int x, int y) {
		UITestUtil.dragAndDrop(toolbarEnd, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));

		WebElement node = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector("g > g:nth-child(2) > g > g > foreignObject > div > div")));
		String nodeId = node.getAttribute("id");
		return nodeId;
	}

	public String createDecison(String name, int x, int y) {
		UITestUtil.dragAndDrop(toolbarDecision, x, y, driver);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));

		WebElement node = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector("g > g:nth-child(2) > g > g > foreignObject > div > div")));
		String nodeId = node.getAttribute("id");

		WebElement decisionTextfield = driver.findElement(propertiesName);
		decisionTextfield.clear();
		decisionTextfield.sendKeys(name);

		return nodeId;
	}

	public boolean relatedRequirementDisplayed() {
		return UITestUtil.isElementPresent(By.cssSelector("tracing-link"), driver);
	}
}