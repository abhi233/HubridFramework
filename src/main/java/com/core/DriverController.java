
package com.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.config.AppConfig;
//import com.reporting.listeners.WebDriverEventHandler;
import com.typesafe.config.ConfigFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.devicefarm.DeviceFarmClient;
import software.amazon.awssdk.services.devicefarm.model.CreateTestGridUrlRequest;
import software.amazon.awssdk.services.devicefarm.model.CreateTestGridUrlResponse;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;


import static com.aventstack.extentreports.MediaEntityBuilder.createScreenCaptureFromPath;

//@Slf4j
public class DriverController extends WebOptions {
    private static final AppConfig appConfig = new AppConfig(ConfigFactory.load());
    private static WebDriver driverThread = null;
    private static AppiumDriver mobileThread = null;
    private static BrowserMobProxyServer proxy;
    private final String browserstack_username = System.getenv("BROWSERSTACK_USERNAME");
    private final String browserstack_access_key = System.getenv("BROWSERSTACK_ACCESS_KEY");
    DriverService appiumService = null;
    //WebDriverListener driverListener = new WebDriverEventHandler();
    private String testName = null;
    public static ExtentReports extent;
    public static ExtentTest test;
    public static ITestResult result;
    public static Logger log = LogManager.getLogger(DriverController.class);



