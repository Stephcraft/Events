package net.stephcraft.events;

public class Demo {

	public static void main(String[] args) {
		new World();
	}
	
	// this object host events
	// note that it could also listen to events
	protected static class Player {
		
		public final Event onJump;
		public final Event onCollect;
		
		int x, y, points;
		
		public Player() {
			
			// define event parameter types here
			onJump = new Event();
			onCollect = new Event(Integer.class);
		}
		
		public void jump() {
			y += 10;
			
			// event: /!\ must match parameter types provided in constructor
			onJump.trigger();
		}
		
		public void collect(int points) {
			this.points += points;
			
			// event: /!\ must match parameter types provided in constructor
			onCollect.trigger(points);
		}
	}
	
	// this object listens to the player's events
	protected static class World {
		
		Player player;
		
		boolean doublePoints;
		boolean noPoints;
		boolean test;
		
		public World() {
			player = new Player();
			player.onJump.bind(this, "onPlayerJump");
			player.onCollect.bind(this, "onPlayerCollect");
			
			// --- playground ---
			
			player.jump();
			
			player.collect(100);
			
			doublePoints = true;
			
			player.collect(100);
			
			noPoints = true;
			
			player.collect(1000);
			
			player.onJump.unbind(this, "onPlayerJump");
			
			player.jump();
		}
		
		// feedback for when the player jumps
		public void onPlayerJump() {
			System.out.println("Jump! :D");
		}
		
		// feedback for when the player collects points.
		public void onPlayerCollect(Integer points) {
			System.out.println("Player collected " + points + " points!");
		}
		
		// WARNING: since binding has a reference to the listener's class
		// this can cause memory leaks if not used properly
		public void flush() {
			
			// unbind when no longer needed
			player.onJump.unbind(this, "onPlayerJump");
			player.onCollect.unbind(this, "onPlayerCollect");
		}
	}
}
