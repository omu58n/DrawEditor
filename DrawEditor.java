import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URI;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
 

// 描画した図形を記録する Figure クラス (継承して利用する),シリアライズ化する。
class Figure implements Serializable{
  protected int x,y,width,height;
  protected Color color;
  public Figure(int x,int y,int w,int h,Color c) {
    this.x = x; this.y = y;  
    width = w; height = h;   
    color = c;               
  }
  public void setSize(int w,int h) {
    width = w; height = h;
  }
  public void setLocation(int x,int y) {
    this.x = x; this.y = y;
  }
  public void reshape(int x1,int y1,int x2,int y2) {
    int newx = Math.min(x1,x2);
    int newy = Math.min(y1,y2);
    int neww = Math.abs(x1-x2);
    int newh = Math.abs(y1-y2);
    setLocation(newx,newy);
    setSize(neww,newh);
  }
  public void draw(Graphics g) {}
}
 
class RectangleFigure extends Figure {	//四角形を描画するクラス
  public RectangleFigure(int x,int y,int w,int h,Color c) {
    super(x,y,w,h,c);
  }
  public void draw(Graphics g) {
    g.setColor(color);
    g.drawRect(x,y,width,height);
  }
}
class FillRectangleFigure extends Figure{	//塗りつぶし四角形
	public FillRectangleFigure(int x,int y,int w,int h,Color c) {
		super(x,y,w,h,c);
	}
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}
}
class OvalFigure extends Figure {	//楕円
	  public OvalFigure(int x,int y,int w,int h,Color c) {
	    super(x,y,w,h,c);
	  }
	  public void draw(Graphics g) {
	    g.setColor(color);
	    g.drawOval(x,y,width,height);
	  }
}
class FillOvalFigure extends Figure {	//塗りつぶし楕円
	  public FillOvalFigure(int x,int y,int w,int h,Color c) {
	    super(x,y,w,h,c);
	  }
	  public void draw(Graphics g) {
	    g.setColor(color);
	    g.fillOval(x,y,width,height);
	  }
}
class LineFigure extends Figure {	//直線
	public LineFigure(int x,int y,int w,int h,Color c) {
		super(x,y,w,h,c);
	}	
	public void reshape(int x1,int y1,int x2,int y2) {
		setLocation(x1,y1);
		setSize(x2,y2);
	}
	public void draw(Graphics g) {
		g.setColor(color);
	    g.drawLine(x,y,width,height);
	}
}
class PenFigure extends Figure{		//自由曲線(丸の連続描画)
	public PenFigure(int x,int y,int w,int h,Color c) {
		super(x,y,w,h,c);
	}
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillOval(x,y,width,height);
	}
}
////////////////////////////////////////////////
// Model (M)
 
