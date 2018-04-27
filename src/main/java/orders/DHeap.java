package orders;

public class DHeap {
    private int[] heap;
    private int   size;
    private int   arity;

    static int expansion_factor = 2;
    static int default_arity    = 2;

    public DHeap() {
        this(default_arity, 10);
    }

    public DHeap(int arity, int size) {
        this.arity = arity;
        this.heap = new int[size];
        this.size = 0;

    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int findMin() {
        if (size == 0) {
            throw new java.lang.IllegalStateException("Empty Heap");
        }

        return heap[0];
    }

    public void insert(int toBeInserted) {
        int[] heap = this.heap;
        if (size == 0) {
            heap[0] = toBeInserted;
            size++;
            return;
        }


        // Start at the bottom, and search to find where
        // we should insert the new element
        //7
        // Stop when we find a parent node that is small
        // than the element to be inserted, or we reach the top

        int i = size;

        int arity = this.arity;
        for (; heap[(i - 1) / arity] > toBeInserted; i = (i - 1) / arity) {
            // if we've reached the top, do the assignment
            if (i == 0) break;

            // otherwise swap down the parent
            heap[i] = heap[(i - 1) / arity];
        }

        heap[i] = toBeInserted;
        size++;

    }

    public int deleteMin() {
        int size = this.size;
        if (size == 0) {
            throw new java.lang.IllegalStateException("Empty Heap");
        }

        int[] heap     = this.heap;
        int   toReturn = heap[0];

        int lastElement = heap[size - 1];

        int minChild;

        int i = 0;

        for (; (i * arity) + 1 < size; i = minChild) {
            // Assume initially that the smallest child is
            // the first child
            minChild = (i * arity) + 1;

            // There are no children for this node
            if (minChild > size) {
                break;
            }

            // Search through all the children for the
            // smallest value
            int j = 1, currentSmallestChild = minChild;
            for (; j < arity; j++) {
                if (minChild + j == size) break;
                if (heap[currentSmallestChild] > heap[minChild + j])
                    currentSmallestChild = minChild + j;
            }

            minChild = currentSmallestChild;

            // if the minChild that we found is smaller
            // than the last element, we should percolate
            // up the child to the parent and keep searching
            // for a suitable place to put the last element
            if (lastElement > heap[minChild]) {
                heap[i] = heap[minChild];
            } else {
                break;
            }
        }

        heap[i] = lastElement;
        this.size--;
        return toReturn;
    }

    public static void main(String[] args) {
        // NOTE:
        // We could have also exposed an additional PARAM
        // here to control the arity of the D-Heap
        //
        // But default_arity and this proposed new param
        // could only be used mutually exclusively.

        int maxSize = 100000;
        DHeap heap = new DHeap(4, maxSize);

        int i, j;


        for (i = 0, j = maxSize / 2; i < maxSize; i++, j = (j + 71) % maxSize) {
            heap.insert(j);
        }

        for (j = 0; j < maxSize; j++) {
            if (heap.deleteMin() != j) {
                System.out.println("Error in deleteMin: " + j);
            }
        }

        System.out.println("Done...");
    }

}