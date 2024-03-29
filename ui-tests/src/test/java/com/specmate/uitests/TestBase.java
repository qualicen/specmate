package com.specmate.uitests;

import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import com.specmate.uitests.pagemodel.LoginElements;
import com.specmate.uitests.pagemodel.UITestUtil;

@Ignore
@RunWith(Parameterized.class)
public class TestBase implements SauceOnDemandSessionIdProvider {
	public static String username = System.getenv("SAUCE_USERNAME");
	public static String accesskey = System.getenv("SAUCE_ACCESS_KEY");
	public static String seleniumURI;
	public static String buildTag;
	public static final String tunnelidentifier = System.getenv("TRAVIS_JOB_NUMBER");

	public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

	/** Mark the Sauce Job as passed/failed when the test succeeds or fails */
	@Rule
	public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

	@Rule
	public TestName name = new TestName() {
		public String getMethodName() {
			return String.format("%s", super.getMethodName());
		}
	};

	protected String browser;
	protected String os;
	protected String version;
	protected String deviceName;
	protected String deviceOrientation;
	protected String sessionId;
	protected WebDriver driver;

	/** Constructor for test instances */
	public TestBase(String os, String version, String browser, String deviceName, String deviceOrientation) {
		super();
		this.os = os;
		this.version = version;
		this.browser = browser;
		this.deviceName = deviceName;
		this.deviceOrientation = deviceOrientation;
	}

	/** Browser configurations */
	@Parameters
	public static LinkedList<String[]> browsersStrings() {
		LinkedList<String[]> browsers = new LinkedList<String[]>();

		browsers.add(new String[] { "Windows 10", "93.0", "Chrome", null, null });
		// browsers.add(new String[]{"Windows 10", "18.17763", "MicrosoftEdge", null,
		// null});
		// browsers.add(new String[]{"Windows 10", "73.0", "firefox", null, null});
		return browsers;
	}

	@Before
	public void setUp() throws Exception {
		// TODO change this to true for local test execution. The path to the chromedriver must be adapted
		boolean isLocalTest = false;
		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
		capabilities.setCapability(CapabilityType.VERSION, version);
		capabilities.setCapability("deviceName", deviceName);
		capabilities.setCapability("device-orientation", deviceOrientation);
		capabilities.setCapability("platform", os);
		capabilities.setCapability("tunnel-identifier", "github-action-tunnel-" + System.getenv("GITHUB_RUN_NUMBER"));

		String methodName = name.getMethodName();
		capabilities.setCapability("name", methodName);

		if (buildTag != null) {
			capabilities.setCapability("build", buildTag);
		}
		if (!isLocalTest) {
			this.driver = new RemoteWebDriver(
					new URL("https://" + username + ":" + accesskey + seleniumURI + "/wd/hub"), capabilities);
		} else {
			// TODO Adapt the path
			// The version of the driver must be the same as the browser, download here: https://chromedriver.chromium.org/downloads
			System.setProperty("webdriver.chrome.driver", "Q:\\chromedriver.exe");
			driver = new ChromeDriver();
		}

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

		if (!isLocalTest) {
			this.sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();
		}
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
	}

	public String getSessionId() {
		return sessionId;
	}

	@BeforeClass
	public static void setupClass() {
		// Get the uri to send the commands to
		seleniumURI = "@ondemand.saucelabs.com:443";

		// Set the buildTag to the Github Action number
		buildTag = "#" + System.getenv("GITHUB_RUN_NUMBER");
		if (buildTag == null) {
			buildTag = System.getenv("SAUCE_BUILD_NAME");
		}
	}

	public void performLogin(LoginElements login) {
		UITestUtil.waitForProjectsToLoad(driver);
		login.username("username");
		login.password("password");
		login.changeToEnglish();
		login.changeToGerman();
		login.changeToProject("test-data");
		login.login();
	}
}
