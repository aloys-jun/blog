package com.aloys.blog.lucene;

import com.alibaba.fastjson.JSONObject;
import com.aloys.blog.beans.MyDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LuceneUtil {

    private static IndexWriter indexWriter;
    private static IndexWriterConfig indexWriterConfig;
    private static FSDirectory fsDirectory;
    private static Analyzer analyzer;
    private static final String DIR = "/luceneIndex";


    //为文件创建索引
    public static void createIndex(File file,String title){

        String name = file.getName();
        //针对txt文件进行索引创建
        if(name.endsWith(".txt")){
            String content = txtToString(file);
            String id = name.substring(0,name.lastIndexOf("."));

            Document document = new Document();
            document.add(new StringField("id",id, Field.Store.YES));
            document.add(new StringField("title",title, Field.Store.YES));
            document.add(new TextField("content",content, Field.Store.YES));
            try {
                File src = new File("");
                fsDirectory = FSDirectory.open(Paths.get(src.getCanonicalPath() + DIR));
                analyzer = new SmartChineseAnalyzer();
                indexWriterConfig = new IndexWriterConfig(analyzer);
                indexWriter = new IndexWriter(fsDirectory,indexWriterConfig);
                indexWriter.addDocument(document);
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //执行搜索
    public static JSONObject doSearch(String value){
        JSONObject jsonObject = new JSONObject();

        if (value == null || "".equals(value)){
            return jsonObject;
        }

        try {
            File src = new File("");
            fsDirectory = FSDirectory.open(Paths.get(src.getCanonicalPath() + DIR));
            analyzer = new SmartChineseAnalyzer();
            IndexReader reader = DirectoryReader.open(fsDirectory);

            IndexSearcher indexSearcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser("content",analyzer);
            Query query = parser.parse(value);

            //设置高亮
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
            QueryScorer queryScorer = new QueryScorer(query);
            Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter,queryScorer);
            highlighter.setTextFragmenter(fragmenter);

            long start = System.currentTimeMillis();
            TopDocs topDocs = indexSearcher.search(query, 10);
            long end = System.currentTimeMillis();
            System.out.println((end-start) + "毫秒");

            jsonObject.put("total",topDocs.totalHits);
            List<MyDocument> list = new ArrayList<>();
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (ScoreDoc scoreDoc : scoreDocs) {
                int doc = scoreDoc.doc;
                Document doc1 = indexSearcher.doc(doc);
                MyDocument myDocument = new MyDocument();
                myDocument.setId(doc1.get("id"));
                myDocument.setTitle(doc1.get("title"));
                String content = doc1.get("content");
                if (content != null){
                    TokenStream tokenStream = analyzer.tokenStream("content",new StringReader(content));
                    String bestFragment = highlighter.getBestFragment(tokenStream, content);
                    content = bestFragment;
                }
                myDocument.setContent(content);
                list.add(myDocument);
            }
            jsonObject.put("doc",list);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //txt文件内容转换成字符串
    private static String txtToString(File file){
        StringBuffer buffer = new StringBuffer();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                buffer.append(s + "\n");
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
