/*
 * Helen Zhang
 * SpaceInvaders.java
 *
 * This is the class in which the main game and all of its functions are performed. Called by SpaceMenu
 */


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.*; 													// lots of fun imports
import java.io.*; 
import javax.imageio.*; 
public class SpaceInvaders extends JFrame implements ActionListener{
	
	Timer myTimer;
	GamePanel game;
	public static SpaceMenu menu;											// the game starts from SpaceMenu - aka run that one
	JButton play;	
	
	// basic setup
    public SpaceInvaders(){
    	super("Space Invaders");
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setSize(1170,800);
    	myTimer = new Timer(10, this);	
    	game=new GamePanel(this);
    	add(game,"Center");
    	
    	setResizable(false);    	
    	setVisible(true);									// setVisible starts out true because the game should show up as soon as "PLAY" is selected
    }      
    
    public void start(){
		myTimer.start();
	}
    
    // actionPerformed calls a bunch of methods that do things in the game. To be explained later.
    public void actionPerformed(ActionEvent evt){
    	Object source = evt.getSource();
		game.move();
		game.shoot();
		game.enemyshoot();
		game.repaint();
		
		if(source==play){													// this is a different play button from the menu one. This one shows up once the
			setVisible(true);												// player has either won or lost and takes them back to the menu
			menu.setVisible(false);
		}
	}    
}

// GamePanel! Aka where everything in the game happens.
class GamePanel extends JPanel implements KeyListener,ActionListener{
	private JButton play;
	private JLabel scoreDisp;
	private boolean []keys;
	private boolean shipHit=false,special=false;;
	private Image ship,lives3,lives2,enemyBoss;
	private Image title,gameOver,score,endGame;
	private BufferedImage bases=null;										//tons of variables yay
	private int x,y,sx,sy,esx,esy=1000,eny,lives=3;
	private int mwait=0,eswait=0,points=0,c,specialRand,spcx;
	private SpaceInvaders mainFrame;
	private ArrayList<Enemy> enemies=new ArrayList<Enemy>();
	private int move=7,waitTime=30;
	private Random choose=new Random();	    	
	
	// checking for the play button after the game is over, as mentioned above
	public void actionPerformed(ActionEvent evt){
    	Object source = evt.getSource();
		if(source==play){
			SpaceInvaders.menu=new SpaceMenu(0);
			setVisible(false);
		}
	}
	
	// constructor
	public GamePanel(SpaceInvaders m){
		keys = new boolean[KeyEvent.KEY_LAST+1];
		ship = new ImageIcon("ship.png").getImage();
		enemyBoss = new ImageIcon("enemyBoss.png").getImage();
		lives3 = new ImageIcon("3lives.png").getImage();
		lives2 = new ImageIcon("2lives.png").getImage();			
		title = new ImageIcon("title.png").getImage();
		score = new ImageIcon("score.png").getImage();				// Images and more images. Bases (which is a picture of all four bases in a row instead of
		try{														// just one) is a buffered image because getRGB and setRGB are needed
    		bases= ImageIO.read(new File("bases.png"));
		} 
		catch (IOException e) {
		}
		
		mainFrame = m;
		
		for(int x = 0; x < 12; x++){								// loops and adds enemies to an ArrayList that will be referenced later for drawing etc.
			for(int y = 0; y < 5; y++){								// minimal math for spacing and visuals
				enemies.add(new Enemy(x*60+15,y*50+120,y));
			}
		}
		
		x=167;										// starting position. Safely tucked behind a base.
		y=700;										// the x will change when moved, but the y never will
		setSize(1170,600);
		addKeyListener(this);
		
		scoreDisp=new JLabel();						// used a JLabel for the score, perhaps not my most brilliant idea?
		scoreDisp.setForeground(Color.green);		// either way, it works. Set some colors, sizes, fonts, etc
		scoreDisp.setSize(100,30);
	    scoreDisp.setLocation(710,17);
	    scoreDisp.setFont(new Font("Arial", Font.PLAIN, 30));
	}
	
	public void addNotify(){
        super.addNotify();
        requestFocus();
        mainFrame.start();
    }
    
