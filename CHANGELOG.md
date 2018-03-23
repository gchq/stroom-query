# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added

### Changed

## [v2.0.0-alpha.34] - 2018-03-23

* Simplification of search completion state.

## [v2.0.0-alpha.33] - 2018-03-21

* Simplification of search completion state.

## [v2.0.0-alpha.32] - 2018-03-21

* Fix to ensure completion listeners are always notified on completion

## [v2.0.0-alpha.31] - 2018-03-20

### Changed
* Fluent API for building authentication rules in the test Auth Wiremock service
* jOOQ based Doc Ref service that still permits any Query Service implementation

## [v2.0.0-alpha.30] - 2018-02-20

### Changed
* Tidied up the inheritable bundles for JOOQ and JPA

## [v2.0.0-alpha.29] - 2018-02-20

### Added
* Issue **#10**: Added a jOOQ implementation of the audit bundle

## [v2.0.0-alpha.28] - 2018-02-16

### Changed
* Issue **#8** : Fix handling of non-incremental queries on the query API. Adds timeout option in request and blocking code to wait for the query to complete.

## [v2.0.0-alpha.27] - 2018-01-31

### Changed

* Tune the hibernate data source pool

* Make fifo log appender thread safe

* Making the audit fifo log rule more sophisticated to allow more detailed assertions about audit logs

## [v2.0.0-alpha.26] - 2018-01-26

### Changed

* Common integration tests now force sub classes to create their app and wiremock rules.

* Using TestRules instead of inherited behaviour as much as possible

## [v2.0.0-alpha.24] - 2018-01-25

### Changed

* Removed custom test implementations of auth service and http client, use Wiremock and javax.ws.rs.Client

* Setup the doc ref info fields (created/updated user/time)

* Providing standard implementations of integration tests, plus demonstration apps for hibernate and generic query apps

## [v2.0.0-alpha.22] - 2018-01-15

### Changed

* Added Authentication to DocRef and Query resources.

* Implementations now need to create Service implementations, REST layers are now entirely common.

## [v2.0.0-alpha.21] - 2018-01-09

### Changed

* Added misc info field for Doc Refs

## [v2.0.0-alpha.20] - 2018-01-08

### Changed

* Added resource definitions for external doc ref management

## [v2.0.0-alpha.19] - 2017-12-12

### Changed

* Added missing fields to result builder

## [v2.0.0-alpha.14] - 2017-12-11

### Changed

* Added Import and Export endpoints

## [v2.0.0-alpha.13] - 2017-12-06

### Changed

* Fixed minor bug in QueryableEntityBuilder that returned the specific builder class instead of the CHILD_CLASS

## [v2.0.0-alpha.12] - 2017-12-06

### Changed

* Now using openjdk instead of oraclejdk

* Changed the builders to remove the parenting/templating. Now using a simpler pattern where developers will create the child builders themselves.

## [v2.0.0-alpha.11] - 2017-12-01

### Changed

* Added DocRef Resource, added client implementations for DocRef and Query Resource

## [v2.0.0-alpha.10] - 2017-11-29

### Changed

* Change build to use Bintray dependencies and push to Bintray

## [v2.0.0-alpha.9] - 2017-11-27

### Changed

* Merge changes up from stroom v5

* Add DropWizard resource bundles

## [v2.0.0-alpha.8] - 2017-11-14

### Changed

* Uplift to latest event-logging release

## [v2.0.0-alpha.7] - 2017-11-03

### Added

* Actually releasing audit and hibernate

## [v2.0.0-alpha.6] - 2017-11-03

### Changed

* Added standard implementations of Query Resource, with auditing

* Build a hibernate implementation of Query Resource

## [v2.0.0-alpha.5] - 2017-24-16

### Changed

* Pojo Builders for all classes in the API project

## [v2.0.0-alpha.4] - 2017-10-16

### Changed

* Added null check to cope with empty field list when running queries

* gh-13 - Using Guice for common auditable query resource implementations

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

[Unreleased]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.34...HEAD
[v2.0.0-alpha.34]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.33...v2.0.0-alpha.34
[v2.0.0-alpha.33]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.32...v2.0.0-alpha.33
[v2.0.0-alpha.32]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.31...v2.0.0-alpha.32
[v2.0.0-alpha.31]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.30...v2.0.0-alpha.31
[v2.0.0-alpha.30]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.29...v2.0.0-alpha.30
[v2.0.0-alpha.29]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.28...v2.0.0-alpha.29
[v2.0.0-alpha.28]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.27...v2.0.0-alpha.28
[v2.0.0-alpha.27]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.26...v2.0.0-alpha.27
[v2.0.0-alpha.26]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.24...v2.0.0-alpha.26
[v2.0.0-alpha.24]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.23...v2.0.0-alpha.24
[v2.0.0-alpha.23]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.22...v2.0.0-alpha.23
[v2.0.0-alpha.22]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.21...v2.0.0-alpha.22
[v2.0.0-alpha.21]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.20...v2.0.0-alpha.21
[v2.0.0-alpha.20]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.19...v2.0.0-alpha.20
[v2.0.0-alpha.19]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.14...v2.0.0-alpha.19
[v2.0.0-alpha.14]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.13...v2.0.0-alpha.14
[v2.0.0-alpha.13]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.12...v2.0.0-alpha.13
[v2.0.0-alpha.12]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.11...v2.0.0-alpha.12
[v2.0.0-alpha.11]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.10...v2.0.0-alpha.11
[v2.0.0-alpha.10]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.9...v2.0.0-alpha.10
[v2.0.0-alpha.9]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.8...v2.0.0-alpha.9
[v2.0.0-alpha.8]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.7...v2.0.0-alpha.8
[v2.0.0-alpha.7]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.6...v2.0.0-alpha.7
[v2.0.0-alpha.6]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.5...v2.0.0-alpha.6
[v2.0.0-alpha.5]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.4...v2.0.0-alpha.5
[v2.0.0-alpha.4]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.3...v2.0.0-alpha.4
[v2.0.0-alpha.3]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.2...v2.0.0-alpha.3
[v2.0.0-alpha.2]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.1...v2.0.0-alpha.2
[v2.0.0-alpha.1]: https://github.com/gchq/stroom-query/compare/v1.0.1...v2.0.0-alpha.1
[v1.0.1]: https://github.com/gchq/stroom-query/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/gchq/stroom-query/releases/tag/v1.0.0
