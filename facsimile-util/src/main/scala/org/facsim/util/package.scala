//======================================================================================================================
// Facsimile: A Discrete-Event Simulation Library
// Copyright © 2004-2020, Michael J Allen.
//
// This file is part of Facsimile.
//
// Facsimile is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
// version.
//
// Facsimile is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
// warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
// details.
//
// You should have received a copy of the GNU Lesser General Public License along with Facsimile. If not, see:
//
//   http://www.gnu.org/licenses/lgpl.
//
// The developers welcome all comments, suggestions and offers of assistance. For further information, please visit the
// project home page at:
//
//   http://facsim.org/
//
// Thank you for your interest in the Facsimile project!
//
// IMPORTANT NOTE: All patches (modifications to existing files and/or the addition of new files) submitted for
// inclusion as part of the official Facsimile code base, must comply with the published Facsimile Coding Standards. If
// your code fails to comply with the standard, then your patches will be rejected. For further information, please
// visit the coding standards at:
//
//   http://facsim.org/Documentation/CodingStandards/
//======================================================================================================================

//======================================================================================================================
// Scala source file belonging to the org.facsim.util package.
//======================================================================================================================
package org.facsim

// The Scala "macros" language feature is currently experimental, and needs to be enabled via the import statement
// below.
import java.io.File
import java.net.{URI, URL}
import java.time.ZonedDateTime
import java.util.jar.JarFile
import java.util.{Date, GregorianCalendar}
import scala.annotation.elidable
import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.blackbox.Context
import scala.util.matching.Regex

/** ''[[http://facsim.org/ Facsimile]]'' Simulation Utility Library.
 *
 *  Package providing miscellaneous utility elements required by the ''Facsimile'' simulation library, made available
 *  separately in case they are of use for non-simulation activities.
 *
 *  @since 0.0
 */
