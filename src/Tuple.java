public class Tuple {

    int integerVar;
    double doubleVar;

    Tuple (int integerVar, double doubleVar) {

        this.integerVar = integerVar;
        this.doubleVar = doubleVar;
    }

    public int getId() {

        return this.integerVar;
    }

    public double getRank() {

        return this.doubleVar;
    }

}
