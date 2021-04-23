# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [v2.2.23] - 2021-04-23

* Update github token in travis build.

## [v2.2.23] - 2021-04-23

* Add description to the maven pom confirguration.

## [v2.2.22] - 2021-04-23

* Change to use event-logging v3.3.0_schema-v3.3.1 from maven central.

* Change to use stroom-expression from maven central.

## [v2.2.21] - 2020-10-19

* Issue **#stroom#1906** : Change to allow search to terminate when enough results have been found.

## [v2.2.20] - 2020-10-16

* Issue **#stroom#1907** : Fixed NPE.

## [v2.2.19] - 2020-09-08

* Issue **#stroom#1083** : Added `any()`, `first()`, `last()`, `nth()`, `top()` and `bottom()` selection functions to select child values of grouped items.

## [v2.2.18] - 2020-09-08

* Issue **#stroom#1083** : Added `any()`, `first()`, `last()`, `nth()`, `top()` and `bottom()` selection functions to select child values of grouped items.

* Upgraded stroom expression to v1.5.10.

## [v2.2.17] - 2020-09-04

* Upgraded stroom expression to v1.5.8.

## [v2.2.16] - 2020-09-04

* Upgraded stroom expression to v1.5.7.

## [v2.2.15] - 2020-09-04

* Upgraded stroom expression to v1.5.6.

## [v2.2.14] - 2020-08-25

* Improved completion state code.

## [v2.2.13] - 2020-08-25

* Improved completion state code.

## [v2.2.12] - 2020-08-25

* Improved completion state code.

## [v2.2.11] - 2020-08-24

* Improved completion state code.

## [v2.2.10] - 2020-08-24

* Improved completion state code.

## [v2.2.9] - 2019-11-26

* Added ability to copy a field with builders.

## [v2.2.8] - 2019-11-20

* Add `id` to field and add fields to table results to identify what field each row value belongs to.

## [v2.2.7] - 2019-11-13

* Upgraded stroom expression to v1.5.5.

## [v2.2.6] - 2019-11-04

* Added new field types.

## [v2.2.5] - 2019-10-30

* Upgraded stroom expression to v1.5.4.

## [v2.2.4] - 2019-10-24

* Added logging to diagnose NPE.

## [v2.2.2] - 2019-09-13

* Issue **#stroom#1263** : Fixed issue where date expressions were being allowed without '+' or '-' signs to add or subtract durations.

## [v2.2.1] - 2019-09-12

* Upgraded stroom expression to v1.5.2.

## [v2.2.0] - 2019-08-28

* Issue **#stroom#1244** : Updated dropwizard to version 1.3.14 to remove memory leak.

## [v2.2-beta.7] - 2019-08-08

* Issue **#stroom#1215** : Fixed limiting max results.

## [v2.2-beta.6] - 2019-07-19

* Issue **#stroom#1143** : Added mechanism to inject statically mapped values so that dashboard parameters can be echoed by expressions to create dashboard links.

## [v2.2-beta.5] - 2019-07-16

* Issue **gchq/stroom#1200** : Fixed search hanging due to a blocking queue being used when it shouldn't be.

## [v2.2-beta.4] - 2019-06-24

* Issue **gchq/stroom#1154** : Fixed broken tests.

## [v2.2-beta.3] - 2019-06-24

* Issue **gchq/stroom#1154** : Fixed broken tests.

## [v2.2-beta.2] - 2019-06-21

* Issue **gchq/stroom#1154** : Add HasTerminate where needed to allow termination of searches.

## [v2.2-beta.1] - 2019-06-11

* Issue **gchq/stroom#1167** : Added IN_FOLDER as a new condition type and removed unnecessary dictionary doc ref.

## [v2.1-beta.23] - 2019-05-28

* Upgraded stroom expression to v1.4.16.

## [v2.1-beta.22] - 2019-05-24

* Upgraded stroom expression to v1.4.15.

## [v2.1-beta.21] - 2018-12-13

* Upgraded stroom expression to v1.4.14.

## [v2.1-beta.20] - 2018-12-13

* Issue **gchq/stroom#1015** : Fix problem of stroom-query ignoring visualisation sort settings.

## [v2.1-beta.19] - 2018-12-11

* Issue **gchq/stroom#1007** : Max visualisation results are now limited by default to the maximum number of results defined for the first level of the parent table. This can be further limited by settings in the visualisation.