    // The method in which everyting happens. At least most things.
	public void move(){
		ArrayList<Enemy> remove=new ArrayList<Enemy>();			// list of enemies to remove to avoid the re-iterate-while-changing error
		setLayout(null);
				
		scoreDisp.setText(""+points);							// score Display JLabel being added to the screen
		add(scoreDisp);
		
		if(keys[KeyEvent.VK_RIGHT] ){							// if the right arrow key is pressed, move right
			x += 5;												// some math to make sure it stays in the screen
			x=Math.min(1170-55,x);
		}
		if(keys[KeyEvent.VK_LEFT]){								// same to go left
			x -= 5;
			x=Math.max(0,x);
		}
		
		sy-=15;													// periodically subtract from the y values of the shot and the enemies' shots so that
		esy+=15;												// they can be drawn and look like they're moving
		mwait+=1;
		boolean oneOfTheDudesHitTheWall = false;				// Pure beauty.
		
		if(waitTime<=0){										// to avoid dividing by 0 error
			waitTime=1;
		}
		if(mwait%waitTime==0){									// wait mod stuff to make the glitchy movement
			for(Enemy en : enemies){							// go through the arraylist of enemies and move them
				en.x+=move;										// move is a variable because it needs to change
				if(en.x<0 || en.x > 1115){
					oneOfTheDudesHitTheWall = true;				// if the coordinate goes off the screen, set off the boolean
				}
			}
		}
		if(oneOfTheDudesHitTheWall){							// ..and send everyone back the way they came
			eny+=20;											// eny is for moving them down every time they hit a wall
			move *= -1;
			waitTime-=5;										// waitTime increases the speed
			c+=1;												// c is used below to see how many times the enemies can move close before they hit your
		}														// bases and you lose
		if(c==10){
			gameOver(1);										// and if you do, call gameOver. The 1 indicates the player lost. 2 is for winning.
		}
		
		for(Enemy en:enemies){
			if(sx>en.x && sx<en.x+55 && sy>en.y && sy<en.y+35){ // if the enemy is hit with a shot...
				if(en.t==1){
					points+=10;
				}												// depending on its type (t), add a different amount of points to the total
				if(en.t==2){
					points+=20;
				}
				if(en.t==3){
					points+=30;
				}
				remove.add(en);									// and then add said enemy to the to-be-removed list
			}
		}
		for(Enemy en:remove){
			enemies.remove(en);									// ..then remove the enemy and reset the shot y value to -1 so it can be fired again
			sy=-1;			
		}
														
		if(shipHit==true){										// On the other hand, if the player ship is hit, subtract 1 from lives
			lives-=1;											// then reset it to false to it can be hit again
			shipHit=false;
		}
		if(lives<=0){
			lives=0;											// to make sure the lives don't go negative, and to call gameOver when there are no lives left
			gameOver(1);
		}
		if(enemies.size()==0){									// if all the enemies are gone, call gameOver for the player's win
			gameOver(2);
		}
		if(esy>550 & esy<630){									// if the enemy shot is in range of the bases image, call drawDamage which checks the spots
			drawDamage(esx,esy-550);
		}
		if(sy<630 & sy>550){									// similarily, if the players shot is in range of the bases image, also check, but don't call
			if(baseCheckHit(sx,sy-550)==true){					// draw damage because the bases are magically immune to the player's shots
				sy=-1;											// and reset the shot so it can be fired again
			}
		}
		
		
		// section for the special (giant red thing in the back that's worth more points)
		if(special==false){
			specialRand=choose.nextInt(1000);					// special starts out as false, so if it is, choose a random number
		}
		if(specialRand==7){										// if that random number is lucky seven, then the special becomes true
			special=true;
		}
		if(special==true){										// and in that case, make sure the special is within the screen as it's moved across rather slowly
			if(spcx<1245){
				spcx=spcx+2;
			}
			if(spcx>1245){										// if the x value is too far and off the screen, the special is done and becomes false
				spcx=2000;
				special=false;
			}
			if(sx>spcx && sx<spcx+75 && sy>50 && sy<85){		// However, if the player manages to hit it before that happens, reset it again
				spcx=2000;										// special becomes false, and a random number of points between 100,200,and 300 is selected
				special=false;
				points+=choose.nextInt(3)*100;
			}
		}		
	}															// phew.
	
	// Shoot, as its name suggests, handles the shooting. Specifically shooting of the player
	public void shoot(){
		if(endGame==null){										// first make sure the game isn't over..
			if(sy<0){											// then if the shot has been reset and space is pressed, start the shot
				if(keys[KeyEvent.VK_SPACE] ){
					sx=x+25;									// x+25 so that it looks like the shot is actually coming out of the cannon, and sy
					sy=700;										// starts at 700 and is added to elsewhere
				}
			}
		}
						
	}
	
	// Similar to shoot, except with a bunch of enemies and therefore more complicated
	public void enemyshoot(){
		eswait+=1;												// enemy shots have a wait for glitchy jerkiness
		if(eswait%30==0){
			if(esy>800 && enemies.size()>0){					// if the shot has been reset and there are still enemies left,
				int chosen=choose.nextInt(enemies.size());		// choose a random enemy from the list to shoot with
				esx=(enemies.get(chosen).x)+15;					// same thing as above
				esy=(enemies.get(chosen).y);
			}
		}		
		if(esx<x+50 && esx>x && esy>y && esy<y+31){				// if their shot hits the player's ship, shipHit becomes true, and the shot is reset
			shipHit=true;
			esy=800;
		}		
	}
	
