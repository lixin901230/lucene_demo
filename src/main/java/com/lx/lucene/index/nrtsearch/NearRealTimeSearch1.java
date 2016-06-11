package com.lx.lucene.index.nrtsearch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.lx.util.FileUtils;

/**
 * 请参考完整版近实时搜索索引管理类：{@link NRTSearchManager}
 * 
 * nrt（Near Real Time）
 * 近实时搜索测试
 * 
 */
public class NearRealTimeSearch1 {
	
	private Directory directory = null;
    IndexWriter writer = null;
	
	/**nrt init**/
    private TrackingIndexWriter trackingIndexWriter = null;
    private ReferenceManager<IndexSearcher> reMgr = null;//类似于Lucene3.x中的NrtManager
    private ControlledRealTimeReopenThread<IndexSearcher> crt = null;
    
    private String[] ids = {"1","2","3","4","5","6"};
    private String[] emails = {"aa@itat.org","bb@itat.org","cc@cc.org","dd@sina.org","ee@zttc.edu","ff@itat.org"};
    private String[] contents = {
            "welcome to visited the space,I like book",
            "hello boy, I like pingpeng ball",
            "my name is cc I like game",
            "I like football",
            "I like football and I like basketball too",
            "I like movie and swim"
    };
    private Date[] dates = null;
    private int[] attachs = {2,3,1,4,5,5};
    private String[] names = {"zhangsan","lisi","john","jetty","mike","jake"};
    
    //数据初始化
    public NearRealTimeSearch1(){
        
        try{
            directory = FSDirectory.open(Paths.get(getIndexDirPath()));
            writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
            
            trackingIndexWriter = new TrackingIndexWriter(writer);
            reMgr = new SearcherManager(writer, true, new SearcherFactory());
            /**在0.025s~5.0s之间重启一次线程，这个是时间的最佳实践**/
            crt = new ControlledRealTimeReopenThread<>(trackingIndexWriter, reMgr, 5.0,0.025);
            crt.setDaemon(true);//设置为后台服务
            crt.setName("Index update to disk");//线程名称
            crt.start();//线程启动
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * 定期提交内存中得索引到硬盘上，防止丢失
     */
    public void commit(){
        try {
            writer.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 建立索引
     * 要实现search nrt,需要使用TrackIndexWriter保存document，同时Writer也不需要关闭。
     **/
    public void index(boolean isNew) {
        if(isNew){
            try {
                Document doc = null;
                for(int i=0;i<ids.length;i++) {
                    doc = new Document();
                    doc.add(new StringField("id",ids[i],Store.YES));
                    doc.add(new StringField("email", emails[i],Store.YES));
                    doc.add(new TextField("content", contents[i], Store.NO));
                    doc.add(new StringField("name",names[i], Store.YES));
                    //存储数字
                    doc.add(new IntField("attach",attachs[i], Store.YES));
                    //存储日期
                    doc.add(new LongField("date", dates[i].getTime(), Store.YES));
                    //使用trackingIndexWriter创建document
                    trackingIndexWriter.addDocument(doc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                commit();//首次创建，提交索引,只有提交后，才会在索引片段中也将信息改变
            }    
        }
    }
    
    /***查询**/
    public void query() {
        IndexSearcher is = getSearcher();
        
        try {
            //通过reader可以有效的获取到文档的数量
            System.out.println("numDocs:"+is.getIndexReader().numDocs());
            System.out.println("maxDocs:"+is.getIndexReader().maxDoc());
            System.out.println("deleteDocs:"+is.getIndexReader().numDeletedDocs());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                reMgr.release(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**删除
     * 使用trackIndexWriter进行数据删除，也不需要关闭Writer
     * **/
    public void delete(){
        try{
            trackingIndexWriter.deleteDocuments(new Term("id","2"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    /**修改
     * 使用trackIndexWriter进行修改，不需要关闭writer
     * **/
    public void update(){
        try{
            Document doc = new Document();
            /*
             * Lucene并没有提供更新，这里的更新操作其实是如下两个操作的合集
             * 先删除之后再添加
             */
            doc.add(new StringField("id", "21", Store.YES));
            doc.add(new TextField("email", "aa.bb@s", Store.YES));
            doc.add(new TextField("content", "update content like", Store.NO));
            doc.add(new StringField("name", "jackson", Store.YES));
            trackingIndexWriter.updateDocument(new Term("id","1"), doc);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**使用单例获取IndexSearch **/
    public IndexSearcher getSearcher() {
        IndexSearcher is = null;
        try {
            if(is==null) {
                reMgr.maybeRefresh();//刷新reMgr,获取最新的IndexSearcher
                is = reMgr.acquire();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(is == null){
            throw new RuntimeException("indexSearcher is null!!!!");
        }
        return is;
        
    }
    
    /** 查询 
     * 
     * 查询时search如果使用完成，需要将search释放会searchFactory中，使用reMgr。release(indexSearcher)进行释放
     * **/
    public void search(){
        IndexSearcher is = getSearcher();
        try {
            TermQuery query = new TermQuery(new Term("content","like"));
            TopDocs tds = is.search(query, 10);
            for(ScoreDoc sd:tds.scoreDocs){
                Document doc = is.doc(sd.doc);
                System.out.println(doc.get("id")+"---->"+
                        doc.get("name")+"["+doc.get("email")+"]-->"+doc.get("id")+","+
                        doc.get("attach")+","+doc.get("date")+"," +doc.getValues("email")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                reMgr.release(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
    /**
     * 关闭初始化线程的处理
     */
    public void close(){
        //stop the re-open thread
        crt.interrupt();
        crt.close();
        //close the indexWriter,commit 所有有过修改的内容
        try {
            writer.commit();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
	 * 获取索引存储路径
	 * @return
	 */
	public static String getIndexDirPath() {
		String webappPath = FileUtils.getWebappPath();
		webappPath = webappPath.endsWith("/") ? webappPath : webappPath + "/";
		String indexDirPath = webappPath + "luceneData/luceneIndex";	//索引存放路径
		return indexDirPath;	//索引存放路径;
	}
}
