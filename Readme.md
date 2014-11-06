boneidle
========
**Annotation-driven lazy-loading for people too bone idle to write it themselves**

Got a bunch of data you want to load in to your class just once, but don't want to write all those tedious ifs yourself?
Don't worry, boneidle has got you covered with these three easy steps:

1. Write a method to do the loading
2. Annotate the lazy-loaded getters with `@LazyLoadWith` (or any other method you want to trigger the loader)
3. Create your lazy-loading proxy with `LazyFactory.proxy()`

Example
-------
Suppose you have a data holder class that looks like:

```java
public class DataClass {
    private int id;
    private String name;
    private String description;

    /*
     * Constructors that take arguments, or are even inaccessible, are fine
     */
    private DataClass(int id) {
        this.id = id;
    }

    /*
     * Un-annotated methods are left alone, so you can mix lazy and eager loaded data
     */
    public int getId() {
        return id;
    }

    /*
     * Declare which method to call in order to load the data backing this method using @LazyLoadWith
     */
    @LazyLoadWith("loadData")
    public String getName() {
        return name;
    }

    /*
     * Any method can be annotated; it doesn't have to be a JavaBean getter
     */
    @LazyLoadWith("loadData")
    public String tellMeAboutIt() {
        return description;
    }

    /*
     * The loader method can be private, but must have no parameters
     */
    private void loadData() {
        // Do something expensive, like a big calculation or read from an external source
    }
}
```

You could then use an instance of it like so:

```java
DataClass dataClass = LazyFactory.proxy(new DataClass());

// No lazy loading done here
int id = dataClass.getId();

// Lazy loading done here (i.e. loadData is called)
String name = dataClass.getName();

// But no loading done here, as it's already happened
dataClass.getName();

// Or here - boneidle knows both methods share the same loader
String description = dataClass.tellMeAboutIt();
```

Restrictions
------------
Since boneidle is powered by cglib proxies, there are one or two restrictions:

* The annotated class must not be static
* The annotated class must not be final
* Lazy-loaded data must always be retrieved through the annotated methods, even within the annotated class; accessing
the fields directly won't trigger any lazy loading

Potential future improvements
-----------------------------
Ways in which boneidle could be even better include:

* Smarter / configurable strategies for deciding when data needs to be loaded. For example:
 * If the `@LazyLoadWith` loader has been invoked once
 * If _any_ loader specified (by a new annotation?) has been invoked
 * If the field backing the bean getter is not null (for bean getters only, obviously)
* Support passing a `Class` to `LazyFactory` instead / as well an instance
* Publish to Maven Central
* Build on Travis (or similar)