	// gameOver handles what happens when the game is done - either the player wins or loses
	public void gameOver(int result){
		if(result==1){											// if they lose, they get a game over message
			endGame=new ImageIcon("gameOver.png").getImage();
		}
		if(result==2){											// and vice versa
			endGame=new ImageIcon("youWin.png").getImage();
		}
		
		play = new JButton("PLAY");								// once the game is over, the player has the option to press they play button, similar
    	play.addActionListener(this);							// to the way it was in SpaceMenu, to go back to said menu and play again
    	play.setSize(100,30);
    	play.setLocation(550,560);
    	add(play);
    	getRootPane().setDefaultButton(play);
    	
	}
	
	public void keyTyped(KeyEvent e){}

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }															// keyListener things
    
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    
    // draws black pixels onto the bases image when it is hit
    public void drawDamage(int sx, int sy){
    	if(baseCheckHit(sx,sy)==true){							// calls baseCheckHit to see if the shot actually hit
    		int r2=0,g2=0,b2=0,c2;								// and if it did, do some weird math with the RGBs
			c2 = r2 << 16 | g2 << 8 | b2;
			for(int i=0;i<30;i++){								// then loop through and put some random dots on the base
				int spotx=choose.nextInt(10);
				int spoty=choose.nextInt(10);
				bases.setRGB(sx+spotx,sy+spoty,c2);
			}			
			esy=800;											// and reset the enemy's shot
    	}		
	}
	
	// called by drawDamage, checks if the enemy's shot comes into contact with any of the bases
	public boolean baseCheckHit(int sx, int sy){
		int red,green,blue,r2,g2,b2,colorCheck,c2=0;			// yay variables for colors and RGB flipping
		
		colorCheck=bases.getRGB(sx,sy);
		
		red = (colorCheck >> 16) & 0xFF;
		green = (colorCheck >> 8) & 0xFF;
		blue = colorCheck & 0xFF;								// math and other things
		
		if(green>blue && blue>=red){							// since the bases are green, if the pixel the shot is at is mostly green, it must be the base
			return true;										// therefore baseCheckHit would be true
		}
		else{
			return false;										// or otherwise, false
		}    
	}
    
    // paint component does all the drawing and not much logic
    @Override
    public void paintComponent(Graphics g){
    	g.setColor(new Color(0,0,0));							// set the color to black and fill the background with it
		g.fillRect(0,0,getWidth(),getHeight());
		
		if(endGame!=null){										// if the game has ended, stop counting the points and display them in a different spot
			final int pointsFin=points;
			g.drawImage(endGame,0,0,this);						// also draw the endgame image, which is win or lose depending on gameOver from somewhere above
			scoreDisp.setLocation(645,615);
			scoreDisp.setText(""+pointsFin);
			
		}
		
		else if(endGame==null){									// if the game is still going, draw...
			g.drawImage(title,0,0,this);						// the title, SPACE INVADERS
			g.drawImage(score,600,20,this);						// the non-changing part of "Score: ###" because cool fonts
			
			g.drawImage(bases,0,550,this);						// the row of bases
			
			for(Enemy i : enemies){								// all the enemies
				g.drawImage(i.type,(i.x),i.y+eny,this);
			}						
			g.drawImage(ship,x,y,this);							// and of course, the player's ship
			
			g.setColor(new Color(0,255,255));					// also the shots. The player shoots cyan and the computer shoots yellow for
			g.drawLine(sx,sy,sx,sy-15);							// no particular reason
			g.setColor(new Color(255,255,0));
			g.drawLine(esx,esy,esx,esy+30);	
				
			if(special==true){									// if the special is true and happening, draw it
				g.drawImage(enemyBoss,spcx,50,this);
			}		
			
			if(lives==3){
				g.drawImage(lives3,950,10,this);
			}
			if(lives==2){										// display the number of lives in the corner
				g.drawImage(lives2,950,10,this);
			}
			if(lives==1){
				g.drawImage(ship,950,10,this);
			}
		}					
	}
}

// holds all the fields of information of whatnot that are needed to organize the enemies
class Enemy{
	public int x,y,t;
	public Image type;
	
	// constructor and only method. Sets the x and y values then organizes the images of enemies into the right rows depending on what type they are
	public Enemy(int xx, int yy, int typ){
		x=xx;
		y=yy;
		if(typ==0){
			type=new ImageIcon("enemy3.png").getImage();
			t=3;
		}
		if(typ==1 || typ==2){
			type=new ImageIcon("enemy2.png").getImage();
			t=2;
		}
		if(typ==3 || typ==4){
			type=new ImageIcon("enemy1.png").getImage();
			t=1;
		}
	}
}															// ..and that's all she wrote. c: