/*
Facsimile -- A Discrete-Event Simulation Library
Copyright © 2004-2008, Michael J Allen.

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program.  If not, see <http://www.gnu.org/licenses/>.

The developers welcome all comments, suggestions and offers of assistance.
For further information, please visit the project home page at:

    http://www.facsim.org/

Thank you for your interest in the Facsimile project!

IMPORTANT NOTE: All patches (modifications to existing files and/or the
addition of new files) submitted for inclusion as part of the official
Facsimile code base, must comply with the published Facsimile Coding Standards.
If your code fails to comply with the standard, then your patches will be
rejected.  For further information, please visit the coding standards at:

    http://www.facsim.org/Documentation/CodingStandards/

$Id$
*/
//=============================================================================
/**
\file
Test suite LogFile class C++ source file.

C++ source file for the LogFile class that assists with log files generated by
the test suite.
*/
//=============================================================================

/*
Relevant header files.
*/

#include <locale>
#include <unicode/unistr.h>
#include <unicode/locid.h>
#include "LogFile.hpp"

/*
Initialise the suffix to be an empty string.
*/

std::string LogFile::suffix = "";

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
LogFile::setSuffix (const char*) implementation.
*/
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

void LogFile::setSuffix (const char* newSuffix) throw ()
{

/*
If the new suffix is null, then store an empty string.
*/

    if (newSuffix == 0)
    {
        suffix = "";
    }

/*
Otherwise, store the new suffix prefixed with a period (for later simplicity).
*/

    else
    {
        suffix = std::string (".") + newSuffix;
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
LogFile::LogFile (const char*, bool, bool) implementation.
*/
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

LogFile::LogFile (const char* fileName, bool hasLocaleSpecificData, bool
isNewFile) throw ():
    std::ofstream ()
{

/*
Sanity checks.
*/

    assert (fileName != 0);

/*
Create the file name.
*/

    std::string logFileName (fileName);

/*
If we have any locale-specific data, then append the current default locale.
*/

    if (hasLocaleSpecificData)
    {
        logFileName += std::string (".") + icu::Locale::getDefault ().getName
        ();
    }

/*
Now append the suffix and the file extension.

Note: Do not change this file extension!  Also, we use ".testlog" instead of
".log" because version control systems typically ignore any file ending in .log
- and these files need to be added to version control so that we can compare
new output to existing, valid output.
*/

    logFileName += suffix + ".testlog";

/*
If this is a new file, then we're going to erase any existing data in the file.
Otherwise, we're going to open the file and append any new data to it.
*/

    std::ios_base::openmode mode = std::ios_base::out;
    if (isNewFile)
    {
        mode |= std::ios_base::trunc;
    }
    else
    {
        mode |= std::ios_base::app;
    }

/*
OK.  Finally!  We get to open the file.  We do this in overwrite mode so that
any existing data is overwritten.  We may get exceptions doing this, which will
cause the test suite to exit (and fail).
*/

    std::ofstream::open (logFileName.c_str (), mode);

/*
If this is a new file, write the UTF-8 byte-order-mark (BOM) into the file to
make it more obviously a UTF-8 formatted file.
*/

    if (isNewFile)
    {
        icu::UnicodeString bom (static_cast <UChar32> (0xFEFF));
        *this << bom;
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
LogFile::~LogFile () implementation.
*/
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

LogFile::~LogFile () throw ()
{

/*
If this stream is currently open, then close the file.
*/

    if (is_open ())
    {
        close ();
    }
}