    /**
     * Added performance capability
     *
     * @return capabilities
     */
    protected static DesiredCapabilities performance() {
        proxy = new BrowserMobProxyServer();
        proxy.start();
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        try {
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            seleniumProxy.setHttpProxy(hostIp + ":" + proxy.getPort());
            seleniumProxy.setSslProxy(hostIp + ":" + proxy.getPort());
        } catch (Exception e) {
        }
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.newHar("TestPerformance");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY, seleniumProxy);
        return caps;
    }

    @Parameters({"type", "browser", "device", "grid", "perf"})
    @BeforeClass
    public void setup(String type, String browser, String device, String grid, String perf) {
        testName = this.getClass().getName().substring(24);
        switch (type) {
            case "web":
                initWebDriver(browser, grid, perf);
                break;
            case "mobile":
                initMobileDriver(device, grid);
                break;
            default:
                log.info("select test type to proceed with one testing");
                break;
        }
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public WebDriver getWebDriver() {
        return driverThread;
    }

    public AppiumDriver getMobileDriver() {
        return mobileThread;
    }

    /**
     * Initialize web driver
     *
     * @param browser browser
     * @param grid    grid
     * @param perf    perf
     */
    private synchronized void initWebDriver(String browser, String grid, String perf) {
        DeviceFarmClient client = DeviceFarmClient.builder().region(Region.AP_SOUTHEAST_2).build();
        CreateTestGridUrlRequest request = CreateTestGridUrlRequest.builder()
                .expiresInSeconds(300)
                .projectArn("arn:aws:devicefarm:ap-southeast-2:111122223333:testgrid-project:1111111-2222-3333-4444-555555555")
                .build();
        try {
            switch (grid) {
                case "aws":
                    log.info("Make sure that the environment variables AWS_ACCESS_KEY and AWS_SECRET_KEY are configured in your testing environment.");
                    CreateTestGridUrlResponse response = client.createTestGridUrl(request);
                    driverThread =
                          new RemoteWebDriver(new URL(response.url()), addCloudCapabilities(browser));
                    log.info("Grid client setup for AWS Device farm successful");
                    break;
                case "docker":
                    log.info("Make sure that docker containers are up and running");
                    driverThread = new RemoteWebDriver(URI.create("http://localhost:4445/wd/hub").toURL(), getBrowserOptions(browser, perf));
                    log.info("Grid client setup for Docker containers successful");
                    break;
                case "browserstack":
                    log.info("Make sure that browserstack configs provided");
                    driverThread = new RemoteWebDriver(new URL("https://" + browserstack_username + ":" + browserstack_access_key + "@hub-cloud.browserstack.com/wd/hub"), addBrowserStackCapabilities(browser, testName));
                    log.info("Grid client setup for browserstack successful");
                    break;
                case "local":
                    switch (browser) {
                        case "firefox":
                            driverThread = new FirefoxDriver(getFirefoxOptions());
                            log.info("Initiating firefox driver");
                            break;
                        case "chrome":
                            driverThread = new ChromeDriver(getChromeOptions(perf));
                            log.info("Initiating chrome driver");
                            break;
                        case "edge":
                            driverThread = new EdgeDriver(getEdgeOptions());
                            log.info("Initiating edge driver");
                            break;
                        case "safari":
                            driverThread = new SafariDriver(getSafariOptions(perf));
                            log.info("Initiating edge driver");
                            break;
                  /*      case "opera":
                            driverThread = new opera(getOperaOptions(perf));
                            log.info("Initiating edge driver");
                            break;*/
                        default:
                            log.info("Browser listed not supported");
                            break;
                    }
                default:
                    log.info("Running in local docker container");
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Initialize mobile driver
     *
     * @param device device
     */
    private synchronized void initMobileDriver(String device, String cloud) {
        try {
            switch (device) {
                case "NEXUS":
                    log.info("Selected device is NEXUS");
                    caps.setCapability(MobileCapabilityType.UDID, "NEXUS");
                    caps.setCapability(MobileCapabilityType.DEVICE_NAME, "NEXUS");
                    androidCapabilities(caps);
                    cloudCapabilities(cloud, caps, "NEXUS");
                    mobileThread = new AndroidDriver(createURL(cloud), caps);
                    break;
                case "PIXEL":
                    log.info("Selected device is PIXEL");
                    caps.setCapability(MobileCapabilityType.UDID, "PIXEL");
                    caps.setCapability(MobileCapabilityType.DEVICE_NAME, "PIXEL");
                    androidCapabilities(caps);
                    cloudCapabilities(cloud, caps, "PIXEL");
                    mobileThread = new AndroidDriver(createURL(cloud), caps);
                    break;
                case "samsung":
                    log.info("Selected device is SAMSUNG");
                    cloudCapabilities(cloud, caps, "samsung");
                    androidCapabilities(caps);
                    mobileThread = new AndroidDriver(createURL(cloud), caps);
                    break;
                case "iPhone12":
                    log.info("Selected device is IPHONE");
                    cloudCapabilities(cloud, caps, "iPhone12");
                    iosCapabilities(caps);
                    mobileThread = new IOSDriver(createURL(cloud), caps);
                    break;
                case "IPHONE":
                    log.info("Selected device is IPHONE");
                    caps.setCapability(MobileCapabilityType.UDID, "Pixel");
                    caps.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixel");
                    iosCapabilities(caps);
                    cloudCapabilities(cloud, caps, "IPHONE");
                    mobileThread = new IOSDriver(createURL(cloud), caps);
                    break;
                case "EMULATOR":
                    log.info("Selected device is EMULATOR");
                    appiumService = createAppiumService();
                    caps.setCapability(MobileCapabilityType.UDID, "emulator-5554");
                    caps.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixel");
                    caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
                    caps.setCapability(MobileCapabilityType.APP, "D:\\Softwares\\Ashwak\\Selenium_Framework\\HybridTestFramework\\src\\main\\resources\\General-Store.apk");
                    appiumService.start();
                    mobileThread = new AndroidDriver(createURL(cloud) ,caps);
                    break;
                default:
                    log.info("Required device selection");
                    break;
            }
        } catch (NullPointerException | IOException ex) {
            log.error("Appium driver could not be initialised for device", ex);
        }
    }

    @AfterClass
    public void tearDown() throws FileNotFoundException {
        try {
            Har har = proxy.getHar();
            FileOutputStream fos = new FileOutputStream("Reports\\performance\\" + testName + ".har");
            har.writeTo(fos);
            proxy.stop();
            log.info("Performance reports will be available at Report folder");
        } catch (Exception e) {
            log.info("Performance tests not included");
        } finally {
            if (driverThread != null) {
                driverThread.quit();
            } else {
                mobileThread.quit();
                if (appiumService != null) {
                    appiumService.stop();
                    stopAppiumServer();
                }
            }
        }
    }
    @BeforeSuite(alwaysRun = true)
    public void extentReportSetUp() {
        try {
            //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String path = System.getProperty("user.dir") + "//reports//Automation//TestResult"
                    + now.toString().replaceAll(":", "-") + ".html";
            ExtentSparkReporter reporter = new ExtentSparkReporter(path);
            reporter.config().setReportName("Automation Results");
            reporter.config().setDocumentTitle("Test Results");

            extent = new ExtentReports();
            extent.attachReporter(reporter);
            extent.setSystemInfo("Tester", System.getProperty("user.name"));

            extent.setSystemInfo("Machine", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            log.error("Can not invoke extent report");
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void getResult1(ITestResult result) {
        test = extent.createTest(result.getMethod().getMethodName());

    }

    @Parameters({"type"})
    @AfterMethod(alwaysRun = true)
    public void getResult(ITestResult result,String type) throws AWTException, InterruptedException {
        try {

            if (result.getStatus() == ITestResult.FAILURE) {
                test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test Case Failed", ExtentColor.RED));
                test.log(Status.FAIL, MarkupHelper.createLabel(result.getThrowable() + " - Test Case Failed", ExtentColor.RED));
                String screenshotPath;
                if(type.equals("web")) {
                    screenshotPath = getScreenshotPath(result.getMethod().getMethodName(), driverThread);

                }
                else {
                    screenshotPath = getScreenshotPath(result.getMethod().getMethodName(), mobileThread);
                }
                test.fail(result.getName() + " - Test Case Failed",
                        createScreenCaptureFromPath(screenshotPath).build());
            } else if (result.getStatus() == ITestResult.SKIP) {
                test.log(Status.SKIP,
                        MarkupHelper.createLabel(result.getName() + " - Test Case Skipped", ExtentColor.ORANGE));
            } else if (result.getStatus() == ITestResult.SUCCESS) {

                test.log(Status.PASS,
                        MarkupHelper.createLabel(result.getName() + " Test Case PASSED", ExtentColor.GREEN));
            }

            extent.flush();

        } catch (Exception e) {
            log.error("can not create extent report");
        }

    }
    public static String getScreenshotPath(String testCaseName, WebDriver driver) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String destination = System.getProperty("user.dir") + "/reports/screenshot/" + testCaseName + "_"
                    + ThreadLocalRandom.current().nextInt() + ".png";
            FileUtils.copyFile(source, new File(destination));
            return destination;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