// modelは java.util.Observableを継承する．Viewに監視される．
class DrawModel extends Observable {
  protected ArrayList<ArrayList<Figure>> figs;
  protected ArrayList<ArrayList<Figure>> redo;
  protected Figure drawingFigure;
  protected Figure f;
  protected int currentFigure;		//描画する図形
  protected Color currentColor;
  protected int radius;				//自由曲線の太さ
  public DrawModel() {
    figs = new ArrayList<ArrayList<Figure>>();
    redo = new ArrayList<ArrayList<Figure>>();
    drawingFigure = null;  
    currentColor = Color.black;	//色の初期値は黒
    currentFigure=1;			//図形の初期設定は四角形
    radius=5;					//自由曲線の太さの初期設定は5
  }
  public void setColorChoose() {	//ColorChooserで選んだ色をcurrentColorにセット
	  currentColor=JColorChooser.showDialog(null, "JColorChooser", Color.WHITE);
  }
  public void setColorBlack() {		//currentColorをblackにセット
	  currentColor=Color.black;
  }
  public void setColorWhite() {		//currentColorをwhiteにセット
	  currentColor=Color.white;
  }
  public void setFigure(int s) {	//描画する図形を入力に応じてセット
	  currentFigure=s;
  }
  public void setRadius(int r) {	//自由曲線の太さを入力に応じてセット
	  radius=r;
  }
  public ArrayList<ArrayList<Figure>> getFigures() {
    return figs;
  }
  public void createFigure(int x,int y) {	//描画するメソッド
	  if(currentFigure==1) {		//四角形
		 f = new RectangleFigure(x,y,0,0,currentColor);
	 }else if(currentFigure==2) {	//塗りつぶし四角形
		 f=new FillRectangleFigure(x,y,0,0,currentColor);
	 }else if(currentFigure==3) {	//楕円
		 f=new OvalFigure(x,y,0,0,currentColor);
	 }else if(currentFigure==4) {	//塗りつぶし楕円
		 f=new FillOvalFigure(x,y,0,0,currentColor);
	 }else if(currentFigure==5) {	//直線
		 f=new LineFigure(x,y,x,y,currentColor);
	 }else if(currentFigure==6) {	//自由曲線
		 f=new PenFigure(x-radius,y-radius,radius*2,radius*2,currentColor);
	 }	 
	 ArrayList<Figure> fig =new ArrayList<Figure>();
	 fig.add(f);
	 drawingFigure = f;
	 figs.add(fig);
	 setChanged();
	 notifyObservers();
  }
  public void PenWrite(int x,int y) {
	  f=new PenFigure(x-radius,y-radius,radius*2,radius*2,currentColor);
	  figs.get(figs.size()-1).add(f);
	  setChanged();
	  notifyObservers();
  }
  public void reshapeFigure(int x1,int y1,int x2,int y2) {
    if (drawingFigure != null) {
      drawingFigure.reshape(x1,y1,x2,y2);
      setChanged();
      notifyObservers();
    }
  }
  public void Clear() {		//DrawFrameを白紙にもどす
	  figs.clear();
	  redo.clear();
	  setChanged();
	  notifyObservers();
  }
  public void ClearRedo() {
	  redo.clear();
  }
  public void Undo() {		//Undo（一つ戻る）
	  ArrayList<Figure> a=figs.get(figs.size()-1);
	  redo.add(a);
	  figs.remove(figs.size()-1);
	  setChanged();
	  notifyObservers();
  }
  public void Redo() {
	  ArrayList<Figure> a=redo.get(redo.size()-1);
	  figs.add(a);
	  redo.remove(redo.size()-1);
	  setChanged();
	  notifyObservers();
  }
  public void setEnabled() {
	  if(figs.isEmpty()==true) {
		  DrawFrame.b1.setEnabled(false);
		  DrawFrame.b3.setEnabled(false);
	  }else {
		  DrawFrame.b1.setEnabled(true);
		  DrawFrame.b3.setEnabled(true);	  
	  }
	  if(redo.isEmpty()==true) {
		  DrawFrame.b2.setEnabled(false);
	  }else {
		  DrawFrame.b2.setEnabled(true);
	  }
  }
  public void SerialOutput() {
	  try {
		  System.out.println("ファイル名を指定してください");
		  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		  String name =reader.readLine();
		  ObjectOutput out=new ObjectOutputStream(new FileOutputStream(name));
		  out.writeObject(figs);
		  out.flush();
		  out.close();
		  reader.close();
		  System.out.println(name+"に保存されました");
	  }catch (FileNotFoundException e) {
		  e.printStackTrace();
	  }catch (IOException e) {
		  e.printStackTrace();
	  }
  }
  public void SerialInput() {
	  try {
		  System.out.println("ファイル名を指定してください");
		  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
          String name = reader.readLine();
          ObjectInputStream in = new ObjectInputStream(new FileInputStream(name));
          figs= (ArrayList<ArrayList<Figure>>)in.readObject();
          System.out.println("ファイルを読み込みました");
          setChanged();
          notifyObservers();
          in.close();
          reader.close();
	  }catch(FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
      }
  }
  public void captureScreen() {
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
        Image img;
		try {
			Robot robot = new Robot();
			img = robot.createScreenCapture(new Rectangle(sz.width/2-488, sz.height/2-412, 976, 824));
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}

		BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

		Graphics g = bimg.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		try{
			File outputfile = new File("image.png");
			ImageIO.write(bimg, "png", outputfile);
		}catch( IOException e ){
		}
	}
}
////////////////////////////////////////////////
// View (V)
 
// Viewは，Observerをimplementsする．Modelを監視して，
// モデルが更新されたupdateする．実際には，Modelから
// update が呼び出される．
class ViewPanel extends JPanel implements Observer {
  protected DrawModel model;
  
  public ViewPanel(DrawModel m,DrawController c) {
    this.setBackground(Color.white);
    this.addMouseListener(c);
    this.addMouseMotionListener(c);
    model = m;
    model.addObserver(this);
  }
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    ArrayList<ArrayList<Figure>> figs = model.getFigures();
    for(int i = 0; i < figs.size(); i++) {
    	ArrayList<Figure> fig = figs.get(i);
    	for(int j=0;j<fig.size();j++) {
    		Figure f =fig.get(j);
    		f.draw(g);
    	}
    }
    
  }
  public void update(Observable o,Object arg){
    repaint();
  }
}
 
