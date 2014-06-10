package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import analysis.PLSEvent;

public class CopyAndSerializationUtils {
	public static List<PLSEvent> clone(List<PLSEvent> events) {
		List<PLSEvent> result = new ArrayList<PLSEvent>();
		for(PLSEvent e: events)
			result.add(e.clone());
		return result;
	}
	
	
	public static Object restore(File file) {
		Object o = null;
		ObjectInputStream ois = null;
		try{
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			o = ois.readObject();
			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return o;
	}
	
	public static void save(File file, Object o) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			oos.writeObject(o);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
