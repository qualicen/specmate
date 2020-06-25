package com.specmate.uitests.pagemodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Class
 * CEG Editor Elements
 */
public class CEGEditorElements extends EditorElements {

	/**Editor Elements and their locators*/
	By toolbarNode = By.id("toolbar-tools.addCegNode-button");

	/**Property Editor Elements and their locators*/
	By propertiesVariable = By.id("properties-variable-textfield");
	By propertiesCondition = By.id("properties-condition-textfield");
	By propertiesType = By.id("properties-type-dropdown");
	By TypeAND = By.id("type-AND");
	By TypeOR = By.id("type-OR");
	
	By nodeSelector = By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;'] > rect");


	public CEGEditorElements(WebDriver driver, Actions builder) { // constructor
		super(driver, builder);
	}

	/**
	 * creates a new node with corresponding variable and condition at position x,y
	 * and returns the newly created node
	 */
	public int createNode(String variable, String condition, int x, int y) {

		int numberOfNodes = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")).size();
		
		
		// Click node button and drag and drop to editorview 

		UITestUtil.dragAndDrop(toolbarNode, x, y, driver);
		

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;']")));

		WebElement variableTextfield = driver.findElement(propertiesVariable);
		WebElement conditionTextfield = driver.findElement(propertiesCondition);
		variableTextfield.clear();
		variableTextfield.sendKeys(variable);
		conditionTextfield.clear();
		conditionTextfield.sendKeys(condition);

		return numberOfNodes;
	}

	/**
	 * establishes a connection from node1 to node2 and returns the newly created
	 * connection
	 */
	public int connectNode(int node1, int node2) {
		return super.connect(node1, node2, nodeSelector);
	}
	
	public void toggleNegateButtonOn(WebElement connection) {
		// Chose the Select tool from the toolbar in order to be able to select a connection
		
		connection.click();
		
		// Assert, that the connection is selected 
		if(!UITestUtil.isElementPresent(By.cssSelector(".form-check-input"), driver)){
			connection.click();
		}
			
		WebDriverWait wait = new WebDriverWait(driver, 10);
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-check-input")));
		driver.findElement(By.cssSelector(".form-check-input")).click();
	}
	
	public void toggleNegateButtonOnLastConnection() {
			
		WebDriverWait wait = new WebDriverWait(driver, 10);
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-check-input")));
		driver.findElement(By.cssSelector(".form-check-input")).click();
	}
	
	public boolean negationDisplayed() {
		return UITestUtil.isElementPresent(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;'] > path:nth-child(2)[stroke-dasharray='6 6']"), driver);
	}
	
	public boolean checkUndoConnection() {
		int numberOfConnections = driver.findElements(By.cssSelector("g > g:nth-child(2) > g[style*='visibility: visible;'] > path:nth-child(2)"))
				.size();
		return numberOfConnections == 1; 
	}

	public void clearButCancel() {
		driver.findElement(toolbarClear).click();
		driver.findElement(cancel).click();
	}
	
	public void changeTypeToANDInNode(int node) {
		WebElement nodeElement = UITestUtil.getElementWithIndex(node, driver, nodeSelector);
		builder.moveToElement(nodeElement, 0, 25).click().build().perform();
		driver.findElement(By.cssSelector("g > g > g > foreignObject > div > select")).click();
		driver.findElement(By.cssSelector("g > g > g > foreignObject > div > select > option[value=AND]")).click();
		
	}
	
	public void changeTypeToORInNode(int node) {
		WebElement nodeElement = UITestUtil.getElementWithIndex(node, driver, nodeSelector);
		builder.moveToElement(nodeElement, 0, 25).click().build().perform();
		driver.findElement(By.cssSelector("g > g > g > foreignObject > div > select")).click();
		driver.findElement(By.cssSelector("g > g > g > foreignObject > div > select > option[value=OR]")).click();
	}

	public void changeTypeToAND(int node) {
		// Click two times as clicking only once will minimize the node
		WebElement nodeElement = UITestUtil.getElementWithIndex(node, driver, nodeSelector);
		builder.moveToElement(nodeElement, 0, 25).click().build().perform();
		driver.findElement(propertiesType).click();
		driver.findElement(TypeAND).click();
	}

	public void changeTypeToOR(int node) {
		// Click two times as clicking only once will minimize the node
		WebElement nodeElement = UITestUtil.getElementWithIndex(node, driver, nodeSelector);
		builder.moveToElement(nodeElement, 0, 25).click().build().perform();
		driver.findElement(propertiesType).click();
		driver.findElement(TypeOR).click();
	}
}