//////////////////////////////////////////////////
// main class
//   (GUIを組み立てているので，view の一部と考えてもよい)
class DrawFrame extends JFrame {
  static DrawModel model;
  ViewPanel view;
  DrawController cont;
  static JButton b1,b2,b3,b5;
   public DrawFrame(){
      model=new DrawModel();
      cont =new DrawController(model);
      view=new ViewPanel(model,cont);
      JMenuBar menubar=new JMenuBar();
      JMenu menu1=new JMenu("COLOR");	//色変更のメニュー
      JMenu menu2=new JMenu("FIGURE");	//図形変更
      JMenu menu3=new JMenu("PEN");		//自由曲線
      JMenu menu4=new JMenu("HELP");    //操作説明
      menu1.setFont(new Font("Arial",Font.PLAIN,24));
      menu2.setFont(new Font("Arial",Font.PLAIN,24));
      menu3.setFont(new Font("Arial",Font.PLAIN,24));
      menu4.setFont(new Font("Arial",Font.PLAIN,24));
      JMenuItem menuitem11=new JMenuItem("BLACK");
      JMenuItem menuitem12=new JMenuItem("WHITE");
      JMenuItem menuitem13=new JMenuItem("MORE COLOR");
      JMenuItem menuitem21=new JMenuItem("RECT");
      JMenuItem menuitem22=new JMenuItem("FILRECT");
      JMenuItem menuitem23=new JMenuItem("OVAL");
      JMenuItem menuitem24=new JMenuItem("FILLOVAL");
      JMenuItem menuitem25=new JMenuItem("LINE");
      JMenuItem menuitem31=new JMenuItem("5");
      JMenuItem menuitem32=new JMenuItem("10");
      JMenuItem menuitem33=new JMenuItem("ERASER");
      JMenuItem menuitem41=new JMenuItem("Open manual");
      JMenuItem menuitem42=new JMenuItem("Open Web manual");
      menuitem11.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem12.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem13.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem21.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem22.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem23.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem24.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem25.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem31.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem32.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem33.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem41.setFont(new Font("Arial",Font.PLAIN,22));
      menuitem42.setFont(new Font("Arial",Font.PLAIN,22));
      JPanel p=new JPanel();
      b1=new JButton("UNDO");
      b2=new JButton("REDO");
      b3=new JButton("CLEAR");
      JPanel pp=new JPanel();
      JButton b4=new JButton("SAVE");
      b5=new JButton("OPEN");
      JButton b6=new JButton("ScreenCapture");
      b1.setFont(new Font("Arial",Font.PLAIN,22));
      b2.setFont(new Font("Arial",Font.PLAIN,22));
      b3.setFont(new Font("Arial",Font.PLAIN,22));
      b4.setFont(new Font("Arial",Font.PLAIN,22));
      b5.setFont(new Font("Arial",Font.PLAIN,22));
      b6.setFont(new Font("Arial",Font.PLAIN,22));
      p.setLayout(new GridLayout(1,3));
      p.add(b1); p.add(b2); p.add(b3);
      pp.setLayout(new GridLayout(1,3));
      pp.add(b4); pp.add(b5); pp.add(b6);
      this.add(pp,BorderLayout.SOUTH);
      this.add(p,BorderLayout.NORTH);
      this.setBackground(Color.black);
      this.setTitle("Draw Editor");
      this.setSize(1000,1000);
      this.setLocationRelativeTo(null);
      this.add(view);
      menubar.add(menu1);  menubar.add(menu2);	 menubar.add(menu3);  menubar.add(menu4);
      menu1.add(menuitem11);  menu1.add(menuitem12);  menu1.add(menuitem13);
      menu2.add(menuitem21);  menu2.add(menuitem22);  menu2.add(menuitem23);  menu2.add(menuitem24);  menu2.add(menuitem25);
      menu3.add(menuitem31);  menu3.add(menuitem32);  menu3.add(menuitem33);
      menu4.add(menuitem41);  menu4.add(menuitem42);
      setJMenuBar(menubar);
      b1.setEnabled(false);	b2.setEnabled(false); b3.setEnabled(false);
      menuitem11.addActionListener(event ->{
    	  model.setColorBlack();
      });
      menuitem12.addActionListener(event ->{
    	  model.setColorWhite();
      });
      menuitem13.addActionListener(event ->{
    	  model.setColorChoose();
      });
      menuitem21.addActionListener(event ->{
    	  model.setFigure(1);
      });
      menuitem22.addActionListener(event ->{
    	  model.setFigure(2);
      });
      menuitem23.addActionListener(event ->{
    	  model.setFigure(3);
      });
      menuitem24.addActionListener(event ->{
    	  model.setFigure(4);
      });
      menuitem25.addActionListener(event ->{
    	  model.setFigure(5);
      });
      menuitem31.addActionListener(event ->{
    	  model.setRadius(5);
    	  model.setFigure(6);
      });
      menuitem32.addActionListener(event ->{
    	  model.setRadius(10);
    	  model.setFigure(6);
      });
      menuitem33.addActionListener(event ->{
    	  model.setRadius(15);
    	  model.setFigure(6);
    	  model.setColorWhite();
      });
      menuitem41.addActionListener(event ->{
    	  JFrame Frame=new TextPaneHelp();
      });
      menuitem42.addActionListener(event ->{
    	  String uriString = "https://omu58n.github.io/DrawEditorHelp"; // 開くURL
    	  Desktop desktop = Desktop.getDesktop();
    	  try{
    	    URI uri = new URI( uriString );
    	    desktop.browse( uri );
    	  }catch( Exception e ){
    	    e.printStackTrace();
    	  }
      });
      b1.addActionListener(event ->{
    	  model.Undo();
    	  model.setEnabled();
      });
      b2.addActionListener(event ->{
    	  model.Redo();
    	  model.setEnabled();
      });
      b3.addActionListener(event ->{
    	  JFrame Frame=new CationFrame();
    	  model.setEnabled();
      });
      b4.addActionListener(event ->{
    	  model.SerialOutput();
    	  b4.setEnabled(false);
      });
      b5.addActionListener(event ->{
    	  model.SerialInput();
    	  b5.setEnabled(false);
      });
      b6.addActionListener(event ->{
    	 model.captureScreen();
      });
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setVisible(true);
    }
    public static void main(String argv[]) {
      new DrawFrame();
   }
}
class CationFrame extends JFrame{	//DrawFrameをclearするときに警告を表示する
	JPanel p1;
	JLabel label;
	JButton b1,b2;
	public CationFrame() {
		this.setTitle("Caution");
		label=new JLabel("This operation cannot be undone.",JLabel.CENTER);
	    label.setFont(new Font("Arial",Font.PLAIN,22));
		this.add(label);
		p1=new JPanel();
		p1.setLayout(new GridLayout(1,2));
		b1=new JButton("OK");	b2=new JButton("Cancel");
		b1.setFont(new Font("Arial",Font.PLAIN,22));
		b2.setFont(new Font("Arial",Font.PLAIN,22));
		p1.add(b1);  p1.add(b2);
		this.add(p1,BorderLayout.SOUTH);
		this.setSize(500, 250);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		b1.addActionListener(event ->{	//OKが押されたら、DrawFrameをClearし、CationFrameを消す
			DrawFrame.model.Clear();
			DrawFrame.model.setEnabled();
			this.dispose();
		});
		b2.addActionListener(event ->{	//Cancelが押されたら、何もせずCationFrameを消す
			this.dispose();
		});		
		
	}
}

