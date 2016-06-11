package com.lx.lucene.index.nrtsearch;

import org.junit.Before;
import org.junit.Test;

/**
 * 请参考完整版近实时搜索索引管理类：{@link NRTSearchManager}
 */
public class NearRealTimeSearch1Test {
	
    private static NearRealTimeSearch1 ns =null;
    @Before
    public void init(){
        ns = new NearRealTimeSearch1();
    }
    
    @Test
    public void testIndex(){
        ns.index(true);
    }
    
    @Test
    public void testSearch(){
        ns.query();
        for(int i=0;i<10;i++){
            ns.search();
            System.out.println(i+"--------------------");
            ns.delete();
            if(i==3){
                ns.update();
                ns.query();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ns.commit();
        ns.query();
    }
    
    @Test
    public void close(){
        ns.close();
    }
    
    @Test
    public void testQuery(){
        ns.query();
    }
}
