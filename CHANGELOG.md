# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

* Added ability to copy a field with builders.

* Add `id` to field and add fields to table results to identify what field each row value belongs to.

* Upgraded stroom expression to v1.5.5.

* Added new field types.

## [v4.0-beta.40] - 2019-11-04

* Updated stroom expression and added float and double field types.

## [v4.0-beta.39] - 2019-11-04

* Updated stroom expression and added float and double field types.

## [v4.0-beta.38] - 2019-09-18

* Reverted to **Java 8**, for greater interoperability (e.g. with Apache Spark)

* Issue **#stroom#1263** : Fixed issue where date expressions were being allowed without '+' or '-' signs to add or subtract durations.

## [v4.0-beta.37] - 2019-08-30

* Issue **#stroom#1244** : Updated dropwizard to version 1.3.14 to remove memory leak.

* Issue **#stroom#1215** : Fixed limiting max results.

## [v4.0-beta.36] - 2019-07-29

## [v4.0-beta.35] - 2019-07-29

* Remove unwanted hibernate stuff.

## [v4.0-beta.34] - 2019-07-29

* Issue **#stroom#1143** : Added mechanism to inject statically mapped values so that dashboard parameters can be echoed by expressions to create dashboard links.

* Issue **gchq/stroom#1200** : Fixed search hanging due to a blocking queue being used when it shouldn't be.

## [v4.0-beta.33] - 2019-07-08

* Issue **gchq/stroom#1154** : Fixed broken tests.

## [v4.0-beta.32] - 2019-07-08

* Added type info.

## [v4.0-beta.29] - 2019-07-08

* Experimental change to field type definitions.

## [v4.0-beta.27] - 2019-06-26

* Added boolean field type.

* Added `IS_NULL` and `IS_NOT_NULL` conditions.

## [v4.0-beta.26] - 2019-06-25

* Issue **gchq/stroom#1154** : Add HasTerminate where needed to allow termination of searches.

* Issue **gchq/stroom#1167** : Added IN_FOLDER as a new condition type and removed unnecessary dictionary doc ref.

* Issue **gchq/stroom#1015** : Fix problem of stroom-query ignoring visualisation sort settings.

* Issue **gchq/stroom#1007** : Max visualisation results are now limited by default to the maximum number of results defined for the first level of the parent table. This can be further limited by settings in the visualisation.

* Updated stroom expression to v2.0.4.

* Issue **gchq/stroom#1007** : Max visualisation results are now limited by default to the maximum number of results defined for the first level of the parent table. This can be further limited by settings in the visualisation.

* Updated stroom expression to v2.0.0.

* Issue **gchq/stroom#945** : More changes to fix some visualisations only showing 10 data points.

* Issue **gchq/stroom#945** : Fix for some visualisations only showing 10 data points.

* Changed the toString() method of ExpressionTerm to show names of DocRefs not UUIDs.

* Updated stroom expression to v1.4.12.

* Fix use of primative boolean for enabled setting.

* Issue **gchq/stroom#830** : Fix api queries that never return before the server times out.

* Issue **gchq/stroom#791** : Fix total results count not updating.

## [v4.0-beta.7] - 2018-09-05

* Added automatic module name for Java 9 builds.

* Issue **stroom-#808** : Fix to clear previous dashboard search results when new results are empty.

* Issue **stroom-#805** : Fix for dashboard date time formatting to use local time zone.

* Issue **stroom-#803** : Fix for dashboard group keys in visualisations.

## [v4.0-beta.3] - 2018-06-08

* Update dependencies.

## [v3.1-beta.10] - 2018-06-20

* Fixed the enabled flag to a default, removed the redundant accessor.

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

[Unreleased]: https://github.com/gchq/stroom-query/compare/v4.0-beta.42...HEAD
[v4.0-beta.42]: https://github.com/gchq/stroom-query/compare/v4.0-beta.41...v4.0-beta.42
[v4.0-beta.41]: https://github.com/gchq/stroom-query/compare/v4.0-beta.40...v4.0-beta.41
[v4.0-beta.40]: https://github.com/gchq/stroom-query/compare/v4.0-beta.39...v4.0-beta.40
[v4.0-beta.39]: https://github.com/gchq/stroom-query/compare/v4.0-beta.38...v4.0-beta.39
[v4.0-beta.38]: https://github.com/gchq/stroom-query/compare/v4.0-beta.37...v4.0-beta.38
[v4.0-beta.37]: https://github.com/gchq/stroom-query/compare/v4.0-beta.36...v4.0-beta.37
[v4.0-beta.36]: https://github.com/gchq/stroom-query/compare/v4.0-beta.35...v4.0-beta.36
[v4.0-beta.35]: https://github.com/gchq/stroom-query/compare/v4.0-beta.34...v4.0-beta.35
[v4.0-beta.34]: https://github.com/gchq/stroom-query/compare/v4.0-beta.33...v4.0-beta.34
[v4.0-beta.33]: https://github.com/gchq/stroom-query/compare/v4.0-beta.32...v4.0-beta.33
[v4.0-beta.32]: https://github.com/gchq/stroom-query/compare/v4.0-beta.31...v4.0-beta.32
[v4.0-beta.31]: https://github.com/gchq/stroom-query/compare/v4.0-beta.30...v4.0-beta.31
[v4.0-beta.30]: https://github.com/gchq/stroom-query/compare/v4.0-beta.29...v4.0-beta.30
[v4.0-beta.29]: https://github.com/gchq/stroom-query/compare/v4.0-beta.28...v4.0-beta.29
[v4.0-beta.28]: https://github.com/gchq/stroom-query/compare/v4.0-beta.27...v4.0-beta.28
[v4.0-beta.27]: https://github.com/gchq/stroom-query/compare/v4.0-beta.26...v4.0-beta.27
[v4.0-beta.26]: https://github.com/gchq/stroom-query/compare/v4.0-beta.25...v4.0-beta.26
