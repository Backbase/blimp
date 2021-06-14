
# release-0.15.0

* Added [stripComments](README.md#stripComments) mojo parameter
** defaults to false to avoid conflicts with other ways of stripping comments (like antrun)
** set to `true` and remove `antrun`

# release-0.15.1

* Added [withInitialVersion](README.md#withInitialVersion) mojo parameter
** defaults to `false`, so the script of the initial version is not generated
