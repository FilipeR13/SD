import java.util.Comparator;

public class ProgramRequestComparator implements Comparator<ProgramRequest> {

    /*
    *   Function: compare
    *   Description: compares two program requests by priority, and in case of a tie, by memory.
    *   The lower the priority, the quicker it is to be executed and if the priority is the same, the one that occupies less memory is executed first.
    */
    @Override
    public int compare(ProgramRequest o1, ProgramRequest o2) {
        
        if (o1.getPriority() < o2.getPriority()) {
            return -1;
        } else if (o1.getPriority() > o2.getPriority()) {
            return 1;
        } else {
            if (o1.getMemory() < o2.getMemory()) {
                return -1;
            } else if (o1.getMemory() > o2.getMemory()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
