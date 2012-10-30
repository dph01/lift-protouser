

package damianhelme.model

import net.liftweb._
import util._
import common._
import net.liftweb.sitemap.Loc.LocParam
import net.liftweb.sitemap.Loc.Template
import net.liftweb.sitemap.Loc.If
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.Templates
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.Loc
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.proto.Crudify
import net.liftweb.mapper.ManyToMany
import net.liftweb.mapper.MappedBoolean
import scala.xml.NodeSeq
import net.liftweb.mapper._
import scala.xml.Elem
import net.liftweb.http.SessionVar
import net.liftweb.http.RequestVar
import net.liftweb.http.CleanRequestVarOnSessionTransition
import net.liftweb.util.HttpHelpers._
import net.liftweb.common.Logger
import net.liftweb.common.Box
/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User
    //   extends MegaProtoUser[User] 
    extends LongKeyedMapper[User]
    with IdPK
    with UserIdAsString
    with ManyToMany
    with Logger {

  import net.liftweb.common.Box
  val i = Full[Int](1)
  def f = Full[Int](1) openOr (() => 0)
  def g = Some[Int](1) getOrElse 0

  def getSingleton = User // what's the "meta" server

  /* 
   * Regular User Fields 
   *
   */
  object firstName extends MappedString(this, User.firstNameLen)
  object lastName extends MappedString(this, User.lastNameLen)
  object email extends MappedEmail(this, User.emailLen) {
    override def uniqueFieldId = Some("emailMsg") // used for displaying err msg next to field
    override def dbIndexed_? = true
    override def validations = valUnique(S.??("unique.email.address")) _ :: super.validations

    /*
    override def toForm: Box[Elem] = {

      debug("in email._toForm. this.is: " + this.is )
      Full(SHtml.text( this.is, x => {debug("setting email: " + x ); this.setFromAny(x);
      debug("email now: " + this)}))
    }
    */
  }
  object password extends MappedPassword(this) {
    override def uniqueFieldId = Full("passwordMsg") // used for setting validation errors

    /* this is a bit of a hack to get the password 'repeat' field sitting nicely in the form
     * - there is no html in the signup.html file - it is all generated here */
    override def _toForm: Box[NodeSeq] = {
      S.fmapFunc({ s: List[String] => this.setFromAny(s) }) { funcName =>
        Full(
          <div class="control-group">
            <label class="control-label" for="password">Password</label>
            <div class="controls">
              <input id={ uniqueFieldId.open_! } type="password" name={ funcName } value={ is.toString }/>
              <span data-lift="Msg?id=password;errorClass=alert alert-error"></span>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label" for="repeatpassword">Repeat Password</label>
            <div class="controls">
              <input id="repeatpassword" type="password" name={ funcName } value={ is.toString }/>
            </div>
          </div>
        )
      }
    }
  }
  object superUser extends MappedBoolean(this) {
    override def defaultValue = false
  }

  /* a random string used for validation, recovering lost passwords */
  object uniqueId extends MappedUniqueId(this, User.uniqueIdLen) {
    override def dbIndexed_? = true
    override def writePermission_? = true
  }

  object validated extends MappedBoolean(this) {
    override def defaultValue = false
  }

  object locale extends MappedLocale(this) {}

  object timezone extends MappedTimeZone(this) {}

  def niceName: String = (firstName.is, lastName.is, email.is) match {
    case (f, l, e) if f.length > 1 && l.length > 1 => f + " " + l + " (" + e + ")"
    case (f, _, e) if f.length > 1                 => f + " (" + e + ")"
    case (_, l, e) if l.length > 1                 => l + " (" + e + ")"
    case (_, _, e)                                 => e
  }

  def shortName: String = (firstName.is, lastName.is) match {
    case (f, l) if f.length > 1 && l.length > 1 => f + " " + l
    case (f, _) if f.length > 1                 => f
    case (_, l) if l.length > 1                 => l
    case _                                      => email.is
  }

  def niceNameWEmailLink = <a href={ "mailto:" + email.is }>{ niceName }</a>

  def userIdAsString: String = id.is.toString

  /*
   * End of Regular User fields
   */

  // whether this user is at the centre of the hub, ie. can the upload files to all the clients etc

}

