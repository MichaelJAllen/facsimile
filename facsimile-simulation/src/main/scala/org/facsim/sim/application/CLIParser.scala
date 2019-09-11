//======================================================================================================================
// Facsimile: A Discrete-Event Simulation Library
// Copyright © 2004-2019, Michael J Allen.
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
// Scala source file belonging to the org.facsim.sim.application package.
//======================================================================================================================
package org.facsim.sim.application

import java.io.File
import org.facsim.sim.LibResource
import org.facsim.util.log.Severity
import org.facsim.util.{NL, SQ}
import scopt.OptionParser

/** Define the supported command line syntax and parse command line arguments.
 *
 *  Command line arguments are parsed even if the application is being run with an animation. Indeed, the command line
 *  must be parsed to determine whether an animation is to be utilized.
 *
 *  @constructor Create a new ''command line interface'' (''CLI'') parser.
 *
 *  @param appName Name of the application.
 *
 *  @param appCopyright Application's copyright message, as a list of strings.
 *
 *  @param appVersionString Application's version string.
 */
private[application] final class CLIParser(appName: String, appCopyright: List[String], appVersionString: String) {

  /** Command line parser for this application. */
  private final val parser = new OptionParser[FacsimileConfig](appName) {

    // Output header lines.
    head(s"$appName$NL${appCopyright.mkString(NL)}${NL}Version: ${appVersionString}")

    // Add a note explaining what this command does.
    note(LibResource("application.CLIParser.Note"))

    // Option defining the HOCON configuration file for the simulation.
    //
    // This option is not necessary to run the simulation. Only a single configuration file may be specified.
    opt[File]('c', "config-file")
    .valueName("<file>")
    .text(LibResource("application.CLIParser.ConfigFileText"))
    .optional
    .maxOccurs(1)
    .validate {f =>

      // Check that the configuration file exists and that it can be read from.
      //
      // It may well be that when we attempt to open this file later on, it will fail (if the file is changed in any
      // way between the moment this code executes and the time it is opened). However, we should do as much as we can
      // up front in order to assist the user as much as possible.
      if(f.exists && f.canRead) success
      else failure(LibResource("application.CLIParser.ConfigFileFailure"))
    }
    .action {(f, c) =>

      // Update the configuration.
      c.copy(configFile = Some(f))
    }

    // Option to run the simulation in "headless" mode, without a GUI interface.
    //
    // If this option is provided, then the simulation will not produce a graphical user interface (GUI) for the
    // simulation run, and will not illustrate what is happening inside the simulation using a 3D animation. However,
    // this will speed-up the simulation run significantly, permitting it to run to completion far faster. As a
    // consequence, this option is highly recommended when performing experiments. For similar reasons, it is highly
    // undesirable to use this option when debugging a model.
    opt[Unit]('h', "headless")
    .text(LibResource("application.CLIParser.HeadlessText"))
    .optional
    .maxOccurs(1)
    .action {(_, c) =>
      c.copy(useGUI = false)
    }

    // Option defining the output file for writing simulation log messages.
    //
    // This option is not necessary to run the simulation. Only a single log file may be specified.
    //
    // Note: A warning will be issued if the simulation produces no output of any kind. Unless the simulation is doing
    // something unusual, there's a danger that the simulation will just consume CPU for no purpose.
    opt[File]('l', "log-file")
    .valueName(CLIParser.FileValueName)
    .text(LibResource("application.CLIParser.LogFileText"))
    .optional
    .maxOccurs(1)
    .validate {f =>

      // Check that the report file exists and that it can be written to.
      //
      // It may well be that when we attempt to open this file later on, it will fail (if the file is changed in any
      // way between the moment this code executes and the time it is opened). However, we should do as much as we can
      // up front in order to assist the user as much as possible.
      //
      // TODO: Check if we have permission to create the file if it doesn't exist.
      if(!f.exists || f.canWrite) success
      else failure(LibResource("application.CLIParser.LogFileFailure"))
    }
    .action {(f, c) =>

      // Update the configuration.
      c.copy(logFile = Some(f))
    }

    // Option defining the report output file for this simulation run.
    //
    // This option is not necessary to run the simulation. Only a single report file may be specified.
    //
    // Note: A warning will be issued if the simulation produces no output of any kind. Unless the simulation is doing
    // something unusual, there's a danger that the simulation will just consume CPU for no purpose.
    opt[File]('r', "report-file")
    .valueName(CLIParser.FileValueName)
    .text(LibResource("application.CLIParser.ReportFileText"))
    .optional
    .maxOccurs(1)
    .validate {f =>

      // Check that the report file exists and that it can be written to.
      //
      // It may well be that when we attempt to open this file later on, it will fail (if the file is changed in any
      // way between the moment this code executes and the time it is opened). However, we should do as much as we can
      // up front in order to assist the user as much as possible.
      //
      // TODO: Check if we have permission to create the file if it doesn't exist.
      if(!f.exists || f.canWrite) success
      else failure(LibResource("application.CLIParser.ReportFileFailure"))
    }
    .action {(f, c) =>

      // Update the configuration.
      c.copy(reportFile = Some(f))
    }

    // Option to specify the log level, or verbosity, for use when writing log messages to the log-file, and to the
    // standard output, if no animation is present.
    opt[String]('v', "log-level")
    .valueName(LibResource("application.CLIParser.LogLevelValueName"))
    .text(LibResource("application.CLIParser.LogLevelText", Severity.severityList.mkString(SQ, ", ", SQ),
      FacsimileConfig().logLevel.name))
    .optional
    .maxOccurs(1)
    .validate {sl =>

      // Validate that the log level is valid. This is case sensitive.
      Severity.withName(sl) match {
        case Some(_) => success
        case None => failure(LibResource("application.CLIParser.LogLevelFailure", sl))
      }
    }
    .action {(sl, c) =>
      c.copy(logLevel = Severity.withName(sl).get)
    }
  }

  /** Parse the command line arguments provided.
   *
   *  @param args Command line arguments provided.
   *
   *  @return Resulting application configuration wrapped in `[[scala.Some Some]]` if successfully parsed, or
   *  `[[scala.None None]]` if the command line could not be parsed.
   */
  private[application] def parse(args: Array[String]): Option[FacsimileConfig] = parser.parse(args, FacsimileConfig())
}

/** Command line interpreter parser companion object. */
private object CLIParser {

  /** Value description to output for a command line file parameter or option. */
  private val FileValueName = LibResource("application.CLIParser.FileValueName")

}