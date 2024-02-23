import edu.princeton.cs.algs4.StdDraw;

public class CollisionSystem {

    private static final double HZ = 0.5;    // number of redraw events per clock tick

    private MinPQ<Event> pq;
    private double t = 0.0;
    private final Particle[] particles;

    public CollisionSystem(Particle[] particles) {
        this.particles = particles.clone(); // copy
    }

    // updates priority queue with all new events for particle a
    private void predict(Particle a, double limit) {

        if(a == null) return;

        // update pq for this particle
        for (Particle particle : particles) {
            double dt = a.timeToHit(particle);
            if (dt < limit)
                pq.insert(new Event(t + dt, a, particle));
        }

        // make pq empty

        double dtv = a.timeToHitVerticalWall();
        double dth = a.timeToHitHorizontalWall();

        if(t + dtv < limit)
            pq.insert(new Event(t + dtv , a , null));
        if(t + dth < limit)
            pq.insert(new Event(t + dth , null , a));

    }

    private void redraw(double limit) {
        StdDraw.clear();
        for (Particle particle : particles) {
            particle.draw();
        }
        StdDraw.show();
        StdDraw.pause(20);
        if (t < limit) {
            pq.insert(new Event(t + 1.0 / HZ, null, null));
            if(t <10 ) System.out.println(t + 1.0 / HZ);
        }
    }

    // simulates the system of particles for the specified amount of time.
    public void simulate(double limit) throws InterruptedException {

        // initial predictions & initial draw

        pq = new MinPQ<>();
        for (Particle particle : particles) {
            predict(particle, limit);
        }
        pq.insert(new Event(0 , null , null));

        // look for every event forward

        while (!pq.isEmpty()) {

            Event e = pq.delMin();
            if(!e.isValid()) continue;
            Particle a = e.a;
            Particle b = e.b;

            for (Particle particle : particles) {
                particle.move(e.time - t);
            }
            t = e.time;


            if(e.a == null && e.b == null) {
                redraw(limit);
            } else if(e.a != null && e.b == null) {
                e.a.bounceOffVerticalWall();
            } else if(e.a == null) {
                e.b.bounceOffHorizontalWall();
            } else {
                e.a.bounceOff(e.b);
            }

            // new predictions for updated velocities of particles
            predict(a , limit);
            predict(b , limit);
        }

    }

    public static void main(String[] args) {

        StdDraw.setCanvasSize(600, 600);

        // enable double buffering
        StdDraw.enableDoubleBuffering();

        // the array of particles
        Particle[] particles;

        // n random particles
        int n = 20;
        particles = new Particle[n];
        for (int i = 0; i < n; i++)
            particles[i] = new Particle();



        // create collision system and simulate
        CollisionSystem system = new CollisionSystem(particles);
        try {
            system.simulate(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
