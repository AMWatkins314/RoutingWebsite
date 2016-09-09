package project;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Salesman {

    public static int NUMBER_PARENTS = 2;
    public static int NUMBER_CHILDREN = 4;
    public static double MUTATION_LIKELIHOOD = 1;

    public static void main(String[] args) throws InterruptedException {
        int iOffset = 0;
        // flags defined in the help() function
        List<String> validFlags = Arrays.asList("-T", "-S", "-G", "-H");
        int[] flagArgs = {1, -1, -1};
        for (int i = 0; i < args.length; i+=2) {
            int index = validFlags.indexOf(args[i].toUpperCase());
            if (index >= 0){
                if (index == 3) {
                    help();
                    i = args.length;
                    return;
                }
                try {
                    int flagValue = Integer.parseInt(args[i+1]);
                    flagArgs[index] = (flagValue > 0) ? flagValue : flagArgs[index];
                    iOffset += 2;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("ERROR: "+args[i]+" = "+args[i+1]+" is not a valid flag value");
                }
            } else {
                i = args.length;
            }
        }
        GPSCoordinate[] coordinates = new GPSCoordinate[(args.length - iOffset)/3];
        for (int i = iOffset; i < args.length; i+=3) {
            coordinates[(i - iOffset)/3] = new GPSCoordinate(args[i], args[i+1], args[i+2]);
        }
        MUTATION_LIKELIHOOD = (100.0/((double)coordinates.length))/1000.0;
        //GenerationController controller = new GenerationController(coordinates);
        long startTime = System.currentTimeMillis();
        long runtime = (flagArgs[1] > 0) ? flagArgs[1] * 1000 : Long.MAX_VALUE;
        long maxNumberRuns = (flagArgs[2] > 0) ? flagArgs[2] : (runtime == Long.MAX_VALUE) ? 300000 : Long.MAX_VALUE;

        int numberThreads = (flagArgs[0] > 0) ? flagArgs[0] : 1;
        ExecutorService service = Executors.newCachedThreadPool();
        final GenerationController[] controllers = new GenerationController[numberThreads];
        for (int j = 0; j < numberThreads; j++) {
            final int finalJ = j;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    controllers[finalJ] = new GenerationController(coordinates);
                    boolean running = true;
                    for (long i = 0; i < maxNumberRuns && running; i++) {
                        controllers[finalJ].nextGeneration();
                        if (System.currentTimeMillis() - startTime > runtime) {
                            running = false;
                        }
                    }
                }
            });
        }
        service.shutdown();
        boolean finished = service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        GenerationController bestController = controllers[0];
        for (int i = 1; i < controllers.length; i++) {
            if (bestController.getBestSoFarDistance() > controllers[i].getBestSoFarDistance())
                bestController = controllers[i];
        }
    }

    public static void help() {
        String[] instructions = {
                "Welcome to the RouteMe backend!",
                "",
                "Flags must proceed all destinations and may be defined as follows:",
                "   -T [Number of threads]",
                "   -S [Length of runtime in seconds]",
                "   -G [Number of generations]",
                "   -H Displays this help menu",
                "Flags will be triggered as soon as their criteria is met; the first flag to trigger will override all the others",
                "",
                "Locations may be passed in the following format:",
                "   [URL encoded title] [latitude] [longitude]",
                "",
                "Happy Travels!"
        };
        for (int i = 0; i < instructions.length; i++) {
            System.out.println(instructions[i]);
        }
    }

    public static String getSortedOrder(String arg) {
        String[] args = arg.split(" ");
        int iOffset = 0;
        // flags defined in the help() function
        List<String> validFlags = Arrays.asList("-T", "-S", "-G", "-H");
        int[] flagArgs = {1, -1, -1};
        for (int i = 0; i < args.length; i+=2) {
            int index = validFlags.indexOf(args[i].toUpperCase());
            if (index >= 0){
                if (index == 3) {
                    help();
                    i = args.length;
                    return "";
                }
                try {
                    int flagValue = Integer.parseInt(args[i+1]);
                    flagArgs[index] = (flagValue > 0) ? flagValue : flagArgs[index];
                    iOffset += 2;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("ERROR: "+args[i]+" = "+args[i+1]+" is not a valid flag value");
                }
            } else {
                i = args.length;
            }
        }
        GPSCoordinate[] coordinates = new GPSCoordinate[(args.length - iOffset)/3];
        for (int i = iOffset; i < args.length; i+=3) {
            coordinates[(i - iOffset)/3] = new GPSCoordinate(args[i], args[i+1], args[i+2]);
        }
        MUTATION_LIKELIHOOD = (100.0/((double)coordinates.length))/1000.0;
        //GenerationController controller = new GenerationController(coordinates);
        long startTime = System.currentTimeMillis();
        long runtime = (flagArgs[1] > 0) ? flagArgs[1] * 1000 : Long.MAX_VALUE;
        long maxNumberRuns = (flagArgs[2] > 0) ? flagArgs[2] : (runtime == Long.MAX_VALUE) ? 300000 : Long.MAX_VALUE;

        int numberThreads = (flagArgs[0] > 0) ? flagArgs[0] : 1;
        ExecutorService service = Executors.newCachedThreadPool();
        final GenerationController[] controllers = new GenerationController[numberThreads];
        for (int j = 0; j < numberThreads; j++) {
            final int finalJ = j;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    controllers[finalJ] = new GenerationController(coordinates);
                    boolean running = true;
                    for (long i = 0; i < maxNumberRuns && running; i++) {
                        controllers[finalJ].nextGeneration();
                        if (System.currentTimeMillis() - startTime > runtime) {
                            running = false;
                        }
                    }
                }
            });
        }
        service.shutdown();
        try {
            boolean finished = service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GenerationController bestController = controllers[0];
        for (int i = 1; i < controllers.length; i++) {
            if (bestController.getBestSoFarDistance() > controllers[i].getBestSoFarDistance())
                bestController = controllers[i];
        }
        return bestController.googleMapsUrlAppend();
    }
}

