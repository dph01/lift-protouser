package damianhelme.snippet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.Stack
import scala.collection.mutable.Stack
import net.liftweb.common.{Logger}
import net.liftweb.mapper.DB
import damianhelme.model.User
import net.liftweb.http.{LiftSession,S}
import net.liftweb.util.StringHelpers
import net.liftweb.common.Empty
import net.liftweb.http.{LiftRules,Req, Html5Properties}
import org.scalatest.BeforeAndAfterAll
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.Server
import java.util.concurrent.TimeUnit
import net.liftweb.common.Box
import net.liftweb.util.Helpers.tryo
import scala.collection.JavaConversions.asScalaBuffer
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.By
import net.liftweb.common.Full


@RunWith(classOf[JUnitRunner])
class UserTest extends FunSpec with BeforeAndAfterAll with Logger{
  
    private var server : Server       = null
    private var selenium : WebDriver  = null
    private val GUI_PORT              = 8081
    private var host                  = "http://localhost:" + GUI_PORT.toString


    override def beforeAll() {
    // Setting up the jetty instance which will be running the
    // GUI for the duration of the tests
    server  = new Server(GUI_PORT)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")
    server.addHandler(context)
    server.start()

    // Setting up the Selenium Client for the duration of the tests
    selenium = new HtmlUnitDriver();
    selenium.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
  }

  override def afterAll() {
    // Close everyhing when done
    selenium.close()
    server.stop()
  }

  // val listPageURL = host + "/event/listevent"
  val signupPageURL = host + "/user/signup"
  // val editPageURL = host + "/event/editevent"
  // val viewPageURL = host + "/event/viewevent"
  // val deletePageURL = host + "/event/deleteevent"
  // val listPageTitle = "App: List Events"
  // val viewPageTitle = "App: View Event"
  val signupPageTitle = "Proto User Sign Up"
  val editPageTitle = "App: Edit Event"
  val deletePageTitle = "App: Delete Event"
   
  describe("user signup screen") {
    
    it("should load and display all expected fields") {
      selenium.get(signupPageURL)
      // field verification is done in the constructor
      val signupPage = new createSignupPage(selenium)
    }
    
    // email address field tests
    it("""should generate an 'invalid email' & 'password must be set' if form is submitted when
        all fields are blank""") { 
      selenium.get(signupPageURL)
      val signupPage = new createSignupPage(selenium)
 
      // submit with username and password fields blank
      signupPage.submit
      
      // should give an error message
      val signupPage2 = new createSignupPage(selenium)
      ("emailMsg","Invalid email address") :: ("password", "Password must be set") :: Nil foreach(x => {
        assert(signupPage2.containsErrorText(x._1, x._2), "error field: " + x._1 + " does not contain: " + x._2)
      })
      // assert(signupPage2.containsErrorText("emailMsg", "Invalid email address"),
      // assert(signupPage2.containsErrorText("password", "Password must be set") === true)
    }
    
    it("should generate an 'invalid' error message on empty email address") { pending }
    it("should generate a 'must be unique' error message when address already exists in db") { pending }
    
    // password field tests
    it("should generate a 'too short' message when both passwords are missing") { pending }
    it("should generate a 'too short' message when both passwords are the same and less than 5 chars") { pending }
    it("should generate a 'don't match' message when passwords are different") { pending }

    it("should give a 'logout first' message if you try to signup when already logged in") { pending }
    it("sigup menu item should be visible when the user isn't logged in") { pending }
  }
  
  describe("user login screen") {
    it("should login when username valid and password matches that in database") { pending }
    it("should give 'invalid' message when username valid and retain entered username") { pending }
    it("should give 'invalid' message when password does not match and retain entered password") { pending }
    it("should give a 'logout first' message if you try to login when already logged in") { pending }
    it("login menu item should be visible when the user isn't logged in") { pending }
  }
  
  describe("logout") {
    it("should log the user out and take them back to the home page") { pending }
    it("should give a 'use needs to be logged in' message if you try to log out when already logged out") { pending }
    it("login menu item should only be visible when the user is logged in") { pending }
    
  }
  
  describe("edit user screen") {
    it("should save the changes and redirect to home page") { pending }
    it("should give an 'invalid email' message on a invalid email, re-presenting the changes the user made")
       { pending }
    it("edit menu item should only be visible when the user is logged in") { pending }
    it("should give a 'use needs to be logged in' message if you try to access screen when not logged in") { pending }
    
  }
  
  describe("lost password screen") {
    it("should send an email containing a password reset link when user enters a valid email") { pending }
    it("should give a 'unknown user' message when the email address entered does not match any in the database") { pending }
  }
  
  class createSignupPage( val sel: WebDriver ) extends pageNotices {
    
     // check that we are on the right page
    expect(signupPageTitle) {sel.getTitle}
    
    // ensure we have the right input elements
    val firstName: Box[WebElement] = tryo(sel.findElement(By.id("firstname")))
    assert(!firstName.isEmpty)
    val lastName: Box[WebElement] = tryo(sel.findElement(By.id("lastname")))
    assert(!lastName.isEmpty)
    val email: Box[WebElement] = tryo(sel.findElement(By.id("email")))
    assert(!email.isEmpty)
    val password: Box[WebElement] = tryo(sel.findElement(By.id("password")))
    assert(!password.isEmpty)
    val repeatPassword: Box[WebElement] = tryo(sel.findElement(By.id("repeatpassword")))
    assert(!repeatPassword.isEmpty)
    