## [v2.1-beta.18] - 2018-12-11

* Issue **gchq/stroom#1007** : Fix build.

## [v2.1-beta.17] - 2018-12-11

* Issue **gchq/stroom#1007** : Max visualisation results are now limited by default to the maximum number of results defined for the first level of the parent table. This can be further limited by settings in the visualisation.

## [v2.1-beta.16] - 2018-12-06

* Upgraded stroom expression to v1.4.13.

## [v2.1-beta.15] - 2018-11-29

* Issue **gchq/stroom#945** : More changes to fix some visualisations only showing 10 data points.

## [v2.1-beta.14] - 2018-11-28

* Issue **gchq/stroom#945** : Fix for some visualisations only showing 10 data points.

## [v2.1-beta.13] - 2018-11-28

* Failed build.

## [v2.1-beta.12] - 2018-11-24

* Changed the toString() method of ExpressionTerm to show names of DocRefs not UUIDs.

## [v2.1-beta.11] - 2018-11-20

## [v2.1-beta.10] - 2018-11-20

* Updated stroom expression to v1.4.12.

## [v2.1-beta.8] - 2018-11-13

* Fix use of primative boolean for enabled setting.

## [v2.1-beta.7] - 2018-10-11

* Issue **gchq/stroom#830** : Fix api queries that never return before the server times out.

* Issue **gchq/stroom#791** : Fix total results count not updating.

## [v2.1-beta.6] - 2018-07-10

* Issue **stroom-#808** : Fix to clear previous dashboard search results when new results are empty.

## [v2.1-beta.5] - 2018-07-10

* Issue **stroom-#805** : Fix for dashboard date time formatting to use local time zone.

## [v2.1-beta.4] - 2018-07-09

* Issue **stroom-#803** : Fix for dashboard group keys in visualisations.

## [v2.1-beta.2] - 2018-05-02

* Fix DocRef equality.

## [v2.1-beta.1] - 2018-05-02

* Migrated to stroom-expression v1.3.1

* Removed ConditionalWait.

## [v2.0-beta.3] - 2018-04-13

* Set response completion state prior to assembling results.

## [v2.0-beta.2] - 2018-04-12

* Issue **#16** : Fix for Hessian serialisation of table coprocessor settings.

## [v2.0-beta.1] - 2018-04-09

* Issue **#12** : Add server side caching of search results to query-common.

* Fix handling of InterruptedException in TablePayloadHandler

## [v2.0.0-alpha.28] - 2018-02-16

* Issue **#8** : Fix handling of non-incremental queries on the query API. Adds timeout option in request and blocking code to wait for the query to complete.

## [v2.0.0-alpha.27] - 2018-01-31

* Tune the hibernate data source pool

* Make fifo log appender thread safe

* Making the audit fifo log rule more sophisticated to allow more detailed assertions about audit logs

## [v2.0.0-alpha.26] - 2018-01-26

* Common integration tests now force sub classes to create their app and wiremock rules.

* Using TestRules instead of inherited behaviour as much as possible

## [v2.0.0-alpha.24] - 2018-01-25

* Removed custom test implementations of auth service and http client, use Wiremock and javax.ws.rs.Client

* Setup the doc ref info fields (created/updated user/time)

* Providing standard implementations of integration tests, plus demonstration apps for hibernate and generic query apps

## [v2.0.0-alpha.22] - 2018-01-15

* Added Authentication to DocRef and Query resources.

* Implementations now need to create Service implementations, REST layers are now entirely common.

## [v2.0.0-alpha.21] - 2018-01-09

* Added misc info field for Doc Refs

## [v2.0.0-alpha.20] - 2018-01-08

* Added resource definitions for external doc ref management

## [v2.0.0-alpha.19] - 2017-12-12

* Added missing fields to result builder

## [v2.0.0-alpha.14] - 2017-12-11

* Added Import and Export endpoints

## [v2.0.0-alpha.13] - 2017-12-06

* Fixed minor bug in QueryableEntityBuilder that returned the specific builder class instead of the CHILD_CLASS

## [v2.0.0-alpha.12] - 2017-12-06

* Now using openjdk instead of oraclejdk

* Changed the builders to remove the parenting/templating. Now using a simpler pattern where developers will create the child builders themselves.

## [v2.0.0-alpha.11] - 2017-12-01

