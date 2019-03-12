package com.howtodoinjava.demo.lucene.file;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
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
import org.apache.lucene.analysis.PorterStemmer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import com.howtodoinjava.demo.lucene.file.LuceneReadIndexFromFileExample.indexList;
public class Index{
	/////////////////////////////////////////////////////////////////////////////////////
	static int id=1;
	static indexList prevsyn;
	static indexList headsyn;
	static class indexList{
		String name;
		indexList next;
		indexList syn;
	}
	static class ScoreList{
		String fileName;
		float fileScore;
		ScoreList next;
	}
	public static ScoreList insertDoc(String docName,ScoreList head,float docScore,int code) {
		if(head==null) {
			ScoreList p = new ScoreList();
			p.fileName=docName;
			p.fileScore=code*docScore;
			p.next=null;
			head=p;
		}
		else {
			ScoreList p=head;
			ScoreList prev=null;
				while(p!=null) {
					if((p.fileName).compareTo(docName)<0) {
						prev=p;
						p=p.next;
					}
					else if((p.fileName).compareTo(docName)==0) {
						p.fileScore = p.fileScore+code*docScore;
						break;
					}
					else {
						if(p==head) {
							ScoreList temp = new ScoreList();
							temp.fileName = docName;
							temp.fileScore=docScore;
							temp.next=head;
							head=temp;
						}
						else {
							ScoreList temp = new ScoreList();
							temp.fileName = docName;
							temp.fileScore=docScore;
							prev.next=temp;
							temp.next=p;
						}
						break;
					}
				}
				if(p==null) {
					ScoreList temp = new ScoreList();
					temp.fileName = docName;
					temp.fileScore=docScore;
					prev.next=temp;
					temp.next=null;
				}
		}
		return head;
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
			if((p.name).compareTo(word)<0){
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
	public static String searchNames(indexList head,String name){
		indexList p=head;
		String newName="";
		while(p!=null){
			if((p.name).compareTo(name)==0)
				break;
			p=p.next;
		}
		if(p!=null) {
			String ref=p.name;
			p=p.syn;
			if(p!=null) {
				while((p.name).compareTo(ref)!=0){
					newName = newName + " " + p.name;
					p=p.syn;
					if(p==null)
						break;
				}
			}
			System.out.println("\n\n");
			return newName;
		}
		else {
			newName="$";
			return newName;
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
				head = insert(head, new_node, a.toLowerCase(), id1, id2);
			}
		}
		return head;
	}
	/////////////////////////////////////////////////////////////////////////////////////
	//directory contains the lucene indexes
	private static final String INDEX_DIR = "indexedFiles";

	private final Font BIGGER_FONT = new Font("monspaced",Font.PLAIN, 20);
	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Index window = new Index();
					window.frame.setVisible(true);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public Index() throws IOException {
		initialize();
	}
	private void initialize() throws IOException{
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		ActionListener operatorListener = new OperatorListener();
		textField = new JTextField();
		textField.setBounds(94, 100, 256, 26);
		panel.add(textField);
		JButton button = new JButton("search");
		button.setFont(BIGGER_FONT);
		button.setBounds(170, 145, 100, 27);
        panel.add(button);
        
        JLabel lblMedigle = new JLabel("Medigle");
        lblMedigle.setFont(new Font("Trebuchet MS", Font.PLAIN, 32));
        lblMedigle.setBounds(157, 39, 167, 36);
        panel.add(lblMedigle);
        button.addActionListener(operatorListener);
		}
		class OperatorListener implements ActionListener {
			public void actionPerformed(ActionEvent e){
				String displayText1 = textField.getText();
				int g;
				String[] topDocuments = new String[10];
			    float[] topScores=new float[10];
				//Create lucene searcher. It search over a single IndexReader.
				if(displayText1!="") {
			        IndexSearcher searcher = null;
					textField.setText("");
					//Creating index for synonyms
			        indexList head=null;
			        File file = new File("synonyms.txt");
			        BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} 
			        String name;
			        try {
						while ((name = br.readLine()) != null) 
						    head=stringSplit(name,head);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			        //searching for the query in index to get all synonyms
			        indexList p=head;
			        int count1=0;
			        String displayText="";
			        int flagVar=0;
			        displayText1 = displayText1.replaceAll("[^\\w]", "|");
			        for(char ch1 : displayText1.toCharArray()){
						if(ch1=='|' && flagVar==0) {
							displayText+=' ';
							flagVar=1;
						}
						else if(ch1!='|'){
							flagVar=0;
							displayText=displayText+ch1;
						}
					}
			        for(char ch1 : displayText.toCharArray()){
						if(ch1==' ')
							count1++;
					}
					String[] queries = displayText.split(" ", count1+1);
					String finalQuery="";
			        for(g=0;g<queries.length;g++){
			        	String k = searchNames(head,queries[g]);
			        	if((k).compareTo("$")==0)
			        		finalQuery = finalQuery + " " + queries[g];
			        	else if(g==0 && (k).compareTo("$")!=0)
			        		finalQuery = finalQuery + queries[g]+k;
			        	else if((k).compareTo("$")!=0)
			        		finalQuery = finalQuery + " " +queries[g]+k;
			        }
			        count1=0;
			        for(char ch1 : finalQuery.toCharArray()){
						if(ch1==' ')
							count1++;
					}
					String[] queries1=queries;
					queries = finalQuery.split(" ", count1);
					ScoreList shead=null;
					int flag=0,flag1=0,ind=0;
					for(int l=0;l<queries.length;l++) {
				        try {
				        	searcher = createSearcher();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					       //Search indexed contents using search term
					    TopDocs foundDocs = null;
					    TopDocs foundDocs1 = null;
						try {
							flag=0;
							flag1=0;
					        //searching for each word in the query in the documents
							if(queries[l].length()>0) {
								foundDocs = searchInContent(queries[l], searcher);
							}
							else
								flag=1;
							if(ind>0 && queries1[ind-1].length()>0 && ind<queries1.length) {
								//searching for 2-shingles
								foundDocs1 = searchInContent(queries1[ind-1]+" "+queries1[ind], searcher);
								flag1=1;
								ind++;
							}
							else if(ind==0) {
								ind++;
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							}
						//inserting the retrieved documents into list
						if(flag==0) {
						    //System.out.println("Total Results :: " + foundDocs.totalHits);
						    //if(flag1==1)
						    	//System.out.println("Total Results :: " + foundDocs1.totalHits);
						    for (ScoreDoc sd : foundDocs.scoreDocs)
						    {
						        Document d = null;
						        try {
									d = searcher.doc(sd.doc);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
						        shead = insertDoc(d.get("path"),shead,sd.score,1);
						        //System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
						    }
						    if(flag1==1) {
						    	//System.out.println("------------------------------------");
						    	for (ScoreDoc sd : foundDocs1.scoreDocs)
							    {
							        Document d = null;
							        try {
										d = searcher.doc(sd.doc);
									} catch (IOException e3) {
										e3.printStackTrace();
									}
							        shead = insertDoc(d.get("path"),shead,sd.score,2);
							        //System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
							    }
						    }
						    ScoreList q=shead;
						    if(q==null)
						    	System.out.println("Unable to find the described symptoms");
						    else if(q!=null) {
							    for(g=0;g<10;g++) {
							    	topScores[g]=q.fileScore;
							    	topDocuments[g]=q.fileName;
							    	q=q.next;
							    	if(q==null)
							    		break;
							    }
							    for(int g1=0;g1<g;g1++) {
							    	for(int k=g1+1;k<g-1;k++) {
							    		if(topScores[g1]<topScores[k]) {
							    			float temp=topScores[g1];
							    			String temp1=topDocuments[g1];
							    			topScores[g1]=topScores[k];
							    			topDocuments[g1]=topDocuments[k];
							    			topScores[k]=temp;
							    			topDocuments[k]=temp1;
							    		}
							    	}
							    }
							    /*for(int k=0;k<g;k++) {
							    	System.out.println(topDocuments[k]+" "+topScores[k]);
							    }*/
							    while(q!=null) {
							    	int g1=0;
							    	while(g1<g) {
							    		if(topScores[g1]>q.fileScore) {
							    			g1++;
							    		}
							    		else {
							    			topScores[g1]=q.fileScore;
							    			topDocuments[g1]=q.fileName;
							    			break;
							    		}
							    	}
							    	q=q.next;
							    }
							    q=shead;
							    while(q!=null) {
							    	//System.out.println(q.fileName);
							    	//System.out.println(q.fileScore);
							    	q=q.next;
							    }
						    }
						}
					}
					System.out.println("Your query : "+displayText);
				    System.out.println("**********************************");
				    System.out.println("Top results :");
				    int fl=0;
					if(g==0)
				    	System.out.println("Unable to find the described symptoms");
				    else {
					    for(int g1=0;g1<g;g1++) {
						    	if(topDocuments[g1]!=null) {
						    		fl=1;
						    	System.out.println(topDocuments[g1]+" "+topScores[g1]);
						    	try {
									BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(topDocuments[g1]),"UTF-8"));
									String output = in.readLine();
									for(int i=0;i<output.length();i++){
									    char ch = output.charAt(i);
									    if (Character.isLetter(ch) || ch == ' ' || ch=='(' || ch==')' || ch=='-') {
									      System.out.print(ch);
									    }
									}
									System.out.println("| Document Details : ("+topScores[g1]+" "+topDocuments[g1]+")");
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
					    }
					    }
					    if(fl==0)
					    	System.out.println("Unable to find the described symptoms");
				    }
				}
			}

			private TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception
		    {
				QueryParser qp = new QueryParser("contents", new EnglishAnalyzer());
		        Query query = qp.parse(textToFind);
		         
		        //search the index
		        TopDocs hits = searcher.search(query, 50);
		        return hits;
		    }
		    private IndexSearcher createSearcher() throws IOException
		    {
		        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		         
		        //It is an interface for accessing a point-in-time view of a lucene index
		        IndexReader reader = DirectoryReader.open(dir);
		         
		        //Index searcher
		        IndexSearcher searcher = new IndexSearcher(reader);
		        return searcher;
		    }
		}
}