// DEPRECATED because why didn't I use the API in the first place???
/*
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimesScraper {

    private final WebDriver driver;
    private final JavascriptExecutor js;

    public TimesScraper() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("webSocketUrl", true);
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
    }

    public void scrapeTimes(String lineName, String fromPoint, String toPoint) {
        driver.get("https://tfl.gov.uk/tube/timetable/" + lineName + "?FromId=" + fromPoint + "&ToId=" + toPoint);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        //WebElement cookieButton = driver.findElement(By.className("cb-buttons-grid-3"));
        //cookieButton.click();
        js.executeScript("acceptAllCookies();");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        WebElement firstTrainLink = driver.findElement(By.className("specific-departure"));
        firstTrainLink.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        List<WebElement> timesElementList = driver.findElements(By.className("time"));
        List<String> timesStringList = new ArrayList<>();
        for (WebElement time : timesElementList) {
            timesStringList.add(time.getText());
        }
        for (int i = 0; i < timesStringList.size(); i++) {
            if (Objects.equals(timesStringList.get(i), timesStringList.get(i + 1))) {
                timesStringList.remove(i);
            }
        }
        System.out.println(timesStringList);
        driver.quit();
    }
}
*/