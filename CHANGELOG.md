# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added

### Changed

## [v2.0.0-alpha.4] - 2017-10-16

### Changed

* Added null check to cope with empty field list when running queries

## [v2.0.0-alpha.3] - 2017-09-04

### Changed

* Remove redundant arg from SearchResponseCreator constructor

## [v2.0.0-alpha.2] - 2017-09-01

### Added

* Add ExpressionBuilder.end() method for better chaining of methods.

* Add Swagger annotations to API model classes

* Add stroom-query-sample module for testing swagger generation


## [v2.0.0-alpha.1] - 2017-08-22

### Changed

* Enhanced component result fetch options to allow for a full fetch on request, no fetch or just data changes.

* Moved ResultStoreCreator out into its own public class for use in client code

* Changed ModelChangeDetector to output new model portrait

* Change to use v1.0.0 of stroom-expression

* Move ParamUtil to stroom.query.shared.v2

* https://github.com/gchq/stroom/issues/98 Replace TrimSettings with MaxResults and StoreSize to separate limiting the data held in memory in the store from limiting the sorted/grouped results returned to the user/client.

* Move common code into stroom.query.common.v2 so it is versioned

## [v1.0.1] - 2017-07-11

### Changed

* Improved toString() and toMultiLineString() methods.

* Removed null from fields and values in toString()

## [v1.0.0] - 2017-05-19

* Initial release

[Unreleased]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.4...HEAD
[v2.0.0-alpha.4]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.3...v2.0.0-alpha.4
[v2.0.0-alpha.3]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.2...v2.0.0-alpha.3
[v2.0.0-alpha.2]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.1...v2.0.0-alpha.2
[v2.0.0-alpha.1]: https://github.com/gchq/stroom-query/compare/v1.0.1...v2.0.0-alpha.1
[v1.0.1]: https://github.com/gchq/stroom-query/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/gchq/stroom-query/releases/tag/v1.0.0
