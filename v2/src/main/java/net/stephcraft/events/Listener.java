package net.stephcraft.events;

public interface Listener {
	interface P0 { void execute(); }
	interface P1<A> { void execute(A a); }
	interface P2<A,B> { void execute(A a, B b); }
	interface P3<A,B,C> { void execute(A a, B b, C c); }
	interface P4<A,B,C,D> { void execute(A a, B b, C c, D d); }
	interface P5<A,B,C,D,E> { void execute(A a, B b, C c, D d, E e); }
	interface P6<A,B,C,D,E,F> { void execute(A a, B b, C c, D d, E e, F f); }
	interface P7<A,B,C,D,E,F,G> { void execute(A a, B b, C c, D d, E e, F f, G g); }
	interface P8<A,B,C,D,E,F,G,H> { void execute(A a, B b, C c, D d, E e, F f, G g, H h); }
	interface P9<A,B,C,D,E,F,G,H,I> { void execute(A a, B b, C c, D d, E e, F f, G g, H h, I i); }
}
