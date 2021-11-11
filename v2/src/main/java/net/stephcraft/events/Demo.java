package net.stephcraft.events;

import net.stephcraft.events.settings.EventFlag;
import net.stephcraft.events.variable.Variable;

public class Demo {
	
	public static void main(String[] args) {
		new World();
	}
	
	// this object host events
	// note that it could also listen to events
	public static class Player {
		
		public final Event.P0 onJump;
		public final Event.P1<Variable<Integer>> onCollect;
		
		int x, y, points;
		
		public Player() {
			onJump = Event.P0.create();
			onCollect = Event.P1.create(EventFlag.CANCELLABLE);
		}
		
		public void jump() {
			y += 10;
			
			// event
			onJump.trigger();
		}
		
		public void collect(int points) {
			Variable<Integer> variable = Variable.of(points);
			
			// event
			onCollect.trigger(variable);
			
			// check if event was cancelled, if not continue the process
			if(!onCollect.isCancelled() && !variable.isNull())
				this.points += points;
		}
	}
	
	// this object listens to the player's events
	public static class World {
		
		public final Listener.P0 onPlayerJump = this::onPlayerJump;
		public final Listener.P1<Variable<Integer>> onPlayerCollect = this::onPlayerCollect;
		public final Listener.P1<Variable<Integer>> onPlayerCollected = this::onPlayerCollected;
		
		Player player;
		
		boolean doublePoints;
		boolean noPoints;
		boolean test;
		
		public World() {
			player = new Player();
			player.onJump.bind(onPlayerJump);
			player.onCollect.bind(onPlayerCollect, 0);
			player.onCollect.bind(onPlayerCollected, 1, true);
			
			// --- playground ---
			
			player.jump();
			
			player.collect(100);
			
			doublePoints = true;
			
			player.collect(100);
			
			noPoints = true;
			
			player.collect(1000);
			
			player.onJump.unbind(onPlayerJump);
			
			player.jump();
		}
		
		// feedback for when the player jumps
		public void onPlayerJump() {
			System.out.println("Jump! :D");
			
			// WARNING: this is not allowed. cancel() can only be called:
			// #1 on events that are cancellable: 'Event.PX.create(true)' 
			// #2 inside a listener method, othwerwise will throw an exception
			if(test) player.onJump.cancel();
		}
		
		// apply no points and double points modifiers to points collected by player
		public void onPlayerCollect(Variable<Integer> points) {
			if(noPoints)
				player.onCollect.cancel();
			else
				points.apply((p) -> doublePoints? p * 2 : p);
			
			System.out.println("[info] applied points modifier");
		}
		
		// feedback for when the player collects points.
		// two listeners are bound to the same event, but this one
		// has a higher priority so it is executed last showing the final
		// points collected. This will not execute if ignoreCancelled is
		// set to true during binding.
		public void onPlayerCollected(Variable<Integer> points) {
			System.out.println("Player collected " + points.get() + " points!");
		}
		
		public void flush() {
			
			// WARNING: will not unbind, the operator :: creates a new immutable reference
			player.onJump.unbind(this::onPlayerJump);
			
			// that's why we kept a final reference Listener for each
			player.onJump.unbind(onPlayerJump);
			player.onCollect.unbind(onPlayerCollect);
			
			// WARNING: since binding has a reference to the listener's container class method
			// this can cause memory leaks if not used properly
			// in uncontrolled scenarios, this method can be used as a failsafe when you are
			// done with an object.
			Event.flush(this);
		}
	}
}
