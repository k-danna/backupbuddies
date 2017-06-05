package backupbuddies;

import java.util.List;
import java.util.Arrays;
import java.io.File;

import backupbuddies.shared.IInterface;
//import backupbuddies.network.Network;

public class UnitTest {

    static List<String> log =  IInterface.INSTANCE.getEventLog();

    public static List<String> updateLog() {
        return IInterface.INSTANCE.getEventLog();
    }

    public static void testLogin() {
        //inputs = known, unknown, blank ip handled in prog
        List<String> ips = Arrays.asList(
                "unknownipadress", "planetguy.sandcats.io");
        List<String> passwords = Arrays.asList(
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

    public static void testLoadNetwork() {
        //execute with test input, none in this case
        IInterface.INSTANCE.loadNetwork();
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testSaveNetwork() {
        //execute with test input, none in this case
        IInterface.INSTANCE.saveNetwork();
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testUploadFile() {
        //inputs = known, unknown, blank strings handled in prog
        List<String> ips = Arrays.asList(
                "unknownipadress", "planetguy.sandcats.io");
        File[] inFile = new File("MANIFEST.MF").listFiles();
        //execute with test input, none in this case
        for (String peer: ips) {
            IInterface.INSTANCE.uploadFile(inFile, peer);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testDownloadFile() {
        //inputs = known, unknown, blank strings handled in prog
        List<String> ips = Arrays.asList(
                "unknownipadress", "planetguy.sandcats.io");
        String inFile = "unknownfile";
        //execute with test input, none in this case
        for (String peer: ips) {
            IInterface.INSTANCE.downloadFile(inFile, peer);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testSetStoragePath() {
        //inputs = known, unknown, blank strings handled in prog
        List<String> paths = Arrays.asList(
                "unknownpath", "./");
        //execute with test input, none in this case
        for (String path : paths) {
            IInterface.INSTANCE.setStoragePath(path);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testSetEncryptKey() {
        //inputs = string. longer than 16 char string, blank strings handled in prog
        List<String> keys = Arrays.asList(
                "key1", "keylongerthan16characters");
        //execute with test input, none in this case
        for (String key : keys) {
            IInterface.INSTANCE.setEncryptKey(key);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testSetDisplayName() {
        //inputs = string. longer than 16 char string, blank strings handled in prog
        List<String> names = Arrays.asList(
                "name1", "name2");
        //execute with test input, none in this case
        for (String name : names) {
            IInterface.INSTANCE.setDisplayName(name);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testSetStorageSpace() {
        //inputs = less than current, zero, more than current, 
        List<Integer> amounts = Arrays.asList(
                7, 0, 14);
        //execute with test input, none in this case
        for (Integer amount : amounts) {
            IInterface.INSTANCE.setStorageSpace(amount);
        }
        //verify output
        log = updateLog();
        for (String event : log) {
            System.out.printf("%s\n", event);
        }
    }

    public static void testGetUserList() {
        if (IInterface.INSTANCE.fetchUserList() != null) {
            System.out.printf("got user list return\n");
        }
        else {
            System.out.printf("bad user list return\n");
        }
    }

    public static void testGetFileList() {
        if (IInterface.INSTANCE.fetchFileList() != null) {
            System.out.printf("got file list return\n");
        }
        else {
            System.out.printf("bad file list return\n");
        }
    }

    public static void testGetDisplayName() {
        if (IInterface.INSTANCE.getDisplayName() != null) {
            System.out.printf("got display name return\n");
        }
        else {
            System.out.printf("bad display name return\n");
        }
    }

    public static void testGetStorageSpace() {
        int space = IInterface.INSTANCE.getStorageSpace();
        if (space >= 0) {
            System.out.printf("got storage space return\n");
        }
        else {
            System.out.printf("bad storage space return\n");
        }
    }

    public static void testGetStorageSpaceLimit() {
        int space = IInterface.INSTANCE.getStorageSpaceLimit();
        if (space >= 0) {
            System.out.printf("got storage space return return\n");
        }
        else {
            System.out.printf("bad storage space limit return\n");
        }
    }

    public static void testNetworkExists() {
        if (IInterface.INSTANCE.networkExists()) {
            System.out.printf("got network return\n");
        }
        else {
            System.out.printf("bad network return\n");
        }
    }

    public static void testUnits() {
        //units tests are independent, order doesnt matter
        testLogin();
        testLoadNetwork();
        testSaveNetwork();
        testUploadFile();
        testDownloadFile();
        testSetStoragePath();
        testSetEncryptKey();
        testSetDisplayName();
        testSetStorageSpace();
        testGetUserList();
        testGetFileList();
        testGetDisplayName();
        testGetStorageSpace();
        testGetStorageSpaceLimit();
        testNetworkExists();
        
        System.exit(0);

    }

}