/**
 * The singleton that has methods for accessing the database
 */
object User extends User
    //with MetaMegaProtoUser[User] 
    with LongKeyedMetaMapper[User]
    with Logger {

  override def dbTableName = "users" // define the DB table name
  // override def screenWrap = Full(<lift:surround with="default" at="content">
  // <lift:bind /></lift:surround>)

  // def skipValidation = false

  def firstNameLen = 32
  def lastNameLen = 32
  def emailLen = 48
  def uniqueIdLen = 32

  // def loginPagePath = "user" :: "login" :: Nil
  // def loginPageURL = loginPagePath.mkString("/")

  /**
   * Calculate the path given a suffix by prepending the basePath to the suffix
   */
  protected def thePath(end: String): List[String] = basePath ::: List(end)

  /**
   * Return the URL of the "login" page
   */
  def loginPageURL = loginPath.mkString("/", "/", "")

  /**
   * The base path for the user related URLs.  Override this
   * method to change the base path
   */
  def basePath: List[String] = "user" :: Nil

  /**
   * The path suffix for the sign up screen
   */
  def signUpSuffix: String = "signup"

  /**
   * The computed path for the sign up screen
   */
  lazy val signUpPath = thePath(signUpSuffix)

  /**
   * The path suffix for the login screen
   */
  def loginSuffix = "login"

  /**
   * The computed path for the login screen
   */
  lazy val loginPath = thePath(loginSuffix)

  /**
   * The path suffix for the lost password screen
   */
  def lostPasswordSuffix = "lostpassword"

  /**
   * The computed path for the lost password screen
   */
  lazy val lostPasswordPath = thePath(lostPasswordSuffix)

  /**
   * The path suffix for the reset password screen
   */
  def passwordResetSuffix = "resetpassword"

  /**
   * The computed path for the reset password screen
   */
  lazy val resetPasswordPath = thePath(passwordResetSuffix)

  /**
   * The path suffix for the change password screen
   */
  def changePasswordSuffix = "changepassword"

  /**
   * The computed path for change password screen
   */
  lazy val changePasswordPath = thePath(changePasswordSuffix)

  /**
   * The path suffix for the logout screen
   */
  def logoutSuffix = "logout"

  /**
   * The computed pat for logout
   */
  lazy val logoutPath = thePath(logoutSuffix)

  /**
   * The path suffix for the edit screen
   */
  def editSuffix = "edituser"

  /**
   * The computed path for the edit screen
   */
  lazy val editPath = thePath(editSuffix)

  /**
   * The path suffix for the validate user screen
   */
  def validateUserSuffix = "validate_user"

  /**
   * The calculated path to the user validation screen
   */
  lazy val validateUserPath = thePath(validateUserSuffix)

  /**
   * The application's home page
   */
  def homePage = "/"

  /**
   * Given an username (probably email address), find the user
   */
  def findUserByUserName(email: String): Box[User] =
    find(By(this.email, email))

  /**
   * Given a unique id, find the user
   */
  def findUserByUniqueId(id: String): Box[User] =
    find(By(uniqueId, id))

  /**
   * Create a new instance of the User
   */
  protected def createNewUserInstance(): User = User.create

  /**
   * Given a String representing the User ID, find the user
   */
  def userFromStringId(id: String): Box[User] = find(id)

  /* 
   * Current User stuff
   */

  /**
   * By default, destroy the session on login.
   * Change this is some of the session information needs to
   * be preserved.
   */
  protected def destroySessionOnLogin = true

  var onLogIn: List[User => Unit] = Nil

  var onLogOut: List[Box[User] => Unit] = Nil

  /**
   * This function is given a chance to log in a user
   * programmatically when needed
   */
  var autologinFunc: Box[() => Unit] = Empty

  // is there a user logged in, if not call the autologinfunc is one exists
  def loggedIn_? = {
    if (!currentUserId.isDefined)
      for (f <- autologinFunc) f()
    currentUserId.isDefined
  }

  // Is there a user logged in and are they a superUser? 
  def superUser_? : Boolean = currentUser.map(_.superUser.is) openOr false

  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }

  def logUserIn(who: User, postLogin: () => Nothing): Nothing = {
    if (destroySessionOnLogin) {
      S.session.open_!.destroySessionAndContinueInNewSession(() => {
        logUserIn(who)
        postLogin()
      })
    } else {
      logUserIn(who)
      postLogin()
    }
  }

  def logUserIn(who: User) {
    curUserId.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUser(Full(who))
    onLogIn.foreach(_(who))
    debug("Logged user: " + who)
  }

  def logoutCurrentUser = logUserOut()

  def logUserOut() {
    onLogOut.foreach(_(curUser))
    curUserId.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }

  /**
   * If you want to redirect a user to a different page after login,
   * put the page here
   */
  object loginRedirect extends SessionVar[Box[String]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  private object curUserId extends SessionVar[Box[String]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUserId: Box[String] = curUserId.is

  private object curUser extends RequestVar[Box[User]](currentUserId.flatMap(userFromStringId))
      with CleanRequestVarOnSessionTransition {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUser: Box[User] = curUser.is

  /////////////////////////////////////////////////////////////////////////////
  //
  // Loc Stuff
  //
  /////////////////////////////////////////////////////////////////////////////
  /**
   * A Menu.LocParam to test if the user is logged in
   */
  import net.liftweb.sitemap.Loc._

  lazy val testLoggedIn = If(loggedIn_? _, (S.??("must.be.logged.in")))
  lazy val testNotLoggedIn = Unless(loggedIn_? _, (S.??("must.be.logged.in")))

  /**
   * A Menu.LocParam to test if the user is a super user
   */
  lazy val testSuperUser = If(superUser_? _, S.??("must.be.super.user"))

  // def loginMenuLoc = Menu("Login") / "user" / "login" >> User.testNotLoggedIn >> LocGroup("user") 

  def loginMenuLoc = Menu(Loc("Login", loginPath, S.??("login"),
    User.testNotLoggedIn :: LocGroup("user") :: Nil))

  def signupMenuLoc = Menu(Loc("Signup", signUpPath, S.??("sign.up"),
    User.testNotLoggedIn :: LocGroup("user") :: Nil))

  def logoutMenuLoc = Menu(Loc("Logout", logoutPath, S.??("logout"),
    User.testLoggedIn :: Template(() => doLogout) :: Nil))

  def editUserMenuLoc = Menu(Loc("EditUser", editPath, S.??("edit.user"),
    User.testLoggedIn :: Nil))

  def changePasswordMenuLoc = Menu(Loc("ChangePassword", changePasswordPath, S.??("change.password"),
    User.testLoggedIn :: Nil))

  // reset - is the link that is sent in response to the user submitting a 'lost password' form
  // the password reset request comes in with a unique id on the end
  // e.g. /user/resetpassword/K2UVO2NH51P5HT1WOA5PMCDRSM2PIS2R 
  def resetPasswordMenuLoc = Menu(Loc("ResetPassword", (resetPasswordPath, true), S.??("reset.password"),
    // we need to specify the template explicitly to stop Lift looking for a template of the name
    // K2UVO2NH51P5HT1WOA5PMCDRSM2PIS2R
    Template(() => Templates(resetPasswordPath).openOr(NodeSeq.Empty)) :: User.testNotLoggedIn :: Nil))

  // lost - sends a email to the user for when they've forgotten their password
  def lostPasswordMenuLoc = Menu(Loc("LostPassword", lostPasswordPath, S.??("lost.password"),
    LocGroup("user") :: User.testNotLoggedIn :: Nil))

  def validateUserMenuLoc = Menu(Loc("ValidateUser", (validateUserPath, true), S.??("validate.user"),
    User.testNotLoggedIn :: Template(() => validateUser(snarfLastItem)) :: Hidden :: Nil))

  protected def snarfLastItem: String =
    (for (r <- S.request) yield r.path.wholePath.last) openOr ""

  def doLogout: NodeSeq = {
    logoutCurrentUser
    S.redirectTo(homePage)
  }

  def validateUser(id: String): NodeSeq = {
    // the validate user request comes in with a unique id on the end
    // e.g. /user/resetpassword/K2UVO2NH51P5HT1WOA5PMCDRSM2PIS2R 
    // val id = (for (r <- S.request) yield r.path.wholePath.last) openOr ""

    User.findUserByUniqueId(id) match {
      case Full(user) if !user.validated =>
        user.validated(true).uniqueId.reset().save
        User.logUserIn(user, () => {
          S.notice(S.??("account.validated"))
          S.redirectTo(User.homePage)
        })

      case _ => S.error(S.??("invalid.validation.link")); S.redirectTo(User.homePage)
    }
  }
  /*
   *  email stuff
   */
  def skipEmailValidation = false
  def signupMailSubject = S.??("sign.up.confirmation")
  def emailFrom = "noreply@" + S.hostName
  def bccEmail: Box[String] = Empty
  def passwordResetEmailSubject = S.??("reset.password.request")
  /**
   * The string that's generated when the user name is not found.  By
   * default: S.??("email.address.not.found")
   */
  def userNameNotFoundString: String = S.??("email.address.not.found")

  // override def loginXhtml : Elem = Templates("user" :: "login" :: Nil) map(_.asInstanceOf[Elem]) openOr <b>Login Template missing</b> 
  // override def signupXhtml(user: TheUserType) : Elem = Templates("user" :: "signup" :: Nil) map(_.asInstanceOf[Elem]) openOr <b>Signup Template missing</b> 

  // define the order fields will appear in forms and output
  // override def fieldOrder = List(id, firstName, lastName, email, isMediaAdmin, isClient, locale, timezone, password)
  // override def signupFields = List(firstName, lastName, email, isMediaAdmin, isClient, locale, timezone, password)
  // override def editFields = List(firstName, lastName, email, isMediaAdmin, isClient, locale, timezone, password)

  // comment this line out to require email validations
  // override def skipEmailValidation = true

  // specify where to go after login
  // loginRedirect(Some("xxx"))

  // override def homePage = "/medialist"
  /*
  import _root_.net.liftweb.sitemap.Loc.strToFailMsg
  val MustBeSuperUser = If(() => { 
	  		User.currentUser.map{ u => 
	  			// debug("MustBeSuperUser for: " + u.email + " = " + u.superUser); 
	  			u.superUser.is
	  		}.openOr(false)
  }, "")

  override def loginMenuLocParams = LocGroup("user") :: super.loginMenuLocParams
  override def createUserMenuLocParams = LocGroup("user") :: super.createUserMenuLocParams
  override def lostPasswordMenuLocParams = LocGroup("user") :: super.lostPasswordMenuLocParams 
  
  override def actionsAfterSignup(theUser: TheUserType, func: () => Nothing): Nothing = {
    theUser.setValidated(skipEmailValidation).resetUniqueId()
    theUser.save
    if (!skipEmailValidation) {
      sendValidationEmail(theUser)
      S.notice(S.??("sign.up.message"))
      func()
    } else {
    	//logUserIn(theUser, () => {      
    	// S.notice(S.??("welcome"))
      func() //  } //) } } */

}