package object util {

  /** Regular expression to match class argument name. */
  private val ClassArgRE = """^\w+\.this\.(\w+)$""".r

  /** Regular expression for identifying periods in package path names. */
  private val PeriodRegEx = """(\.)""".r

  /** Regular expression for extracting a ''jar'' file ''URI'' from a ''URL''.
   *
   *  ''jar URL''s are typically of the form:
   *
   *  `jar:file:/''path/to/jar/file.jar''!/''some/package/classname''.class`.
   *
   *  For example, in ''Java'' 8, the URL for the [[String]] class might look like this (on an ''Ubuntu'' system):
   *
   *  `jar:file:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar!/java/lang/String.class`
   *
   *  such that `file:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar` is the URI for the ''JAR'' file that contains
   *  this class.
   *
   *  @note Such URLs are reported for types packaged as ''JAR'' files; this may not be the case for types packages in
   *  other formats.
   */
  private val JarUriRegEx = """^jar\:(.+)\!.+$""".r

  /** Regular expression for extracting a ''Java module name'' from a ''URL''.
   *
   *  ''jrt URL''s are typically of the form:
   *
   *  `jrt:/''module-name''/''some/package/classname''.class`.
   *
   *  For example, in ''Java'' 11, the URL for the [[String]] class might look like this:
   *
   *  `jrt:/java.base/java/lang/String.class`
   *
   *  such that `java.base` is the module-bane that contains this class.
   *
   *  @note Such URLs are reported for types packaged as ''JIMAGE'' files; this may not be the case for types packaged
   *  in other formats.
   */
  private val JrtModuleNameRegEx = """^jrt\:/(java\.[a-z_]+)/.+$""".r

  /** File separator. */
  private[facsim] val FS: String = File.separator

  /** ''JAR'' file class file separator. */
  private[facsim] val JFS: String = "/"

  /** Path separator */
  private[facsim] val PS: String = File.pathSeparator

  /** Line separator. */
  private[facsim] val LS: String = sys.props("line.separator")

  /** Single quote character. */
  private[facsim] val SQ: String = "'"

  /** Double quote character. */
  private[facsim] val DQ: String = "\""

  /** Comparison return value implying less than. */
  private[facsim] val CompareLessThan = -1

  /** Comparison return value implying equality. */
  private[facsim] val CompareEqualTo = 0

  /** Comparison return value implying greater than. */
  private[facsim] val CompareGreaterThan = 1

  /** Key for assertNonNull string resource. */
  private[facsim] val AssertNonNullKey = "assertNonNull"

  /** Key for requireNonNull string resource. */
  private[facsim] val RequireNonNullKey = "requireNonNull"

  /** Key for requireValid string resource. */
  private[facsim] val RequireValidKey = "requireValid"

  /** Key for requireFinite string resource. */
  private[facsim] val RequireFiniteKey = "requireFinite"

  /** Implicit conversion of a `[[java.time.ZonedDateTime ZonedDateTime]]` to a `[[java.util.Date Date]]`.
   *
   *  Conversion between pre-''Java 1.8'' `java.util` time classes (such as `Date`, `[[java.util.GregorianCalendar
   *  GregorianCalendar]]`, etc.) and the new post-''Java 1.8'' `java.time` time classes (`[[java.time.Instant
   *  Instant]]`, `ZonedDateTime`, etc) is cumbersome at best. The former could be dispensed with completely if it
   *  wasn't for the fact that `[[java.text.MessageFormat MessageFormat]]` currently supports only the `Date` class.
   *  This function makes working with the new time classes, and text message formatting, a little more straightforward.
   *
   *  @param date Date, expressed as a `ZonedDateTime` to be converted.
   *
   *  @return `date` expressed as a `Date`.
   *
   *  @throws scala.NullPointerException if `date` is null.
   *
   *  @throws scala.IllegalArgumentException if `date` is too large to represent as a `GregorianCalendar` value.
   */
  private[facsim] implicit def toDate(date: ZonedDateTime): Date = GregorianCalendar.from(date).getTime

  /** Obtain the resource ''URL'' associated with a class's type information.
   *
   *  @param elementType Element type instance for which a resource ''URL'' will be sought.
   *
   *  @return Resource ''URL'' associated with `elementType` wrapped in `[[scala.Some Some]]`, or `[[scala.None None]]`
   *  if the element type's resource URL could not be identified.
   */
  private[util] def resourceUrl(elementType: Class[_]): Option[URL] = {

    // (BTW, this is a rather convoluted process. If you know of a better (i.e. simpler or quicker) approach, feel free
    // to implement it...)
    //
    // Retrieve the name of the class, and convert it into a resource path. To do this, we need to prefix it with a
    // slash, replace all periods with slashes and add a ".class" extension.
    //
    // NOTE: DO NOT use the system-dependent separator character, as only slashes (not backslashes, as on Windows) are
    // acceptaed. We quote the replacement string (the slash) just in case it contains characters that require quoting.
    //
    // Note: The Class[T].getSimpleName method crashes for some Scala elements. This is a known bug. Refer to
    // [[https://issues.scala-lang.org/browse/SI-2034 Scala Issue SI-2034]] for further details.
    val name = elementType.getName
    val path = JFS + PeriodRegEx.replaceAllIn(name, Regex.quoteReplacement(JFS)) + ".class"

    // Now retrieve the resource URL for this element path and wrap it in an Option
    Option(elementType.getResource(path))
  } ensuring(_ ne null) //scalastyle:ignore null

  /** Obtain the manifest associated with the specified element type.
   *
   *  @param elementType Element type instance for which a manifest will be sought.
   *
   *  @return Manifest associated with `elementType`.
   */
  private[util] def manifestOf(elementType: Class[_]): Manifest = {

    // If a URL could not be identified, return the null manifest. Otherwise, process the resulting URL.
    resourceUrl(elementType).fold[Manifest](NullManifest) {url =>
      url.toString match {

        // If the URL identifies a JAR file, then it will be of the (String) form:
        //
        // jar:file:/{path-of-jar-file}!/{element-path}
        //
        // The jar file URI (file:/{path-of-jar-file}) is extracted from this URL, in the form of a string, and used to
        // create a new JAR file instance. The Java manifest is then retrieved, and used to populate the returned
        // Facsimile Manifest instance.
        //
        // Note: As of Java 9, the Java runtime library is no longer packaged in a JAR file, and so URLs for Java
        // standard runtime classes will not resolve as having JAR file URLs.
        case JarUriRegEx(uri) => {

          // Retrieve the manifest for the indicated JAR file.
          val jManifest = Option(new JarFile(new File(new URI(uri))).getManifest)

          // If the JAR file has no manifest, then use the null manifest, otherwise construct a new JARManifest from the
          // JAR manifest.
          jManifest.fold[Manifest](NullManifest)(new JARManifest(_))
        }

        // If the URL identifies a class belonging to a module in a Java image file (JIMAGE), then the URL will be of
        // the (String) form:
        //
        // jrt:/{module-name}/{element-path}
        //
        // For example, the java.lang.String class has the URL (from Java 9 onwards):
        //
        // jrt:/java.base/java/lang/String.class
        //
        // If the module name begins "java.", then return the JREManifest, which simulates a JAR-type manifest.
        //
        // Note: URLs of this form are only returned if using Java 9+ runtimes.
        case JrtModuleNameRegEx(_) => JREManifest

        // If there's no match on the URL, report a null manifest instead.
        case _ => NullManifest
      }
    }
  }

  /** Assertion that a value is not null.
   *
   *  Code using this assertion is only generated if the `-Xelide-below` Scala compiler option is at least ASSERTION.
   *
   *  @note Assertions should only be used to verify internal state; they must '''never''' be used to verify external
   *  state (use the require methods to verify external state instead).
   *
   *  @param arg Argument whose value is to be compared to `null`.
   *
   *  @throws java.lang.AssertionError if `arg` is `null`
   *
   *  @since 0.0
   */
  @elidable(elidable.ASSERTION)
  def assertNonNull(arg: AnyRef): Unit = macro assertNonNullImpl

  /** Require that argument value is non-`null`.
   *
   *  Throw a `[[scala.NullPointerException NullPointerException]]` if supplied argument value is `null`.
   *
   *  Normally, a `NullPointerException` will be thrown by the ''Java'' virtual machine (''JVM'') if an attempt is made
   *  to dereference a `null` pointer. However, if a function takes an object reference argument and that argument is
   *  not dereferenced until after the function has returned, then the function must verify that the reference is
   *  non-`null` as one of its preconditions; this function makes such precondition verification simpler.
   *
   *  Furthermore, even if the ''JVM'' can be relied upon to throw this exception, performing this verification
   *  explicitly is regarded as good practice. One reason is that exceptions thrown by the ''JVM'' provide limited
   *  explanation to the user as to their cause; this function provides an explanation automatically.
   *
   *  @note This is a non-macro version of `[[org.facsim.util.requireNonNull(AnyRef)* requireNonNull(AnRef)]]` for use
   *  within the ''facsimile-util'' project.
   *
   *  @param arg Argument whose value is to be compared to `null`.
   *
   *  @param name Name of the argument whose value is being tested.
   *
   *  @throws scala.NullPointerException if `arg` is `null`.
   */
  @inline
  private[util] def requireNonNullFn(arg: AnyRef, name: => String): Unit = {
    if(arg eq null) { //scalastyle:ignore null
      throw new NullPointerException(LibResource(RequireNonNullKey, name))
    }
  }

  /** Require that argument value is valid.
   *
   *  Throw a `[[scala.IllegalArgumentException IllegalArgumentException]]` if supplied parameter value is invalid.
   *
   *  @note This function supersedes the `[[scala.Predef Predef]]` `require` methods.
   *
   *  @note Tests for non-`null` argument values should be verified by the `requireNonNull` function.
   *
   *  @note This is a non-macro version of `[[org.facsim.util.requireValid(Any,Boolean)* requireValid(Any,Boolean)]]`
   *  for use within the ''facsimile-util'' project.
   *
   *  @tparam T Type of argument value.
   *
   *  @param arg Value of the argument being tested.
   *
   *  @param isValid Predicate determining the validity of `arg`. If `true`, function merely returns; if `false` an
   *  `IllegalArgumentException` is raised.
   *
   *  @param name Name of the argument being tested.
   *
   *  @throws scala.IllegalArgumentException if `isValid` is `false`.
   */
  @inline
  private[util] def requireValidFn[T](arg: T, isValid: T => Boolean, name: => String): Unit = {
    if(!isValid(arg)) throw new IllegalArgumentException(LibResource(RequireValidKey, name, arg))
  }

  /** Require that argument value is non-`null`.
   *
   *  Throw a `[[scala.NullPointerException NullPointerException]]` if supplied argument value is `null`.
   *
   *  Normally, a `NullPointerException` will be thrown by the ''Java'' virtual machine (''JVM'') if an attempt is made
   *  to dereference a `null` pointer. However, if a function takes an object reference argument and that argument is
   *  not dereferenced until after the function has returned, then the function must verify that the reference is
   *  non-`null` as one of its preconditions; this function makes such precondition verification simpler.
   *
   *  Furthermore, even if the ''JVM'' can be relied upon to throw this exception, performing this verification
   *  explicitly is regarded as good practice. One reason is that exceptions thrown by the ''JVM'' provide limited
   *  explanation to the user as to their cause; this function provides an explanation automatically.
   *
   *  @param arg Argument whose value is to be compared to `null`.
   *
   *  @throws scala.NullPointerException if `arg` is `null`.
   *
   *  @since 0.0
   */
  def requireNonNull(arg: AnyRef): Unit = macro requireNonNullImpl

  /** Require that argument value is valid.
   *
   *  Throw a `[[scala.IllegalArgumentException IllegalArgumentException]]` if supplied parameter value is invalid.
   *
   *  @note This function supersedes the `[[scala.Predef Predef]]` `require` methods.
   *
   *  @note Tests for non-`null` argument values should be verified by the `[[org.facsim.util.requireNonNull(AnyRef)*
   *  requireNonNull(AnyRef)]]` function.
   *
   *  @param arg Argument being verified.
   *
   *  @param isValid Flag representing the result of a condition determining the validity of `arg`. If `true`, function
   *  merely returns; if `false` an `IllegalArgumentException` is raised.
   *
   *  @throws scala.IllegalArgumentException if `isValid` is `false`.
   *
   *  @since 0.0
   */
  def requireValid(arg: Any, isValid: Boolean): Unit = macro requireValidImpl

  /** Require a finite double value.
   *
   *  Double arguments that equate to `NaN` (''not a number'') or ''infinity'' will result in a
   *  `[[scala.IllegalArgumentException IllegalArgumentException]]` being thrown.
   *
   *  @param arg Argument whose value is being validated.
   *
   *  @throws scala.IllegalArgumentException if `arg` does not have a finite value.
   *
   *  @since 0.0
   */
  def requireFinite(arg: Double): Unit = macro requireFiniteImpl

  /** Clean argument names.
   *
   *  Class argument names are prefixed by "{ClassName}.this." (where "{ClassName}" is the name of the class to which
   *  the argument belongs), which creates confusion when identifying failing arguments, and testing that failed
   *  argument messages match expected messages. This function removes class argument prefixes so that the raw argument
   *  name is returned.
   *
   *  @param arg A class or method argument to be cleaned.
   *
   *  @return Cleaned argument name, matching the value expected by the user.
   *
   *  @since 0.0
   */
  def cleanArgName(arg: String): String = arg match {

    // If this is a class argument, remove the prefix and return the actual name of the argument.
    case ClassArgRE(classArg) => classArg

    // Otherwise, just return the value supplied.
    case basicArg: String => basicArg
  }

  /** Convert an expression into a string expression.
   *
   *  @param c AST context for the conversion.
   *
   *  @param arg Expression to be converted.
   *
   *  @return String expression capturing contents of original expression.
   */
  private def exprAsString(c: Context)(arg: c.Expr[Any]): c.Expr[String] = {
    import c.universe._
    c.Expr[String](Literal(Constant(show(arg.tree))))
  }

  /** IndentationCheckerProvides implementation of the `[[org.facsim.util.assertNonNull(AnyRef)*
   *  assertNonNull(AnyRef)]]` macro.
   *
   *  @param c Abstract syntax tree (AST) context for this macro definition.
   *
   *  @param arg Argument whose value is to be tested. If this argument evaluates to `null`, then an
   *  `[[java.lang.AssertionError AssertionError]]` is thrown by the macro implementation, together with the name of the
   *  failed argument.
   *
   *  @return Implementation of this instance of the `assertNonNull` macro.
   *
   *  @since 0.0
   */
  def assertNonNullImpl(c: Context)(arg: c.Expr[AnyRef]): c.Expr[Unit] = {

    // Convert the argument into a string that represents the expression that was passed to the requireNonNull macro -
    // we'll output that as part of the exception.
    import c.universe._
    val argString = exprAsString(c)(arg)

    // Generate the AST to be substituted for the macro reference.
    //
    // If the argument evaluates to be null, throw an AssertionError with some useful information.
    reify {
      //scalastyle:off null
      if(arg.splice eq null) {
        throw new AssertionError(LibResource(AssertNonNullKey, cleanArgName(argString.splice)), null)
      }
      //scalastyle:on null
    }
  }

  /** Provides implementation of the `[[org.facsim.util.requireNonNull(AnyRef)* requireNonNull(AnyRef)]]` macro.
   *
   *  @param c Abstract syntax tree (AST) context for this macro definition.
   *
   *  @param arg Argument whose value is to be tested. If this argument evaluates to `null`, then a
   *  `[[scala.NullPointerException NullPointerException]]` is thrown by the macro implementation, together with the
   *  name of the failed argument.
   *
   *  @return Implementation of this instance of the `requireNonNull` macro.
   *
   *  @since 0.0
   */
  def requireNonNullImpl(c: Context)(arg: c.Expr[AnyRef]): c.Expr[Unit] = {

    // Convert the argument into a string that represents the expression that was passed to the requireNonNull macro -
    // we'll output that as part of the exception.
    import c.universe._
    val argString = exprAsString(c)(arg)

    // Generate the AST to be substituted for the macro reference.
    //
    // If the argument evaluates to be null, throw a NullPointerException with some useful information.
    reify {
      if(arg.splice eq null) { //scalastyle:ignore null
        throw new NullPointerException(LibResource(RequireNonNullKey, cleanArgName(argString.splice)))
      }
    }
  }

  /** Provides implementation of the `[[org.facsim.util.requireValid(Any,Boolean)* requireValid(Any,Boolean)]]` macro.
   *
   *  @param c Abstract syntax tree (AST) context for this macro definition.
   *
   *  @param arg Argument whose value is to be tested. If `isValid` is evaluated to `false`, then a
   *  `[[scala.IllegalArgumentException IllegalArgumentException]]` is thrown by the macro implementation, together with
   *  the name of the failed argument.
   *
   *  @param isValid Flag representing the result of a condition determining the validity of `arg`. If `true`, function
   *  merely returns; if `false` an `IllegalArgumentException` is raised.
   *
   *  @return Implementation of this instance of the `requireValid` macro.
   *
   *  @since 0.0
   */
  def requireValidImpl(c: Context)(arg: c.Expr[Any], isValid: c.Expr[Boolean]): c.Expr[Unit] = {

    // Convert the arguments to strings.
    import c.universe._
    val argString = exprAsString(c)(arg)

    // Generate the AST to be substituted for the macro reference.
    //
    // If the argument is deemed invalid, then throw an IllegalArgumentException with some useful information.
    reify {
      if(!isValid.splice) throw new IllegalArgumentException(LibResource(RequireValidKey,
      cleanArgName(argString.splice), arg.splice))
    }
  }

  /** Provides implementation of the `[[org.facsim.util.requireFinite(Double)* requireFinite(Double)]]` macro.
   *
   *  @param c Abstract syntax tree (AST) context for this macro definition.
   *
   *  @param arg Argument whose value is to be tested. If evaluated as `NaN`, `+∞` or `-∞`, then a
   *  `[[scala.IllegalArgumentException IllegalArgumentException]]` is thrown by the macro implementation, together with
   *  the name of the failed argument.
   *
   *  @return Implementation of this instance of the `requireFinite` macro.
   *
   *  @since 0.0
   */
  def requireFiniteImpl(c: Context)(arg: c.Expr[Double]): c.Expr[Unit] = {

    // Convert the argument to a string.
    import c.universe._
    val argString = exprAsString(c)(arg)

    // Generate the AST to be substituted for the macro reference.
    //
    // Determine whether the value is finite; if not, then throw an exception.
    reify {
      if(arg.splice.isNaN || arg.splice.isInfinite) {
        throw new IllegalArgumentException(LibResource(RequireFiniteKey, cleanArgName(argString.splice), arg.splice))
      }
    }
  }
}