class GPSCoordinate {
    String address;
    double latitude;
    double longitude;

    public GPSCoordinate(String address, String latitude, String longitude) {
        try {
            this.address = java.net.URLDecoder.decode(address, "UTF-8");
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GPSCoordinate(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

class CoordinateOrder {
    int[] positions;
    double fitness;

    public CoordinateOrder(int numberPositions) {
        this.positions = new int[numberPositions];
        for (int i = 0; i < numberPositions; i++)
            this.positions[i] = i;
    }

    public CoordinateOrder(int[] positions) {
        this.positions = positions;
    }

    public CoordinateOrder duplicate(GPSCoordinate[] destinations) {
        int[] positionsCopy = new int[this.positions.length];
        for (int i = 0; i < positionsCopy.length; i++)
            positionsCopy[i] = this.positions[i];
        CoordinateOrder retVal = new CoordinateOrder(positionsCopy);
        retVal.calculateFitness(destinations);
        return retVal;
    }

    public int[] getPositions() {
        return positions;
    }

    public void setPositions(int[] positions) {
        this.positions = positions;
    }

    public int getLength() {
        return this.positions.length;
    }

    public void setValue(int pos, int value) {
        this.positions[pos] = value;
    }

    public void switchValues(int posA, int posB) {
        int temp = this.positions[posA];
        this.positions[posA] = this.positions[posB];
        this.positions[posB] = temp;
    }

    public int getValueAtPos(int pos) {
        return this.positions[pos];
    }

    public double calculateFitness(GPSCoordinate[] destinations) {
        this.fitness = 0.0;
        for (int i = 1; i < this.positions.length+1; i++) {
            GPSCoordinate toDest;
            GPSCoordinate fromDest = destinations[this.positions[i-1]];
            if (i == this.positions.length)
                toDest = destinations[0];
            else
                toDest = destinations[this.positions[i]];
            this.fitness += Math.sqrt(Math.pow(toDest.latitude - fromDest.latitude, 2) + Math.pow(toDest.longitude - fromDest.longitude, 2));
        }
        return this.fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public void mutate() {

    }
}

class GenerationController {
    GPSCoordinate[] destinations;
    CoordinateOrder[] parents;
    CoordinateOrder[] children;
    CoordinateOrder bestSoFar;
    int generationNumber;

    public GenerationController(GPSCoordinate[] destinations) {
        this.destinations = destinations;
        this.parents = new CoordinateOrder[Salesman.NUMBER_PARENTS];
        for (int i = 0; i < this.parents.length; i++)
            parents[i] = new CoordinateOrder(destinations.length);
        this.children = new CoordinateOrder[Salesman.NUMBER_CHILDREN];
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = this.parents[0].duplicate(this.destinations);
            this.children[i].calculateFitness(this.destinations);
        }
        this.bestSoFar = this.parents[0].duplicate(this.destinations);
        this.generationNumber = 0;
    }

    public void nextGeneration() {
        int mostFitChild = 0;
        for (int i = 0; i < this.parents.length; i++) {
            for (int j = 1; j < this.children.length; j++)
                if (this.children[j] != null)
                    if (this.children[j].getFitness() < this.children[mostFitChild].getFitness())
                        mostFitChild = j;
            this.parents[i] = this.children[mostFitChild].duplicate(this.destinations);
            this.children[mostFitChild] = null;
            for (int j = 0; j < this.children.length; j++) {
                if (this.children[j] != null) {
                    mostFitChild = j;
                    j = this.children.length;
                }
            }
        }
        if (this.parents[0].getFitness() < this.bestSoFar.getFitness()) {
            System.out.println(getBestSoFarDistance() + " -> " + this.parents[0].getFitness() * 111);
            this.bestSoFar = this.parents[0].duplicate(this.destinations);
        }
        for (int i = 0; i < this.children.length; i++) {
            this.children[i] = Mutator.makeChild(this.parents);
            this.children[i].calculateFitness(this.destinations);
        }
        this.generationNumber++;
    }

    public GPSCoordinate[] getDestinations() {
        return destinations;
    }

    public void setDestinations(GPSCoordinate[] destinations) {
        this.destinations = destinations;
    }

    public CoordinateOrder[] getParents() {
        return parents;
    }

    public void setParents(CoordinateOrder[] parents) {
        this.parents = parents;
    }

    public CoordinateOrder[] getChildren() {
        return children;
    }

    public void setChildren(CoordinateOrder[] children) {
        this.children = children;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public void setGenerationNumber(int generationNumber) {
        this.generationNumber = generationNumber;
    }

    public CoordinateOrder getBestSoFar() {
        return bestSoFar;
    }

    public String[] getBestSoFarString() {
        String[] retVal = new String[this.bestSoFar.getLength()];
        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = destinations[bestSoFar.getValueAtPos(i)].getAddress();
        }
        return retVal;
    }

    public double getBestSoFarDistance() {
        return this.bestSoFar.getFitness()*111;
    }

    public String googleMapsUrlAppend() {
        String retVal = "";
        for (int i = 0; i < this.bestSoFar.getLength(); i++) {
            GPSCoordinate current = destinations[bestSoFar.getValueAtPos(i)];
            retVal += current.getLatitude()+","+current.getLongitude();
            retVal += (i < this.bestSoFar.getLength()-1) ? "/" : "";
        }
        return retVal;
    }
}

class Mutator {

    public static CoordinateOrder makeChild(CoordinateOrder[] parents) {
        CoordinateOrder newChild = crossover(parents);
        mutate(newChild);
        return newChild;
    }

    public static CoordinateOrder crossover(CoordinateOrder[] parents) {
        Random r = new Random();
        CoordinateOrder newChild = new CoordinateOrder(parents[0].getLength());
        int parentPos = r.nextInt(parents.length);
        for (int i = 0; i < newChild.getLength(); i++)
            newChild.setValue(i, parents[parentPos].getValueAtPos(i));
        return newChild;
    }

    public static void mutate(CoordinateOrder child) {
        Random r = new Random();
        for (int i = 1; i < child.getLength(); i++) {
            if (r.nextDouble() <= Salesman.MUTATION_LIKELIHOOD) {
                child.switchValues(i, r.nextInt(child.getLength()-1)+1);
            }
        }
    }
}
