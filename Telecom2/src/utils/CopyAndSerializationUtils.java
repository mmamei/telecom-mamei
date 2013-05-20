package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import analysis.PlsEvent;

public class CopyAndSerializationUtils {
	public static List<PlsEvent> clone(List<PlsEvent> events) {
		List<PlsEvent> result = new ArrayList<PlsEvent>();
		for(PlsEvent e: events)
			result.add(e.clone());
		return result;
	}
	
	
	public static Object restore(File file) {
		Object o = null;
		try{
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			o = ois.readObject();
			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public static void save(File file, Object o) {
		try {
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		oos.writeObject(o);
		oos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