class TextPaneHelp extends JFrame{

	  TextPaneHelp(){
	    setTitle("HELP");
		this.setSize(700,1000);
		this.setLocationRelativeTo(null);
	    this.setVisible(true);
	    
	    JTextPane textPane = new JTextPane();
	    JScrollPane scroll = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	    getContentPane().add(scroll);

	    StyleContext sc = new StyleContext();
	    DefaultStyledDocument doc = new DefaultStyledDocument(sc);

	    textPane.setDocument(doc);
	    textPane.setEditable(false);

	    initDocument(doc, sc);

	    changeStyle(doc);
	  }

	  protected void initDocument(DefaultStyledDocument doc, StyleContext sc){
	    StringBuffer sb = new StringBuffer();
	    sb.append("操作説明\n");
	    sb.append("\n");
	    sb.append("COLOR\n");
	    sb.append(" 色の変更ができます。黒を使いたいときはBLACK、白を使いたいときはWHITEを押してください。その他の色を使いたいときはMORE COLORを押すとダイアログが開くので、好きな色を選択してください。\n");
	    sb.append("\n");
	    sb.append("FIGURE\n");
	    sb.append(" 描画する図形の変更ができます。四角形枠(RECT)、塗りつぶし四角形(FILLRECT)、楕円枠(OVAL)、塗りつぶし楕円(FILLOVAL)、直線(LINE)から描画したい図形を選んで押してください。\n");
	    sb.append("\n");
	    sb.append("PEN\n");
	    sb.append(" マウスのドラッグで自由曲線が描画できます。太さを5か10から選んでください。ERASERを押すと消しゴム機能が使用できます。\n");
	    sb.append("\n");
	    sb.append("UNDO\n");
	    sb.append(" 直前の描画を取り消せます（一つ戻れます）。押した回数だけ戻ります。\n");
	    sb.append("\n");
	    sb.append("REDO\n");
	    sb.append(" UNDOボタンで戻った操作を取り消せます。UNDOボタンの操作回数分だけ取り消すことができます。\n");
	    sb.append("\n");
	    sb.append("CLEAR\n");
	    sb.append(" 描画したものを全て消し、白紙に戻すことができます。この操作は取り消せません（REDOボタンで取り消せません）。CLEARボタンを押すと、確認画面が出るので、画面をクリアしてもよければOKを、ダメならCANCELを押してください。\n");
	    sb.append("\n");
	    sb.append("SAVE\n");
	    sb.append(" 描画したものを保存できます。コンソールでファイル名の入力を促されるので、好きな名前を入力してください。保存は起動後一度だけ行えます。\n");
	    sb.append("\n");
	    sb.append("OPEN\n");
	    sb.append(" 過去に保存したものを読み込んで、続きを描くことができます。コンソールでファイル名の入力を促されるので、読み込みたいものの名前を入力してください。読み込みは起動時にのみ可能です。\n");
	    sb.append("\n");
	    sb.append("\n");
	    sb.append("閉じるボタンで操作説明を閉じます\n");

	    try{
	      /* 文書を挿入する */
	      doc.insertString(0, new String(sb), sc.getStyle(StyleContext.DEFAULT_STYLE));
	    }catch (BadLocationException ble){
	      System.err.println("初期文書の読み込みに失敗しました。");
	    }
	  }

