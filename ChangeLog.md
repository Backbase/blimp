
# release-0.16.5

* Don't insert empty lines in formatted SQL statements
* Format INSERT INTO statements

# release-0.16.4

* Don't add empty files to generated assembly

# release-0.16.3

* Clarified the meaning of the [changeLogFile](README.md#changelogfile-1) parameter of `blimp:lint`

# release-0.16.2

* Extended Blimp linter to support `dbms` selectors

# release-0.16.0

* Added changelog linter
* Added SQL formatter

# release-0.15.3

* Workaround to ClassCastException thrown by Liquibase

# release-0.15.1

* Added [withInitialVersion](README.md#withinitialversion) mojo parameter
  * defaults to `false`, so the script of the initial version is not generated

# release-0.15.0

* Added [stripComments](README.md#stripcomments) mojo parameter
  * defaults to `false` to avoid conflicts with other ways of stripping comments (like `antrun`)
  * set to `true` and remove `antrun`