    def setFirstName(firstNameText: String) : Unit = firstName.foreach( _.sendKeys(firstNameText))
    def setLastName(lastNameText: String) : Unit = lastName.foreach( _.sendKeys(lastNameText))
    def setEmail(emailText: String) : Unit = email.foreach( _.sendKeys(emailText))
    def setPassword(passwordText: String) : Unit = firstName.foreach( _.sendKeys(passwordText))
    def setRepeatPassword(repeatPasswordText: String) : Unit = firstName.foreach( _.sendKeys(repeatPasswordText))
    
    def submit : Unit = firstName.foreach(_.submit)
  }
  
    // a trait to mix into other pages to enable us to check if specified text appears as 
  // either error, 
  trait pageNotices {
      def sel: WebDriver
      def errorNotices : Box[WebElement] = tryo(selenium.findElement(By.id("lift__noticesContainer___error")))
      
      // check to see if any of the error message contains a specified text
      def containsErrorText(text: String) : Boolean = {
        val errorLIs : scala.collection.mutable.Buffer[WebElement] = {
          // needed to split this out in order to convince the scala type checker
          val emptyList = new java.util.Vector[WebElement]
          if ( errorNotices.isEmpty) emptyList else  errorNotices.map(x => x.findElements(By.tagName("li"))).open_!
        }
        // iterate over the error list elements testing to see if the text of any contains the input text
        errorLIs.map(_.getText).exists(s => s.contains(text))
      }
      
      /**
       * Is there an error message given for a specific field. E.g.
       * <div class="controls">
          <input id="email" name="F1026983738518U0COT4" type="text" value="" maxlength="48"> 
          <span id="emailMsg">
            <span class="alert alert-error">Invalid email address</span>
          </span>
        </div>
       */
      def containsErrorText(fieldId: String, text: String) : Boolean = {
        /* first see if we have an error span for this id, e.g. should return
         * <span id="emailMsg">
         *   <span class="alert alert-error">Invalid email address</span>
         * </span>
         */
        def topLevelErrorSpan : Box[WebElement] = tryo(selenium.findElement(By.id(fieldId)))
        
        /* second, do any of the inner error spans have the searched for error text
         *   <span class="alert alert-error">Invalid email address</span>
         */
        def innerErrorSpans: Box[scala.collection.mutable.Buffer[WebElement] ] = {
          debug("topLevelErrorSpan: " + topLevelErrorSpan)
          val ret = topLevelErrorSpan.map(x => {
            // this variable added for force an implicit conversion from java.util.list to mutable buffer
            val z : scala.collection.mutable.Buffer[WebElement] = x.findElements(By.className("alert-error"))
            debug("z: " + z)
            z
          })
          debug("ret: " + ret)
          ret
        }
        
        // if the innerErrorSpans exist, do any of them contain our text
        innerErrorSpans.map(buffer => {
          debug("buffer: " + buffer)
          buffer.map( _.getText).exists( s => { debug("testing: " + s + " for: " + text); s.contains(text)})
        }).openOr(false)
      }
  }
    
}

@RunWith(classOf[JUnitRunner])
class UserOpsTest extends FunSpec with Logger {
  
  val session : LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
  
  override def withFixture(test: NoArgTest) {
    // set up your db here
    try {
      LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
      S.initIfUninitted(session) {
        // val user : User = User.create
        // user.firstName("XXX")
        // user.lastName("YYYY")
        // user.save
        // User.logUserIn(user)
        test() // Invoke the test function
      }
    }
    finally {
      // tear down your db here
    }
  }
  
  describe("signupMailBody") {
    it("should find tempalte and substitute username and link") {
      val email = "fred@damianhelme.com"
      val link = "http://www.somelink.com"
        
      val user = User.create.email(email).firstName("Damian").lastName("Helme")
      val userOps = new UserOps 
      val emailBody = userOps.signupMailBody(user, link)
      
      val expected = <html> <head> <title>Media Hub Account Validation</title> </head> <body>
        <p>Dear Damian Helme <br></br> <br></br>
              Please click on the link below to complete your registration with MediaHub:<br></br>
            <a href="http://www.somelink.com" id="validationLink">dummy link</a> <br></br> <br></br>
            Regards <br></br> The Media Hub Team </p> </body></html>
      assert( scala.xml.Utility.trim(expected) === scala.xml.Utility.trim(emailBody))
    }
  }
  
  describe("resetPasswordMailBody") {
      it("should find tempalte and substitute username and link") {
      val email = "fred@damianhelme.com"
      val link = "http://www.somelink.com"
        
      val user = User.create.email(email).firstName("Damian").lastName("Helme")
      val userOps = new UserOps 
      val emailBody = userOps.passwordResetMailBody(user, link)
      // debug("password reset mail body: " + emailBody)
      val expected = <html><head> 
        <title>Media Hub Reset Password</title> 
        </head> <body>
          <p> Dear Damian Helme <br></br> <br></br>
          Please click on the link below to reset you MediaHub password:<br></br>
          <a id="resetPasswordLink" href="http://www.somelink.com">http://www.somelink.com</a> <br></br> <br></br>
          Regards
          <br></br>
          The Media Hub Team
          </p> </body></html>
        
      assert( scala.xml.Utility.trim(expected) === scala.xml.Utility.trim(emailBody))
    } 
  }
}