* Added DocRef Resource, added client implementations for DocRef and Query Resource

## [v2.0.0-alpha.10] - 2017-11-29

* Change build to use Bintray dependencies and push to Bintray

## [v2.0.0-alpha.9] - 2017-11-27

* Merge changes up from stroom v5

* Add DropWizard resource bundles

## [v2.0.0-alpha.8] - 2017-11-14

* Uplift to latest event-logging release

## [v2.0.0-alpha.7] - 2017-11-03

* Actually releasing audit and hibernate

## [v2.0.0-alpha.6] - 2017-11-03

* Added standard implementations of Query Resource, with auditing

* Build a hibernate implementation of Query Resource

## [v2.0.0-alpha.5] - 2017-24-16

* Pojo Builders for all classes in the API project

## [v2.0.0-alpha.4] - 2017-10-16

* Added null check to cope with empty field list when running queries

## [v2.0.0-alpha.3] - 2017-09-04

* Remove redundant arg from SearchResponseCreator constructor

## [v2.0.0-alpha.2] - 2017-09-01

* Add ExpressionBuilder.end() method for better chaining of methods.

* Add Swagger annotations to API model classes

* Add stroom-query-sample module for testing swagger generation

## [v2.0.0-alpha.1] - 2017-08-22

* Enhanced component result fetch options to allow for a full fetch on request, no fetch or just data changes.

* Moved ResultStoreCreator out into its own public class for use in client code

* Changed ModelChangeDetector to output new model portrait

* Change to use v1.0.0 of stroom-expression

* Move ParamUtil to stroom.query.shared.v2

* https://github.com/gchq/stroom/issues/98 Replace TrimSettings with MaxResults and StoreSize to separate limiting the data held in memory in the store from limiting the sorted/grouped results returned to the user/client.

* Move common code into stroom.query.common.v2 so it is versioned

## [v1.0.1] - 2017-07-11

* Improved toString() and toMultiLineString() methods.

* Removed null from fields and values in toString()

## [v1.0.0] - 2017-05-19

* Initial release

[Unreleased]: https://github.com/gchq/stroom-query/compare/v2.2.24...HEAD
[v2.2.24]: https://github.com/gchq/stroom-query/compare/v2.2.23...v2.2.24
[v2.2.23]: https://github.com/gchq/stroom-query/compare/v2.2.22...v2.2.23
[v2.2.22]: https://github.com/gchq/stroom-query/compare/v2.2.21...v2.2.22
[v2.2.21]: https://github.com/gchq/stroom-query/compare/v2.2.20...v2.2.21
[v2.2.20]: https://github.com/gchq/stroom-query/compare/v2.2.19...v2.2.20
[v2.2.19]: https://github.com/gchq/stroom-query/compare/v2.2.18...v2.2.19
[v2.2.18]: https://github.com/gchq/stroom-query/compare/v2.2.17...v2.2.18
[v2.2.17]: https://github.com/gchq/stroom-query/compare/v2.2.16...v2.2.17
[v2.2.16]: https://github.com/gchq/stroom-query/compare/v2.2.15...v2.2.16
[v2.2.15]: https://github.com/gchq/stroom-query/compare/v2.2.14...v2.2.15
[v2.2.14]: https://github.com/gchq/stroom-query/compare/v2.2.13...v2.2.14
[v2.2.13]: https://github.com/gchq/stroom-query/compare/v2.2.12...v2.2.13
[v2.2.12]: https://github.com/gchq/stroom-query/compare/v2.2.11...v2.2.12
[v2.2.11]: https://github.com/gchq/stroom-query/compare/v2.2.10...v2.2.11
[v2.2.10]: https://github.com/gchq/stroom-query/compare/v2.2.9...v2.2.10
[v2.2.9]: https://github.com/gchq/stroom-query/compare/v2.2.8...v2.2.9
[v2.2.8]: https://github.com/gchq/stroom-query/compare/v2.2.7...v2.2.8
[v2.2.7]: https://github.com/gchq/stroom-query/compare/v2.2.6...v2.2.7
[v2.2.6]: https://github.com/gchq/stroom-query/compare/v2.2.5...v2.2.6
[v2.2.5]: https://github.com/gchq/stroom-query/compare/v2.2.4...v2.2.5
[v2.2.4]: https://github.com/gchq/stroom-query/compare/v2.2.3...v2.2.4
[v2.2.3]: https://github.com/gchq/stroom-query/compare/v2.2.2...v2.2.3
[v2.2.2]: https://github.com/gchq/stroom-query/compare/v2.2.1...v2.2.2
[v2.2.1]: https://github.com/gchq/stroom-query/compare/v2.2.0...v2.2.1
[v2.2.0]: https://github.com/gchq/stroom-query/compare/v2.2-beta.6...v2.2.0
[v2.2-beta.7]: https://github.com/gchq/stroom-query/compare/v2.2-beta.6...v2.2-beta.7
[v2.2-beta.6]: https://github.com/gchq/stroom-query/compare/v2.2-beta.5...v2.2-beta.6
[v2.2-beta.5]: https://github.com/gchq/stroom-query/compare/v2.2-beta.4...v2.2-beta.5
[v2.2-beta.4]: https://github.com/gchq/stroom-query/compare/v2.2-beta.3...v2.2-beta.4
[v2.2-beta.3]: https://github.com/gchq/stroom-query/compare/v2.2-beta.2...v2.2-beta.3
[v2.2-beta.2]: https://github.com/gchq/stroom-query/compare/v2.2-beta.1...v2.2-beta.2
[v2.2-beta.1]: https://github.com/gchq/stroom-query/compare/v2.1-beta.23...v2.2-beta.1
[v2.1-beta.23]: https://github.com/gchq/stroom-query/compare/v2.1-beta.22...v2.1-beta.23
[v2.1-beta.22]: https://github.com/gchq/stroom-query/compare/v2.1-beta.21...v2.1-beta.22
[v2.1-beta.21]: https://github.com/gchq/stroom-query/compare/v2.1-beta.20...v2.1-beta.21
[v2.1-beta.20]: https://github.com/gchq/stroom-query/compare/v2.1-beta.19...v2.1-beta.20
[v2.1-beta.19]: https://github.com/gchq/stroom-query/compare/v2.1-beta.18...v2.1-beta.19
[v2.1-beta.18]: https://github.com/gchq/stroom-query/compare/v2.1-beta.17...v2.1-beta.18
[v2.1-beta.17]: https://github.com/gchq/stroom-query/compare/v2.1-beta.16...v2.1-beta.17
[v2.1-beta.16]: https://github.com/gchq/stroom-query/compare/v2.1-beta.15...v2.1-beta.16
[v2.1-beta.15]: https://github.com/gchq/stroom-query/compare/v2.1-beta.14...v2.1-beta.15
[v2.1-beta.14]: https://github.com/gchq/stroom-query/compare/v2.1-beta.13...v2.1-beta.14
[v2.1-beta.13]: https://github.com/gchq/stroom-query/compare/v2.1-beta.12...v2.1-beta.13
[v2.1-beta.12]: https://github.com/gchq/stroom-query/compare/v2.1-beta.11...v2.1-beta.12
[v2.1-beta.11]: https://github.com/gchq/stroom-query/compare/v2.1-beta.10...v2.1-beta.11
[v2.1-beta.10]: https://github.com/gchq/stroom-query/compare/v2.1-beta.9...v2.1-beta.10
[v2.1-beta.9]: https://github.com/gchq/stroom-query/compare/v2.1-beta.8...v2.1-beta.9
[v2.1-beta.8]: https://github.com/gchq/stroom-query/compare/v2.1-beta.7...v2.1-beta.8
[v2.1-beta.7]: https://github.com/gchq/stroom-query/compare/v2.1-beta.6...v2.1-beta.7
[v2.1-beta.6]: https://github.com/gchq/stroom-query/compare/v2.1-beta.5...v2.1-beta.6
[v2.1-beta.5]: https://github.com/gchq/stroom-query/compare/v2.1-beta.4...v2.1-beta.5
[v2.1-beta.4]: https://github.com/gchq/stroom-query/compare/v2.0.0-beta.3...v2.1-beta.4
[v2.0-beta.3]: https://github.com/gchq/stroom-query/compare/v2.0.0-beta.2...v2.0-beta.3
[v2.0-beta.2]: https://github.com/gchq/stroom-query/compare/v2.0.0-beta.1...v2.0-beta.2
[v2.0-beta.1]: https://github.com/gchq/stroom-query/compare/v2.0.0-alpha.28...v2.0-beta.1
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
