
package com.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


abstract class WebOptions extends MobileOptions {
    public static Logger log = LogManager.getLogger(WebOptions.class);
    /**
     * get chrome options
     *
     * @param perf perf option
     * @return chrome
     */
    protected ChromeOptions getChromeOptions(String perf) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(SystemUtils.IS_OS_LINUX);
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-popup-blocking");
        //options.addArguments(setChromeOWASP());
        //options.addArguments("--incognito");
        //options.addArguments("--disable-extensions");
        //options.addArguments("--dns-prefetch-disable");
        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        if (perf.equalsIgnoreCase("YES")) {
            options.merge(DriverController.performance());
        }
        log.info("Chrome options added");
        return options;
    }

    /**
     * Get firefox options
     *
     * @return options
     */
    protected FirefoxOptions getFirefoxOptions() {
        WebDriverManager.firefoxdriver().setup();
        System.setProperty(FirefoxDriver.Capability.MARIONETTE, "true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        FirefoxOptions options = new FirefoxOptions();
        FirefoxProfile profile = new FirefoxProfile();
        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(false);
        profile.setPreference("network.proxy.type", 0);
        options.setHeadless(SystemUtils.IS_OS_LINUX);
        options.setCapability(FirefoxDriver.Capability.PROFILE, profile);
        //setFirefoxOWASP(options);
        log.info("Firefox options added");
        return options;
    }

    /**
     * Get Edge Options
     *
     * @return options
     */
    protected EdgeOptions getEdgeOptions() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions chromeOptions = new EdgeOptions();
        chromeOptions.setHeadless(true);
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--disable-popup-blocking");
   /*     chromeOptions.setBinary(
                "C:\\Program Files (x86)\\Microsoft\\Edge Dev\\Application\\msedge.exe");
        return new EdgeOptions().merge(chromeOptions);*/
        return chromeOptions;
    }
    protected SafariOptions getSafariOptions(String perf) {
        WebDriverManager.safaridriver().setup();
        SafariOptions options = new SafariOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.setAcceptInsecureCerts(true);
        options.setCapability("safari.cleanSession", true);

        if (perf.equalsIgnoreCase("YES")) {
            options.merge(DriverController.performance());
        }
        log.info("Chrome options added");
        return options;
    }

    protected ChromeOptions getOperaOptions(String perf) {
        WebDriverManager.operadriver().setup();
         ChromeOptions options = new ChromeOptions();
        options.setHeadless(SystemUtils.IS_OS_LINUX);
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-popup-blocking");
        //options.addArguments(setChromeOWASP());
        //options.addArguments("--incognito");
        //options.addArguments("--disable-extensions");
        //options.addArguments("--dns-prefetch-disable");
        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        if (perf.equalsIgnoreCase("YES")) {
            options.merge(DriverController.performance());
        }
        return  options;
    }
    /**
     * Get Browser options
     *
     * @param browser browser
     * @param perf    perf
     * @return browserOption
     */
    protected MutableCapabilities getBrowserOptions(String browser, String perf) {
        switch (browser) {
            case "firefox":
                return getFirefoxOptions();
            case "chrome":
                return getChromeOptions(perf);
            case "safari":
                return getSafariOptions(perf);
            case "opera":
                return getOperaOptions(perf);

            default:
                log.error("No browser option provided");
        }
        return null;
    }

    /**
     * Cloud capabilities
     *
     * @param browser browser
     */
    protected DesiredCapabilities addCloudCapabilities(String browser) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        switch (browser) {
            case "chrome":
                capabilities.setCapability("browserName", "chrome");
                capabilities.setCapability("browserVersion", "90");
                capabilities.setCapability("platform", "windows");
                log.info("Adding aws chrome capabilities");
                break;
            case "firefox":
                capabilities.setCapability("browserName", "firefox");
                capabilities.setCapability("browserVersion", "88");
                capabilities.setCapability("platform", "windows");
                log.info("Adding aws firefox capabilities");
                break;
            case "edge":
                capabilities.setCapability("browserName", "edge");
                capabilities.setCapability("browserVersion", "90");
                capabilities.setCapability("platform", "windows");
                log.info("Adding aws firefox capabilities");
                break;
            default:
                log.info("No supported browser provided");
                break;
        }
        return capabilities;
    }

    /**
     * Add browserstack capabilities
     */
    protected DesiredCapabilities addBrowserStackCapabilities(String browser, String testName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("os", "Windows");
        capabilities.setCapability("os_version", "10");
        capabilities.setCapability("build", "HybridTestFramework");
        capabilities.setCapability("name", testName);
        switch (browser) {
            case "chrome":
                capabilities.setCapability("browser", "Chrome");
                capabilities.setCapability("browser_version", "90.0");
                break;
            case "firefox":
                capabilities.setCapability("browser", "Firefox");
                capabilities.setCapability("browser_version", "88.0");
                break;
            case "edge":
                capabilities.setCapability("browser", "Edge");
                capabilities.setCapability("browser_version", "90.0");
                break;
            default:
                log.info("browser selection is required");
                break;
        }
        return capabilities;
    }

    /**
     * logging preference
     *
     * @return prefs
     */
    private LoggingPreferences pref() {
        LoggingPreferences pref = new LoggingPreferences();
        pref.enable(LogType.BROWSER, Level.OFF);
        pref.enable(LogType.CLIENT, Level.OFF);
        pref.enable(LogType.DRIVER, Level.OFF);
        pref.enable(LogType.PERFORMANCE, Level.OFF);
        pref.enable(LogType.PROFILER, Level.OFF);
        pref.enable(LogType.SERVER, Level.OFF);
        log.info("Performance capability added");
        return pref;
    }

    /**
     * Set chrome for OWASP
     *
     * @return chromeOptions
     */
    private List<String> setChromeOWASP() {
        List<String> chromeOWASP = new ArrayList<>();
        chromeOWASP.add("--proxy-server=http://localhost:8082");
        chromeOWASP.add("--ignore-certificate-errors");
        log.info("OWASP for chrome added");
        return chromeOWASP;
    }

    /**
     * Set firefox for OWASP
     *
     * @param options firefox options
     * @return firefox options
     */
    private FirefoxOptions setFirefoxOWASP(FirefoxOptions options) {
        options.addPreference("network.proxy.type", 1);
        options.addPreference("network.proxy.http", "localhost");
        options.addPreference("network.proxy.http_port", 8082);
        options.addPreference("network.proxy.share_proxy_settings", true);
        options.addPreference("network.proxy.no_proxies_on", "");
        log.info("OWASP for firefox added");
        return options;
    }

    /**
     * Set firefox profile
     *
     * @return capability
     */
    private DesiredCapabilities fireFoxProfile() {
        ProfilesIni allProfiles = new ProfilesIni();
        FirefoxProfile myProfile = allProfiles.getProfile("WebDriver");
        if (myProfile == null) {
            File ffDir = new File(System.getProperty("user.dir") + File.separator + "ffProfile");
            if (!ffDir.exists()) {
                ffDir.mkdir();
            }
            myProfile = new FirefoxProfile(ffDir);
        }
        myProfile.setAcceptUntrustedCertificates(true);
        myProfile.setAssumeUntrustedCertificateIssuer(true);
        myProfile.setPreference("webdriver.load.strategy", "unstable");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(FirefoxDriver.SystemProperty.BROWSER_PROFILE, myProfile);
        return capabilities;
    }

}
