import java.util.Comparator;

public class ProgramRequestComparator implements Comparator<ProgramRequest> {
    @Override
    public int compare(ProgramRequest o1, ProgramRequest o2) {
        if(o1.getMemory() < o2.getMemory()) return -1;
        else if(o1.getMemory() > o2.getMemory()) return 1;
        else return 0;
    }
}
