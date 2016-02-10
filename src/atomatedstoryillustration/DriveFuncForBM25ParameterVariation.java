/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atomatedstoryillustration;

import java.io.IOException;

/**
 *
 * @author swarnenduchakraborty
 */
public class DriveFuncForBM25ParameterVariation {
    
    static float k ,v;
    float step = 0.05f;
    
    static void fixedKVarV(float k) throws IOException, Exception{
        v = 1.0f;
        while(v <= 3.0){
            System.out.println("k = " + k + " v = " + v);
            Retrive r = new Retrive(k,v);
            r.parseXmlTest();
            //r.queryByrelevancefeedback();
            r.removeDupDocName();
            r.computeMAP();
            v = v + 0.5f;
        }
    }
    
    // LM jelink Search Model
    static void kValueforLMJelink() throws IOException, Exception{
        v = 0.01f;
        while(v <= 3.0){
            System.out.println("k = " + k + " v = " + v);
            Retrive r = new Retrive(k,v);
            r.parseXml();
            r.removeDupDocName();
            r.computeMAP();
            v = v + 0.1f;
        }
    }
    
    
    
    public static void main(String[] args) throws Exception{
        
        //k = 0.05f;
        k = 1.0f;
        
        String KStr = String.valueOf(k);
        // this is for BM 25
        
        while(k<=3.0){
            fixedKVarV(k);
            k = k + 0.5f;
        }
        
        
        
        
    }
}
