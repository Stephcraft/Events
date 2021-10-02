# Events
My take on an event system in Java. Originally developed for [Zombiecraft](https://zc.stephcraft.net).

### V1
##### Features
* Interfaceless implementation of events utilizing reflection
* `trigger(...)`
* `bind(reference, methodName)` / `unbind(reference, methodName)`
* `bound(reference, methodName)`

##### Post  
> https://discourse.processing.org/t/event-binding-for-processing/17291

### V2
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

##### Post
> Not yet
