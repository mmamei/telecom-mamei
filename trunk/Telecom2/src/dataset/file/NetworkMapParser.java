package dataset.file;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import region.NetworkCell;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.Logger;



public class NetworkMapParser {
    public static void main(String[] args) throws Exception {
    		
    		//Config.getInstance().changeDataset("ivory-set3");
            
    		new File(Config.getInstance().base_folder+"/NetworkMapParser").mkdirs();
            
            
            File dir = new File(Config.getInstance().network_map_dir);
            String[] files = dir.list();
            for(String file: files) {
            		System.out.println(dir+"/"+file);
                    BufferedReader in = new BufferedReader(new FileReader(dir+"/"+file));
                    HashMap<String, RegionI> map = new HashMap<String, RegionI>();
                    String line;
                    while((line = in.readLine()) != null){
                            String [] splitted = line.split(":");
                            String description = splitted[0];
                            //int barycentre = Integer.parseInt(splitted[1]);
                            long lac = Long.parseLong(splitted[2]);
                            long cell_id = Long.parseLong(splitted[3]);
                            //String param5 = splitted[4];
                            //String param6 = splitted[5];
                            //String param7 = splitted[6];
                            //String param8 = splitted[7];
                            //String param9 = splitted[8];
                            double barycentre_lat = Double.parseDouble(splitted[9]);
                            double barycentre_lon = Double.parseDouble(splitted[10]);
                            double radius = Double.parseDouble(splitted[11]);
                            
                            String celllac = String.valueOf(lac*65536+cell_id);
                            
                            RegionI cell = new NetworkCell(celllac,description,lac,cell_id,barycentre_lat,barycentre_lon,radius);
                            //System.out.println(cell);
                            map.put(cell.getName(), cell);
                    }
                    in.close();
                    
                    RegionMap nm = new RegionMap(file);
                    nm.addAll(map);
                    ObjectOutputStream out=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(Config.getInstance().base_folder+"/NetworkMapParser")+"/"+file.substring(0,file.length()-4)+".bin")));
                    out.writeObject(nm);
                    out.close();
            }
            
            Logger.logln("Done");
    }
}