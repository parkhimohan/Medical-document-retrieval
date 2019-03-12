package com.howtodoinjava.demo.lucene.file;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.AbstractAction;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JButton;

public class LuceneReadIndexFromFileExample
{
	
	/////////////////////////////////////////////////////////////////////////////////////
	static int id=1;
    static indexList prevsyn;
    static indexList headsyn;
    static class indexList{
        String name;
        indexList next;
        indexList syn;
    }
    public static indexList insert(indexList head,indexList new_node,String word, int id1, int id2){
        new_node.name=word;
        new_node.next=null;
        new_node.syn=null;
        if(id==id1)
            headsyn=new_node;
        else if(id<id2-1)
            prevsyn.syn=new_node;
        else{
            new_node.syn=headsyn;
            prevsyn.syn=new_node;
        }
        id++;
        if(head==null){
            head=new_node;
        }
        else{
            indexList p=head,prev=head;
            while(p!=null){
                if((p.name).compareTo(word)<=0){
                    prev=p;
                    p=p.next;
                }
                else
                    break;
            }
            if(p==head){
                head=new_node;
                head.next=p;
            }
            else{
                new_node.next=prev.next;
                prev.next=new_node;
            }
        }
        prevsyn=new_node;
        return head;
    }
    public static void searchNames(indexList head,String name){
        indexList p=head;
        while(p!=null){
            if((p.name).compareTo(name)==0)
                break;
            p=p.next;
        }
        String ref=p.name;
        p=p.syn;
        while((p.name).compareTo(ref)!=0){
            System.out.print(p.name+" ");
            p=p.syn;
        }
    }     
    public static boolean check(indexList head,String a){
        indexList p=head;
        while(p!=null){
            if((p.name).compareTo(a)==0)
                return true;
            p=p.next;
        }
        return false;
    }
    public static indexList stringSplit(String name,indexList head){
        int l=name.length(),count=1,id1=id,id2;
        for(char ch : name.toCharArray()){
            if(ch==';')
                count++;
        }
        id2=id1+count;
        String[] names = name.split(";", count);
        for (String a : names){
            indexList new_node = new indexList();
            if(!check(head,a)){
                head = insert(head, new_node, a, id1, id2);
            }
        }
        return head;
    }
	/////////////////////////////////////////////////////////////////////////////////////
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 
    public static void main(String[] args) throws Exception
    {
        //Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
        //Search indexed contents using search term
        TopDocs foundDocs = searchInContent("puke off balance", searcher);
        //Total found documents
        System.out.println("Total Results :: " + foundDocs.totalHits);
         
        //Let's print out the path of files which have searched term
        for (ScoreDoc sd : foundDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
        }
        ///////////////////////////////////////////////////////////////////////////
        /*indexList head=null;
        File file = new File("C:\\Users\\balamuralikrishna\\Desktop\\trial-lucene\\read.txt");
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        String name;
        while ((name = br.readLine()) != null) 
            head=stringSplit(name,head);
        indexList p=head;
        while(p!=null){
            System.out.println(p.name);
            searchNames(head,p.name);
            System.out.print("\n\n");
            p=p.next;
        }*/
        ///////////////////////////////////////////////////////////////////////////
        
    }
     
    private static TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 10);
        return hits;
    }
 
    private static IndexSearcher createSearcher() throws IOException
    {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
}