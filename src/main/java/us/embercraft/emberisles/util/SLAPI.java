package us.embercraft.emberisles.util;

import java.io.*;

public class SLAPI {
	public static void save(Object obj, String path) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
	        oos.writeObject(obj);
	        oos.flush();
        }
    }

    public static Object load(String path) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
	        Object result = ois.readObject();
	        return result;
        }
    }
}
