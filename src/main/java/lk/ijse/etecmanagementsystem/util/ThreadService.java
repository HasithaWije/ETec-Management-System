package lk.ijse.etecmanagementsystem.util;

public class ThreadService {

    private static Thread inventoryLoadingThread;

    static {
        inventoryLoadingThread = new Thread(() -> {
        });
    }

    ThreadService() {
    }

    ;


    public static void setInventoryLoadingThread(Thread thread) {
        inventoryLoadingThread = thread;
    }

    public static Thread getInventoryLoadingThread() {

        return inventoryLoadingThread;
    }
}
