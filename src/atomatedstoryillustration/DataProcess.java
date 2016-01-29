/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import static atomatedstoryillustration.Retrive.fw;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author swarnenduchakraborty
 */
public class DataProcess {
    public static void main(String[] args) throws FileNotFoundException, IOException{
        File f = new File("/Users/swarnenduchakraborty/study/dissertation/result.txt");
        FileWriter fw =new FileWriter(new File("/Users/swarnenduchakraborty/study/dissertation/result_1.txt"));
        
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line= null;
        ArrayList<String> list = new ArrayList<String>();
        while((line =br.readLine()) != null){
            String[] parts = line.split(" ");
            if(parts.length !=6)
                break;
            String imageid = parts[2];
            if(list.contains(imageid)){
                
                continue;
            }
            else{
                list.add(imageid);
                fw.write(parts[0]);
                fw.write(" ");
            // Query Second Id
            //String secondField = "Q" + temp;
                fw.write(parts[1]);
                fw.write(" ");
            // Document Retrieved
                fw.write(parts[2]);
                fw.write(" ");
            // rank
                fw.write(parts[3]);
                fw.write(" ");
                
                fw.write(parts[4]);
                fw.write(" ");
                
                fw.write(parts[5]);
                fw.write("\n");
                
                
            }
            
        }
        
        fw.close();
        
    }
}
