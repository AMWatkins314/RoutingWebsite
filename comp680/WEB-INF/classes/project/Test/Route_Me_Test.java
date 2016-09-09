import java.io.*;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/*******
* Written by AWatkins
* A selenium program to auto-test Route_Me using test input from TestLocations.txt
* If test needs slowed, use:
* try { Thread.sleep(1000); }
* catch(InterruptedException e){ System.out.println("Unable to sleep for 1 second: " + e); }
*******/
public class Route_Me_Test {

   public static void main(String[] args) {

      // Create a new instance of the Firefox driver
      WebDriver firefoxDriver = new FirefoxDriver();

      // Run the tests on the Firefox browser
      runTest(firefoxDriver);

      // Set the path to the Chrome driver executable
      System.setProperty("webdriver.chrome.driver", "/Applications/chromedriver");

      // Create a new instance of the Chrome driver
      WebDriver chromeDriver = new ChromeDriver();

      // Run the tests on the Chrome browser
      runTest(chromeDriver);


      // Create a new instance of the Safari driver
      WebDriver safariDriver = new SafariDriver();

      // Run the tests on the Safari browser
      runTest(safariDriver);

      // Shut down the selenium tests gracefully after waiting 1 minute to view the routes 
      try { Thread.sleep(60000); }
      catch(InterruptedException e){ System.out.println("Unable to sleep for 1 minute: " + e); }
      firefoxDriver.quit();
      chromeDriver.quit();
      safariDriver.quit();
   }

   public static void runTest(WebDriver driver){

      int loc_ctr = 1;
      String add_location = "loc_";
      String rm_location = "remove_loc_";

      // The name of the file to open
      String fileName = "TestLocations.txt";

      // This will reference one line at a time
      String line = null;

      //  Wait For Page To Load
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

      // Navigate to URL
      //driver.get("http://localhost:9999/comp680/Route_Me.html");
      driver.get("http://ec2-52-39-197-45.us-west-2.compute.amazonaws.com/comp680/Route_Me.html");

      // Maximize the window.
      driver.manage().window().maximize();

      // Try to add more addresses with no start/end address or first location address to get error
      driver.findElement(By.id("addMoreLocations")).click();

      // Submit with no start/end address or first location address to get error
      driver.findElement(By.id("Submit")).click();

      // Add a start/end address
      driver.findElement(By.id("startLoc")).sendKeys("7303 Madora Ave, Winnetka CA");

      // Try to add more addresses with no first location address to get error
      driver.findElement(By.id("addMoreLocations")).click();

      // Submit with no first location address to get error
      driver.findElement(By.id("Submit")).click();

      // Remove the starting location 
      driver.findElement(By.id("startLoc")).clear();
      driver.findElement(By.id("loc_0")).sendKeys("19545 Sherman Way, Reseda CA");

      // Try to add more addresses with no start/end address to get error
      driver.findElement(By.id("addMoreLocations")).click();

      // Submit with no start/end address to get error
      driver.findElement(By.id("Submit")).click();

      // Add a bad start/end address
      driver.findElement(By.id("startLoc")).sendKeys("12345abcde");

      // Submit with bad start/end address and bad first location address to get error
      driver.findElement(By.id("Submit")).click();

      // Add a good start/end address and a good first location
      driver.findElement(By.id("startLoc")).clear();
      driver.findElement(By.id("startLoc")).sendKeys("7303 Madora Ave, Winnetka CA");

      try {
         FileReader fileReader = new FileReader(fileName);
         BufferedReader bufferedReader = new BufferedReader(fileReader);

         // Add locations
         while((line = bufferedReader.readLine()) != null) {
            String id = add_location.concat(Integer.toString(loc_ctr));
            driver.findElement(By.id("addMoreLocations")).click();
            driver.findElement(By.id(id)).sendKeys(line);
            loc_ctr++;
         }
         bufferedReader.close();
      }
      catch(FileNotFoundException ex) {
         System.out.println("Unable to open file '" + fileName + "'");                
      }
      catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'"); 
      }

      // Remove locations
      for(int i=1; i<loc_ctr; i=i+3){
         String id = rm_location.concat(Integer.toString(i));
         driver.findElement(By.id(id)).click();
      }

      // Submit all of the addresses for processing
      driver.findElement(By.id("Submit")).click();
      driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);

   }
}
