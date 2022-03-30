# Events
My take on an event system in Java. It was originally developed for [Project 16x16](https://github.com/Stephcraft/Project-16x16) followed by the improved version v2 for [Zombiecraft](https://zc.stephcraft.net).

## V2 
[Demo](https://github.com/Stephcraft/Events/blob/main/v2/src/main/java/net/stephcraft/events/Demo.java)
|
[Post](https://discourse.processing.org/t/an-event-system-for-processing-4/32564)

##### Features
* Reflectionless implementation of events using functional interfaces and generics
* Support for double colon operator `::` for example `this::onSetup`
* Error handeling, for instance parameter check and listener method reference
* `trigger(...)`
* `bind(method)` / `unbind(method)` / `unbind()`
* `bind(method, priority, ignoreCancelled)` listener priority and cancel handeling
* `bound(method)`
* `cancel()` / `setCancelled(cancelled)` / `isCancelled()`
* `Event.flush(object)` failsafe measure in uncontrolled cases to prevent memory leaks
* `Variable<T>` implementation to modify the impact of events
* Events can pass from `0` to `9` parameters by default, generate more at your needs
* Chronicled events (pre and post event)
* Event priority
* Nested event calls

##### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.Stephcraft.Events</groupId>
    <artifactId>v2</artifactId>
    <version>main-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## V1
[Demo](https://github.com/Stephcraft/Events/blob/main/v1/src/main/java/net/stephcraft/events/Demo.java)
|
[Post](https://discourse.processing.org/t/event-binding-for-processing/17291)

##### Features
* Interfaceless implementation of events utilizing reflection
* `trigger(...)`
* `bind(reference, methodName)` / `unbind(reference, methodName)`
* `bound(reference, methodName)`

##### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.Stephcraft.Events</groupId>
    <artifactId>v1</artifactId>
    <version>main-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