	  protected void changeStyle(DefaultStyledDocument doc){
	    MutableAttributeSet attr1 = new SimpleAttributeSet();
	    StyleConstants.setBold(attr1, true);
	    StyleConstants.setFontSize(attr1, 30);
	    doc.setCharacterAttributes(0, 4, attr1, false);

	    MutableAttributeSet attr2 = new SimpleAttributeSet();
	    StyleConstants.setItalic(attr2, true);
	    StyleConstants.setFontSize(attr2, 24);
	    doc.setCharacterAttributes(4, 7, attr2, false);//color
	    doc.setCharacterAttributes(113, 8, attr2, false);//figure
	    doc.setCharacterAttributes(225, 7, attr2, false);//pen
	    doc.setCharacterAttributes(296, 5, attr2, false);//undo
	    doc.setCharacterAttributes(335, 5, attr2, false);//redo
	    doc.setCharacterAttributes(393, 6, attr2, false);//clear
	    doc.setCharacterAttributes(516, 6, attr2, false);//save
	    doc.setCharacterAttributes(590, 6, attr2, false);//open
	    
	    MutableAttributeSet attr3 = new SimpleAttributeSet();
	    StyleConstants.setFontSize(attr3, 22);
	    doc.setCharacterAttributes(11, 102, attr3, false);//color
	    doc.setCharacterAttributes(121, 103, attr3, false);//figure
	    doc.setCharacterAttributes(230, 66, attr3, false);//pen
	    doc.setCharacterAttributes(301, 34, attr3, false);//undo
	    doc.setCharacterAttributes(340, 53, attr3, false);//redo
	    doc.setCharacterAttributes(399, 117, attr3, false);//clear
	    doc.setCharacterAttributes(522, 68, attr3, false);//save
	    doc.setCharacterAttributes(596, 88, attr3, false);//open
	    
	    MutableAttributeSet attr４ = new SimpleAttributeSet();
	    StyleConstants.setFontSize(attr４, 16);
	    doc.setCharacterAttributes(684, 19, attr４, false);
	  }
	}
////////////////////////////////////////////////
// Controller (C)
 
class DrawController implements MouseListener,MouseMotionListener {
  protected DrawModel model;
  protected int dragStartX,dragStartY;
  public DrawController(DrawModel a) {
    model = a;
  }
  public void mouseClicked(MouseEvent e) { }
  public void mousePressed(MouseEvent e) {
	DrawFrame.b5.setEnabled(false);
    dragStartX = e.getX(); dragStartY = e.getY();
    model.createFigure(dragStartX,dragStartY);
    model.ClearRedo();
    model.setEnabled();
  }
  public void mouseDragged(MouseEvent e) {
	  if(model.currentFigure==6) {	//自由曲線の場合、円を繰り返し描画する
		  model.PenWrite(e.getX(),e.getY());
	  }else{
		  model.reshapeFigure(dragStartX,dragStartY,e.getX(),e.getY());
	  }
  }
  public void mouseReleased(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }
}
