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
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.{Handler, Server}
import java.util.concurrent.TimeUnit
import net.liftweb.common.Box
import net.liftweb.util.Helpers.tryo
import scala.collection.JavaConversions.asScalaBuffer
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.By
import net.liftweb.common.Full

class blah extends FunSpec  {
  describe("my blah") {
    it("blah blah") {
      assert(User.f === 1)
      assert(User.g === 1)
    }
    
  }
}

@RunWith(classOf[JUnitRunner])
class UserTest extends FunSpec with BeforeAndAfterAll with Logger{
  
    private var server : Server       = null
    private var selenium : WebDriver  = null
    private val GUI_PORT              = 8081
    private val host                  = "http://localhost:" + GUI_PORT.toString


    override def beforeAll() {
    // Setting up the jetty instance which will be running the
    // GUI for the duration of the tests
    server  = new Server(GUI_PORT)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")
//    server.addHandler(context)
    server.setHandler(context)
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
  val homePageTitle = "Proto User Home"
  val editPageTitle = "App: Edit Event"
  val deletePageTitle = "App: Delete Event"
   
  // error messages we're expecting
  val emailUnique = "The email address must be unique"
  val invalidEmail = "Invalid email address"
  val passwordTooShort = "Password too short"
  val passwordsDontMatch = "Passwords do not match"
 
    
  describe("user signup screen") {
    
    // happy cases:
    it("should load and display all expected fields and they should be blank") {
      selenium.get(signupPageURL)
      // field verification is done in the constructor
      val signupPage = new SignupPage(selenium)
      
      // check all fields against their expected fields and their values
      expect(""){signupPage.firstName.getAttribute("Value")}
      expect(""){signupPage.lastName.getAttribute("Value")}
      expect(""){signupPage.email.getAttribute("Value")}
      expect(""){signupPage.emailMsg.getAttribute("Value")}
      expect(""){signupPage.password.getAttribute("Value")}
      expect(""){signupPage.passwordMsg.getAttribute("Value")}
      expect(""){signupPage.repeatPassword.getText}
    }
    
    it("should submit OK when all fields filled in properly") {
      selenium.get(signupPageURL)
      val signupPage = new SignupPage(selenium)
      
      val firstName = "Fred"
      val lastName = "Bloggs"
      val email = "fred@acme.com"
      val password = "mypass"
      val repeatPassword = "mypass"
        
      signupPage.setFirstName(firstName)
      signupPage.setLastName(lastName)
      signupPage.setEmail(email)
      signupPage.setPassword(password)
      signupPage.setRepeatPassword(repeatPassword)
      
      signupPage.submit
      
      // should be back to the home page now
      val homePage = new HomePage(selenium)
      
      // make sure that we've got the right messages displayed
      homePage.containsNotice("You have signed up.  A validation email message will be sent to you.")
      
      // check we've got a user in the database with the correct values
      val user = User.findUserByUserName(email).openOrThrowException("Could not find user with email: " + email)
      expect(firstName){user.firstName}
      expect(lastName){user.lastName}
      expect(email){user.email}
      assert(user.password.match_?(password),"passwords do not match")
      
    }
    
    /*
     * error cases
     */
    
    it("""should generate appropriate error msgs when submitting form when all fields are blank""") { 
      selenium.get(signupPageURL)
      val signupPage = new SignupPage(selenium)
 
      // submit with all fields blank
      signupPage.submit
      
      // should take us back to the create page with an error messages
      val signupPage2 = new SignupPage(selenium)
      
       // check all fields against their expected fields and their values
      expect(""){signupPage2.firstName.getText}
      expect(""){signupPage2.lastName.getText}
      expect(""){signupPage2.email.getText}
      expect(invalidEmail){signupPage2.emailMsg.getText}
      expect(""){signupPage2.password.getText}
      expect(passwordTooShort){signupPage2.passwordMsg.getText}
      expect(""){signupPage2.repeatPassword.getText}
    }
    
    it("should generate an appropriate error msg on invalid email address") { 
      selenium.get(signupPageURL)
      val signupPage = new SignupPage(selenium)
 
      // submit with all fields blank
      signupPage.setEmail("blah")
      signupPage.setPassword("password")
      signupPage.setRepeatPassword("password")
      signupPage.submit
      
      // should give an error message
      val signupPage2 = new SignupPage(selenium)
      
      // check all fields against their expected fields and their values
      expect(""){signupPage2.firstName.getText}
      expect(""){signupPage2.lastName.getText}
      expect(""){signupPage2.email.getText}
      expect(invalidEmail){signupPage2.emailMsg.getText}
      expect(""){signupPage2.password.getText}
      expect(""){signupPage2.passwordMsg.getText}
      expect(""){signupPage2.repeatPassword.getText}
    }
    
    it("should generate a 'must be unique' error message when address already exists in db") { 
    
      val firstName = "Fred"
      val lastName = "Bloggs"
      val email = "fred1@acme.com"
      val password = "mypass"
      val repeatPassword = password
 
      // first, create a user directly into the database
      val user = User.create.firstName(firstName).lastName(lastName)
                      .email(email).saveMe
      user.password.setList(password :: password :: Nil)
      user.save
      
      selenium.get(signupPageURL)
      
      // second, try to create the same user through the signup page
      val signupPage = new SignupPage(selenium)
      
      signupPage.setFirstName(firstName)
      signupPage.setLastName(lastName)
      signupPage.setEmail(email)
      signupPage.setPassword(password)
      signupPage.setRepeatPassword(repeatPassword)
      
      signupPage.submit
      
      // should be back to signup page with an error message
      //debug("blah : " + selenium.getPageSource)
      val signupPage2 = new SignupPage(selenium)
      
      // check all fields against their expected fields and their values
      expect(firstName){signupPage2.firstName.getAttribute("Value")}
      expect(lastName){signupPage2.lastName.getAttribute("Value")}
      expect(email){signupPage2.email.getAttribute("Value")}
      expect(emailUnique){signupPage2.emailMsg.getText}
      expect(password){signupPage2.password.getAttribute("Value")}
      expect(""){signupPage2.passwordMsg.getText}
      expect(repeatPassword){signupPage2.repeatPassword.getAttribute("Value")}
    }
    
    // password field tests
    it("should generate a 'too short' message when both passwords are the same and less than 5 chars") { 
      val firstName = "Fred"
      val lastName = "Bloggs"
      val email = "fred2@acme.com"
      val password = "xx"
      val repeatPassword = password
 
     selenium.get(signupPageURL)
      
      // second, try to create the same user through the signup page
      val signupPage = new SignupPage(selenium)
      
      signupPage.setFirstName(firstName)
      signupPage.setLastName(lastName)
      signupPage.setEmail(email)
      signupPage.setPassword(password)
      signupPage.setRepeatPassword(repeatPassword)
      
      signupPage.submit
      
      // should be back to signup page with an error message
      val signupPage2 = new SignupPage(selenium)
      
      // check all fields against their expected fields and their values
      expect(firstName){signupPage2.firstName.getAttribute("Value")}
      expect(lastName){signupPage2.lastName.getAttribute("Value")}
      expect(email){signupPage2.email.getAttribute("Value")}
      expect(""){signupPage2.emailMsg.getText}
      expect(password){signupPage2.password.getAttribute("Value")}
      expect(passwordTooShort){signupPage2.passwordMsg.getText}
      expect(repeatPassword){signupPage2.repeatPassword.getAttribute("Value")}
    }
    it("should generate a 'don't match' message when passwords are different") { 
      val firstName = "Fred"
      val lastName = "Bloggs"
      val email = "fred3@acme.com"
      val password = "mypass"
      val repeatPassword = "asdfasdf"
 
      selenium.get(signupPageURL)
      
      val signupPage = new SignupPage(selenium)
      
      signupPage.setFirstName(firstName)
      signupPage.setLastName(lastName)
      signupPage.setEmail(email)
      signupPage.setPassword(password)
      signupPage.setRepeatPassword(repeatPassword)
      
      signupPage.submit
      
      // should be back to signup page with an error message
      val signupPage2 = new SignupPage(selenium)
      
      // check all fields against their expected fields and their values
      expect(firstName){signupPage2.firstName.getAttribute("Value")}
      expect(lastName){signupPage2.lastName.getAttribute("Value")}
      expect(email){signupPage2.email.getAttribute("Value")}
      expect(""){signupPage2.emailMsg.getText}
      expect(password){signupPage2.password.getAttribute("Value")}
      expect(passwordsDontMatch){signupPage2.passwordMsg.getText}
      expect(repeatPassword){signupPage2.repeatPassword.getAttribute("Value")}
     }

    it("should give a 'logout first' message if you try to signup when already logged in") { 
      val firstName = "Fred"
      val lastName = "Bloggs"
      val email = "fred4@acme.com"
      val password = "mypass"
      val repeatPassword = password
 
      // first, create a user directly into the database and log them in
      val user = User.create.firstName(firstName).lastName(lastName)
                      .email(email).saveMe
      user.password.setList(password :: password :: Nil)
      user.save
      User.logUserIn(user)
      
      selenium.get(signupPageURL)
      
      // second, got to signup page
      val signupPage = new SignupPage(selenium)
     
    }
    
    it("sigup menu item should be visible when the user isn't logged in") { pending }
  }
  
  describe("user login screen") {
    it("should login when username valid and password matches that in database") { pending }
    it("should give 'invalid' message when username valid and retain entered username") { pending }
    it("should give 'invalid' message when password does not match and retain entered password") { pending }
    it("should give a 'logout first' message if you try to login when already logged in") { pending }
    it("login menu item should be visible when the user isn't logged in") { pending }
    it("login menu item should not be visible when the user is logged in") { pending }
  }
  
  describe("logout") {
    it("should log the user out and take them back to the home page") { pending }
    it("should give a 'use needs to be logged in' message if you try to log out when already logged out") { pending }
    it("login menu item should be visible when the user is logged in") { pending }
    it("login menu item should not be visible when no user is logged in") { pending }
    
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
  
  class SignupPage( val sel: WebDriver ) extends pageNotices {
    
    //debug("signup page is: " + sel.getPageSource)
    
     // check that we are on the right page
    expect(signupPageTitle) {sel.getTitle}
    
    private def getElement(name: String): WebElement = tryo(sel.findElement(By.id(name))).openOrThrowException("could not find element: " + name)
    
    // ensure we have the right input elements
    val firstName: WebElement = getElement("firstname")
    val lastName: WebElement = getElement("lastname") 
    val email: WebElement = getElement("email")
    val emailMsg: WebElement = getElement("emailMsg")
    val password: WebElement = getElement("password")
    val passwordMsg: WebElement = getElement("passwordMsg")
    val repeatPassword: WebElement = getElement("repeatpassword")
    
    def setFirstName(firstNameText: String) : Unit = firstName.sendKeys(firstNameText)
    def setLastName(lastNameText: String) : Unit = lastName.sendKeys(lastNameText)
    def setEmail(emailText: String) : Unit = email.sendKeys(emailText)
    def setPassword(passwordText: String) : Unit = password.sendKeys(passwordText)
    def setRepeatPassword(repeatPasswordText: String) : Unit = repeatPassword.sendKeys(repeatPasswordText)
    
    def submit : Unit = firstName.submit
  }
  
  class HomePage( val sel: WebDriver ) extends pageNotices {
    
    // debug("home page is: " + sel.getPageSource)
     // not going to check anything other than that we are on the right page
    expect(homePageTitle) {sel.getTitle}

  }
  
    // a trait to mix into other pages to enable us to check if specified text appears as 
  // either error, 
  trait pageNotices {
      def sel: WebDriver
      def errorMsgs : Box[WebElement] = tryo(selenium.findElement(By.id("lift__noticesContainer___error")))
      def noticeMsgs : Box[WebElement] = tryo(selenium.findElement(By.id("lift__noticesContainer___notice")))
      def warningMsgs : Box[WebElement] = tryo(selenium.findElement(By.id("lift__noticesContainer___warning")))
      
      // check to see if any of the error message contains a specified text
      def containsErrorText(text: String) : Boolean = {
        val errorLIs : scala.collection.mutable.Buffer[WebElement] = {
          // needed to split this out in order to convince the scala type checker
          val emptyList = new java.util.Vector[WebElement]
          if ( errorMsgs.isEmpty) emptyList else  errorMsgs.map(x => x.findElements(By.tagName("li"))).openOrThrowException("could not find 'li' elements in error messages")
        }
        // iterate over the error list elements testing to see if the text of any contains the input text
        errorLIs.map(_.getText).exists(s => s.contains(text))
      }
      
      def containsText(msgs: () => Box[WebElement], text: String) : Boolean = {
        val msgLIs:  Box[scala.collection.mutable.Buffer[WebElement]] = {
          msgs().map( m => {
            val lis: scala.collection.mutable.Buffer[WebElement] = m.findElements(By.tagName("li"))
            lis
          }) 
        }
        msgLIs.map(x => x.map( _.getText).exists( _.contains(text) )).openOr(false)
      }
      
      def containsNotice(text: String) = containsText(noticeMsgs _, text)
      def containsError(text: String) = containsText(errorMsgs _, text)
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
          //debug("topLevelErrorSpan: " + topLevelErrorSpan)
          val ret = topLevelErrorSpan.map(x => {
            // this variable added for force an implicit conversion from java.util.list to mutable buffer
            val z : scala.collection.mutable.Buffer[WebElement] = x.findElements(By.className("alert-error"))
            //debug("z: " + z)
            z
          })
          //debug("ret: " + ret)
          ret
        }
        
        // if the innerErrorSpans exist, do any of them contain our text?
        innerErrorSpans.map(buffer => {
          //debug("buffer: " + buffer)
          buffer.map( _.getText).exists( s => {  s.contains(text)})
        }).openOr(false)
      }
      
      def errorText(fieldId: String) : String = {
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
          val ret = topLevelErrorSpan.map(x => {
            // this variable added for force an implicit conversion from java.util.list to mutable buffer
            val z : scala.collection.mutable.Buffer[WebElement] = x.findElements(By.className("alert-error"))
            z
          })
          ret
        }
        
        // if the innerErrorSpans exist, take the first one (assuming there is only one)
        innerErrorSpans.map(buffer => {
          buffer.map( _.getText).head
        }).openOr("")
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