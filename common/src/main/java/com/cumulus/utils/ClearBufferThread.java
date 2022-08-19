package com.cumulus.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * 清理输入流缓存的线程
 * @author zhutaotao
 * @date 2020-04-23
 */
@Slf4j
public class ClearBufferThread implements Runnable {
    private InputStream inputStream;
    private String type;
 
    public ClearBufferThread(String type, InputStream inputStream){
        this.inputStream = inputStream;
        this.type = type;
    }
 
    public void run() {
    	BufferedReader br = null;
    	String line;
        try{
        	br = new BufferedReader(new InputStreamReader(inputStream));
            while((line = br.readLine()) != null) {
            	log.info(type+"===>"+line);
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }finally {
        	if(br != null) {
        		try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
        	}
        	if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
        }
    }
}
