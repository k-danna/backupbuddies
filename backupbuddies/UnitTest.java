package backupbuddies;

import java.util.List;
import java.util.Arrays;

import backupbuddies.shared.IInterface;
//import backupbuddies.network.Network;

public class UnitTest {

    static List<String> log =  IInterface.INSTANCE.getEventLog();

    public static List<String> updateLog() {
        return IInterface.INSTANCE.getEventLog();
    }

    public static void testLogin() {
        //unit: void IInterface.INSTANCE.login(String ip, String pass);
        //inputs = known, unknown, blank ip
        List<String> ips = Arrays.asList("", 
                "unknownipadress", "planetguy.sandcats.io");
        List<String> passwords = Arrays.asList("", 
                "incorrectpass", "walrus");
        //execute with test input
        for (String ip : ips) {
            for (String pass : passwords) {
                IInterface.INSTANCE.login(ip, pass);
            }
        }
        
        //verify output
        log = updateLog();

        for (String event : log) {
            System.out.printf("%s\n", event);
        }

    }

    public static void testUnits() {
        
        testLogin();

        
        /*
        boolean loadNetwork = loadNetwork();
        boolean saveNetwrok = saveNetwork();
        boolean uploadFile = uploadFile(File[] files, String peerName);
        void downloadFile(String fileName, String fileDir);
        DefaultListModel<ListModel> userList = fetchUserList();
        DefaultListModel<ListModel> fileList = fetchFileList();
        void setStoragePath(String fileDir);
        void setEncryptKey(String key);
        void setDisplayName(String newName);
        String getDisplayName();
        void setStorageSpace(int amount);
        int storage = getStorageSpace();
        int storageLimit = getStorageSpaceLimit();
        boolean networkExists();
        */

    }

}

