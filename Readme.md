boneidle
========
**Annotation-driven lazy-loading for people too bone idle to write it themselves**

## What is it?
Got a bunch of data you want to load in to your class just once, but don't want to write all those tedious ifs yourself?
Don't worry, boneidle has got you covered with these three easy steps:

1. Write a method to do the loading
2. Annotate the lazy-loaded getters with `@LazyLoadWith` (or any other method you want to trigger the loader). Annotate the class to make all methods lazily-loaded by default.
3. Create your lazy-loading proxy with `LazyFactory.proxy()`

For more information see [boneidle.io](http://boneidle.io).

## Where do I get it?
[Maven Central](http://search.maven.org/#artifactdetails%7Cio.boneidle%7Cboneidle-core%7C1.0.0%7Cjar). You can add a dependency to your project like this:

```xml
<dependency>
    <groupId>io.boneidle</groupId>
    <artifactId>boneidle-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

There are two artifacts to be aware of:
- **boneidle-annotations** which contains _only_ the annotation classes, for use with marking up a class for lazy loading
- **boneidle-core** which includes boneidle-annotations _plus_ the classes needing for generating lazy-loading proxies.

If you're not sure which you want, use boneidle-core; boneidle-annotations is intended for use with library modules/projects that define lazily loaded classes, but don't actually make use of them.

## Versioning
Version numbers follow the Semantic Versioning ([semver.org](http://semver.org/)) convention.

## Potential future improvements
Ways in which boneidle could be even better include:

* Smarter / configurable strategies for deciding when data needs to be loaded. For example:
 * If the `@LazyLoadWith` loader has been invoked once
 * If _any_ loader specified (by a new annotation?) has been invoked
 * If the field backing the bean getter is not null (for bean getters only, obviously)
* Extend support for `@LazyLoadWith` on the class:
 * Perhaps add different inclusion filters (e.g. all methods, only public methods, only getter methods)
* Build on Travis (or similar)
