# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [v3.1-beta.8] - 2018-06-08

* Update dependencies. 

## [v3.1-beta.4] - 2018-05-10

* Made Wiremock based tests use dynamically allocated ports.

## [v3.1-beta.3] - 2018-05-10

* Fix dependencies.

## [v3.1-beta.2] - 2018-05-10

* Updated to stroom-expression v1.3.2.

* Renamed SecurityContext to CurrentServiceUser.

* Updated libs to keep in line with dropwizard 1.2.5.

## [v3.1-beta.1] - 2018-05-02

* Fix DocRef equality.

* Migrated to stroom-expression v1.3.1

## [v3.0-beta.8] - 2018-04-30

* Fixed equals and hash codes for query api classes.

## [v3.0-beta.6] - 2018-04-25

* Pulled out stroom-query-authorisation
* Fixed unused/used dependencies using the plugin

* Removed ConditionalWait.

* Set response completion state prior to assembling results.

## [v3.0-beta.5] - 2018-04-12

* Issue **#16** : Fix for Hessian serialisation of table coprocessor settings.

## [v3.0-beta.4] - 2018-04-12

* Reinstates Guice to jOOQ and Hibernate bundles

* Provides service implementations which remote via HTTP

* Added custom service exceptions for query/docref services

## [v3.0-beta.3] - 2018-04-11

* Add clear() to SearchResponseCreatorCache

## [v3.0-beta.2] - 2018-04-10

* Issue **#12** : Add server side caching of search results to query-common.

* Fix handling of InterruptedException in TablePayloadHandler

## [v3.0-beta.1] - 2018-04-04

* Removed parent folder UUID from the explorer action handler API.

* Fix handling of InterruptedException in TablePayloadHandler

* jOOQ injecting DSL Context rather than configuration

* gh-13 - Using Guice for common auditable query resource implementations

* Simplification of search completion state.

* Simplification of search completion state.

* Fix to ensure completion listeners are always notified on completion

* Fluent API for building authentication rules in the test Auth Wiremock service

* Issue **#8** : Fix handling of non-incremental queries on the query API. Adds timeout option in request and blocking code to wait for the query to complete.

* jOOQ based Doc Ref service that still permits any Query Service implementation

* Tidied up the inheritable bundles for JOOQ and JPA

* Issue **#10**: Added a jOOQ implementation of the audit bundle

* Tune the hibernate data source pool

* Make fifo log appender thread safe

* Making the audit fifo log rule more sophisticated to allow more detailed assertions about audit logs

* Common integration tests now force sub classes to create their app and wiremock rules.

* Using TestRules instead of inherited behaviour as much as possible

* Removed custom test implementations of auth service and http client, use Wiremock and javax.ws.rs.Client

* Setup the doc ref info fields (created/updated user/time)

* Providing standard implementations of integration tests, plus demonstration apps for hibernate and generic query apps

* Added Authentication to DocRef and Query resources.

* Implementations now need to create Service implementations, REST layers are now entirely common.

* Added misc info field for Doc Refs

* Added resource definitions for external doc ref management

* Added missing fields to result builder

* Added Import and Export endpoints

* Fixed minor bug in QueryableEntityBuilder that returned the specific builder class instead of the CHILD_CLASS

* Added misc info field for Doc Refs

* Added resource definitions for external doc ref management

* Added missing fields to result builder

* Added Import and Export endpoints

* Fixed minor bug in QueryableEntityBuilder that returned the specific builder class instead of the CHILD_CLASS

* Now using openjdk instead of oraclejdk

* Changed the builders to remove the parenting/templating. Now using a simpler pattern where developers will create the child builders themselves.

* Added DocRef Resource, added client implementations for DocRef and Query Resource

* Change build to use Bintray dependencies and push to Bintray

* Added DocRef Resource, added client implementations for DocRef and Query Resource

* Change build to use Bintray dependencies and push to Bintray

* Merge changes up from stroom v5

* Add DropWizard resource bundles

* Uplift to latest event-logging release

* Actually releasing audit and hibernate

* Uplift to latest event-logging release

* Actually releasing audit and hibernate

* Added standard implementations of Query Resource, with auditing

* Build a hibernate implementation of Query Resource

* Pojo Builders for all classes in the API project

* Added null check to cope with empty field list when running queries

* Remove redundant arg from SearchResponseCreator constructor

* Pojo Builders for all classes in the API project

* Added null check to cope with empty field list when running queries

* Remove redundant arg from SearchResponseCreator constructor

* Add ExpressionBuilder.end() method for better chaining of methods.

* Add Swagger annotations to API model classes

* Add stroom-query-sample module for testing swagger generation

* Enhanced component result fetch options to allow for a full fetch on request, no fetch or just data changes.

* Moved ResultStoreCreator out into its own public class for use in client code

* Changed ModelChangeDetector to output new model portrait

* Change to use v1.0.0 of stroom-expression

* Move ParamUtil to stroom.query.shared.v2

* https://github.com/gchq/stroom/issues/98 Replace TrimSettings with MaxResults and StoreSize to separate limiting the data held in memory in the store from limiting the sorted/grouped results returned to the user/client.

* Move common code into stroom.query.common.v2 so it is versioned

* Improved toString() and toMultiLineString() methods.

* Removed null from fields and values in toString()

[Unreleased]: https://github.com/gchq/stroom-query/compare/v3.1-beta.8...HEAD
[v3.1-beta.4]: https://github.com/gchq/stroom-query/compare/v3.1-beta.7...v3.1-beta.8
[v3.1-beta.4]: https://github.com/gchq/stroom-query/compare/v3.1-beta.6...v3.1-beta.7
[v3.1-beta.4]: https://github.com/gchq/stroom-query/compare/v3.1-beta.4...v3.1-beta.5
[v3.1-beta.4]: https://github.com/gchq/stroom-query/compare/v3.1-beta.3...v3.1-beta.4
[v3.1-beta.3]: https://github.com/gchq/stroom-query/compare/v3.1-beta.2...v3.1-beta.3
[v3.1-beta.2]: https://github.com/gchq/stroom-query/compare/v3.1-beta.1...v3.1-beta.2
[v3.1-beta.1]: https://github.com/gchq/stroom-query/compare/v3.0-beta.8...v3.1-beta.1
[v3.0-beta.8]: https://github.com/gchq/stroom-query/compare/v3.0-beta.6...v3.0-beta.8
[v3.0-beta.6]: https://github.com/gchq/stroom-query/compare/v3.0-beta.5...v3.0-beta.6
[v3.0-beta.5]: https://github.com/gchq/stroom-query/compare/v3.0-beta.4...v3.0-beta.5
[v3.0-beta.4]: https://github.com/gchq/stroom-query/compare/v3.0-beta.3...v3.0-beta.4
[v3.0-beta.3]: https://github.com/gchq/stroom-query/compare/v3.0-beta.2...v3.0-beta.3
[v3.0-beta.2]: https://github.com/gchq/stroom-query/compare/v3.0-beta.1...v3.0-beta.2
[v3.0-beta.1]: https://github.com/gchq/stroom-query/compare/v2.0-beta.1...v3.0-beta.1
[v2.0-beta.1]: https://github.com/gchq/stroom-query/compare/v1.0.1...v2.0-beta.1
[v1.0.1]: https://github.com/gchq/stroom-query/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/gchq/stroom-query/releases/tag/v1.0.0
