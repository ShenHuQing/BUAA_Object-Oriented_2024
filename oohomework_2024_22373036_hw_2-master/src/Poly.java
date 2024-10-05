import java.util.ArrayList;

public class Poly {
    private ArrayList<Mono> monos;

    public Poly() {
        this.monos = new ArrayList<>();
    }

    public void addTerm(Mono mono) {
        monos.add(mono);
    }

    @Override
    public String toString() {
        StringBuilder polyBuilder = new StringBuilder();
        for (int i = 0; i < monos.size(); i++) {
            polyBuilder.append(monos.get(i).toString());
            if (i < monos.size() - 1) {
                polyBuilder.append(" + ");
            }
        }
        return polyBuilder.toString();
    }

    public int size() {
        return monos.size();
    }
}